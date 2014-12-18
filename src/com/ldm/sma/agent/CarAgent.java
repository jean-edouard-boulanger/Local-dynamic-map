package com.ldm.sma.agent;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

import com.ldm.data.structure.Pair;
import com.ldm.model.GPS;
import com.ldm.model.GPSObserver;
import com.ldm.model.LocalData;
import com.ldm.model.RecentData;
import com.ldm.model.factory.RoadNetworkFactory;
import com.ldm.model.geometry.Position;
import com.ldm.model.helper.ColorHelper;
import com.ldm.model.log.BroadcastMessageLog;
import com.ldm.model.log.ExplorationLog;
import com.ldm.model.log.GPSLog;
import com.ldm.model.log.DataLog;
import com.ldm.model.log.Log;
import com.ldm.model.log.ReceiveMessageLog;
import com.ldm.model.manager.RecentDataManager;
import com.ldm.sma.agent.helper.AgentHelper;
import com.ldm.sma.message.ExplorationAnswerMessage;
import com.ldm.sma.message.ExplorationRequestMessage;
import com.ldm.sma.message.Message;
import com.ldm.sma.message.RecentDataMessage;
import com.ldm.sma.message.MessageVisitor;
import com.ldm.sma.behaviour.DriveBehaviour;
import com.ldm.ui.WindowUI;
import com.ldm.ui.WindowUI.carUIEventType;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.GuiEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarAgent extends ShortRangeAgent implements GPSObserver {
	
	/* Indique la position courante du vehicule */
    private Position currentPosition = new Position();
        
    
    /* GPS equipant la voiture */
    private GPS gps;
    
    
    /* Behaviour gerant l'expiration de l'enregistrement d'une donnee locale */
    private TimerBehaviour timerBehaviour;
    private final long largestTimerValue = 600000;
    
    
    /* Set stockant les identifiants des precedantes explorations */
    private HashSet<String> previousExplorationRequests = new HashSet<String>();
    
    /* Hashmap stockant les behaviours des explorations courantes (Identifiant exploration => Behaviour) */
    private HashMap<String, ParallelBehaviour> explorationBehaviours = new HashMap<>();
    
    /* Hashmap stockant les identifiants des sequences de requête d'exploration courantes (Identifiant exploration => Sequence) */
    private HashMap<String, HashSet<Integer>> explorationRequests = new HashMap<>();
    
    
    /* Manager gerant les donnees locales et recentes */
    RecentDataManager recentDataManager;
    
	private PropertyChangeSupport propertyChangeCarAgent;
	WindowUI carUI = null;
	
	/**
	 * Constructeur de l'agent voiture
	 */
	public CarAgent(){
		super();
		propertyChangeCarAgent = new PropertyChangeSupport(this);
		
		/* Creation d'une carte GPS a partir du fichier map */
		File mapFile = new File("gps.map");
		try {
			this.gps = new GPS(RoadNetworkFactory.BuildFromFile(mapFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/* L'agent voiture souscrit aux notifications du GPS */
		gps.subscribe(this);
		
		this.recentDataManager = new RecentDataManager(gps);
	}
	
	/**
	 * @return Instance du GPS equipant la voiture
	 */
	public GPS getGPS(){
		return this.gps;
	}
	
	/**
	 * @return Position courante de la voiture
	 */
	@Override
	public Position getCurrentPosition(){
		return this.currentPosition;
	}
	
	/**
	 * Permet d'indiquer la position courante de la voiture
	 * @param p Position de la voiture
	 */
	public void setCurrentPosition(Position p){		
		this.currentPosition = p;
		
		/* Notifie le GPS que la voiture a change de position */
		this.gps.setCurrentPosition(p);
	}
	
	@Override
	public void setup(){
		super.setup();
				
		this.setCurrentPosition(this.gps.getMap().getIntersectionPosition(1));
		this.gps.setDestination(5);
		
		/* Lancement de l'interface graphique pour une voiture */
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JFXPanel();
				javafx.application.Platform.runLater(new Runnable() {
					@Override
					public void run() {
						CarAgent.this.carUI = WindowUI.startUI(CarAgent.this, propertyChangeCarAgent);
					}
				});
			}
		});
		
		/* Ajout du Behaviour traitant les messages principaux */
		this.addBehaviour(new HandleMessagesBehaviour(this));
		
		/* Ajout du Behaviour conduisant la voiture */
		this.addBehaviour(new DriveBehaviour(this));
		
		/* Ajout du Behaviour envoyant periodiquement les donnees recentes aux vehicules alentours  */
		this.addBehaviour(new BroadcastRecentDataBehaviour(this, 15000));
	}
	
	/**
	 * Fonction executee lorsque la voiture reçoit n'importe quel message de l'exterieur
	 * Notifie l'interface utilisateur de la reception
	 * @param aclMsg Message reçu
	 * @param sentAtPosition Position a laquelle le message a ete envoye
	 */
	@Override
	public void onMessageReceivedFromAround(ACLMessage aclMsg, Position sentAtPosition){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.messageReceived.toString(), null, sentAtPosition);
	}
	
	/**
	 * Fonction executee lorsque la voiture envoie un message vers l'exterieur
	 * Notifie l'interface utilisateur de l'envoie
	 * @param aclMsg Message envoye
	 * @param sentAtPosition Position a laquelle le message a ete envoye
	 */
	@Override
	public void onMessageSentAround(ACLMessage aclMsg, Position sentAtPosition){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.messageSent.toString(), null, sentAtPosition);
	}
	
	/**
	 * Fonction executee lorsque la voiture passe une intersection
	 * Utilisee pour traiter l'enregistrement des informations locales
	 */
	@Override
	public void onIntersectionPassed(Position intersectionPosition) {
		
		/* Si il existe, supprime le timer courant */
		if(this.timerBehaviour != null){
			this.removeBehaviour(timerBehaviour);
			this.timerBehaviour = null;
		}
		
		Integer nextRoad = gps.getNextItineraryRoad();
	
		/* Prepare l'enregistrement de l'information locale pour la prochaine route 
		 * Recupère la donnee locale precedemment enregistree si elle existe
		 */
		LocalData newLocalData = this.recentDataManager.prepareNextLocalData(nextRoad, this.getAID());
	
		/* Fusionne la donnee locale avec les donnees recentes */
		this.mergeLocalData(newLocalData);
		
		if(nextRoad == null) return;
		
		Double travelTime = this.gps.getMap().getRoadTravelTime(nextRoad);
		
		if(travelTime == null) return;
		
		/* On ajoute un timer si l'enregistrement d'une nouvelle donnee locale a debute */
		Date wakeDate = new Date( (new Date()).getTime() + travelTime.intValue() * 2000 );
		System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@onIntersectionPassed] Timer will wake up a " + wakeDate + " if the car is too slow");
		
		this.timerBehaviour = new TimerBehaviour(this, wakeDate);
		this.addBehaviour(timerBehaviour);	
	}
	
	/**
	 * Fonction executee par la voiture lorsque sa position change
	 * Envoie les nouvelles coordonnees de la voiture a l'interface utilisateur
	 * @param newPosition Nouvelle position de la voiture
	 */
	@Override
	public void onPositionChanged(Position newPosition) {
		propertyChangeCarAgent.firePropertyChange(carUIEventType.carMoved.toString() , null, newPosition);
	}
	
	/**
	 * Fonction executee par la voiture lorsqu'elle atteint sa destination
	 * Notifie l'arrivee a l'interface utilisateur
	 */
	@Override
	public void onDestinationReached() {
		this.log(new GPSLog("Destination atteinte"));
		propertyChangeCarAgent.firePropertyChange(carUIEventType.destinationReached.toString() , null, null);
	}
	
	/**
	 * Fonction executee par la voiture lorsque l'itineraire du GPS est change
	 * Envoie le nouvel itineraire a l'interface utilisateur
	 * @param itinerary Nouvel itineraire
	 */
	@Override
	public void onItinerarySet(LinkedList<Integer> itinerary){
		this.log(new GPSLog("Itineraire modifie: " + itinerary));
		propertyChangeCarAgent.firePropertyChange(carUIEventType.itinerarySet.toString() , null, itinerary);
	}
	
	/**
	 * Fonction executee par la voiture lorsque l'itineraire est recalcule
	 * Envoie le nouvel itineraire a l'interface utilisateur
	 * @param itinerary Nouvel itineraire
	 */
	@Override
	public void onItineraryReplanned(LinkedList<Integer> itinerary){
		this.log(new GPSLog("Itineraire recalcule automatiquement"));
		propertyChangeCarAgent.firePropertyChange(carUIEventType.itinerarySet.toString() , null, itinerary);
	}
	
	/**
	 * Fonction executee par la voiture lorsque l'elle passe par un point de l'itineraire
	 * Envoie le point par lequel la voiture est passee a l'interface utilisateur
	 * @param wayPoint Identifiant du point de passage
	 */
	@Override
	public void onWayPointPassed(Integer wayPoint){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.wayPointPassed.toString() , null, wayPoint);
	}
	
	/**
	 * Fonction executee par la voiture lorsqu'elle change de route
	 * @param road Identifiant de la nouvelle route
	 */
	@Override
	public void onRoadChanged(Integer road){
		this.log(new GPSLog("Changement de route ( "+ road +" )"));
	}
	
	/**
	 * Fonction executee par la voiture lorsque la navigation est arretee
	 */
	@Override
	public void onNavigationStop() {
		
	}
	
	/**
	 * Agrege les donnees recentes de la voiture avec la nouvelle donnee locale
	 * Envoie les donnees aggregees aux voitures alentours
	 * @param newLocalData Nouvelle donnee locale
	 */
	public void mergeLocalData(LocalData newLocalData){
		if(newLocalData == null) return;
		
		Pair<RecentData, Boolean> lb = this.recentDataManager.merge(newLocalData);
		this.log(new DataLog("Donnee locale aggregee ("+ lb.first +")"));
		
		if(lb.second == false) return;
		
		/* Si la donnee recente de la voiture a ete modifiee (lb.second == true), on envoie autour de nous */
		RecentDataMessage m = new RecentDataMessage(lb.first);
		AgentHelper.sendMessageAround(this, ACLMessage.PROPAGATE, m);
		
		this.log(new BroadcastMessageLog("Donnee recente envoyee aux vehicules alentours ("+ lb.first +")"));
		
		System.out.println("[DEBUG@"+ this.getLocalName() +"@onIntersectionPassed] RecentData sent "
				+ "(Road: "+ lb.first.getRoadId() +"  AverageDriveTime: "+ lb.first.getAverageTravelTime() +")");
	}
	
	/**
	 * Behaviour envoyant periodiquement les donnees recentes de la voiture aux vehicules alentours
	 */
	public class BroadcastRecentDataBehaviour extends TickerBehaviour{

		public BroadcastRecentDataBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			ArrayList<Integer> roads = CarAgent.this.gps.getMap().getAllRoadsNear(CarAgent.this.getCurrentPosition(), 700);
			ArrayList<RecentData> rds = CarAgent.this.recentDataManager.getRecentDataForRoads(roads);
			if(rds.size() > 0){
				System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@BroadCastRecentDataBehaviour] Broadcasting recent data around: " + rds);

				RecentDataMessage rdMessage = new RecentDataMessage(rds);
				AgentHelper.sendMessageAround(CarAgent.this, ACLMessage.PROPAGATE, rdMessage);
			}
		}
	}
		
	/**
	 * Behaviour traitant la receptions des messages les plus courants
	 */
	public class HandleMessagesBehaviour extends Behaviour{

		public HandleMessagesBehaviour(Agent agent){
			super(agent);
		}
		
		@Override
		public void action() {
			
		   /* Reception des donnees recentes d'autres vehicules */
           boolean received1 = AgentHelper.receiveMessageFromAround(CarAgent.this, MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE), new MessageVisitor(){
        	   public boolean onRecentDataMessage(RecentDataMessage message, ACLMessage aclMsg){
        		   System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@receiveMessageFromAround] Recent data received from " + aclMsg.getSender().getLocalName()
        		   		+ "(Road: " + message.getRecentDatas().get(0).getRoadId() + " Number contributors: " + message.getRecentDatas().get(0).getContributors().size() + ")");
        		   
        		   CarAgent.this.log(new ReceiveMessageLog("Reception d'une donnee recente de " + aclMsg.getSender().getLocalName() + " concernant "+ message.getRecentDatas().size() +" route(s)"));
        		   
        		   RecentDataMessage rdMessage = new RecentDataMessage();
        		   
        		   for(RecentData recentData : message.getRecentDatas()){
        			   Pair<RecentData, Boolean> merged = CarAgent.this.recentDataManager.merge(recentData);
        			   if(merged.second == true && CarAgent.this.gps.getMap().isRoadNear(merged.first.getRoadId(), CarAgent.this.getCurrentPosition(), 700.0)){
        				   rdMessage.addRecentData(merged.first);
        			   }
        		   }
        		           		   
        		   if(rdMessage.size() > 0){        			
            		   CarAgent.this.log(new DataLog("Donnee recente reçue aggregee"));

        			   AgentHelper.sendMessageAround(CarAgent.this, ACLMessage.PROPAGATE, rdMessage);
            		   System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@receiveMessageFromAround] Broadcasted " + rdMessage.size() + " merged recent data");
            		   
            		   CarAgent.this.log(new BroadcastMessageLog("Donnee recente aggregee envoyee aux vehicules alentours"));
        		   }
        		   
        		   return true;
        	   }
            });		
           
           /* Reception des requetes d'exploration */
           boolean received2 = AgentHelper.receiveMessageFromAround(CarAgent.this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST), new MessageVisitor(){
        	   
        		public boolean onExplorationRequestMessage(ExplorationRequestMessage message, ACLMessage aclMsg){
        			
        			if(CarAgent.this.previousExplorationRequests.contains(message.getExplorationRequestId())) return true;
        			
        			if(!CarAgent.this.explorationBehaviours.containsKey(message.getExplorationRequestId())){
            			CarAgent.this.log(new ExplorationLog("Reception d'une demande d'exploration de " + message.getRequestIssuer().getLocalName()));
        				
        				ParallelBehaviour explorationBehaviour = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        				explorationBehaviour.addSubBehaviour(new ExplorationBehaviour(CarAgent.this, message));
        				
        				CarAgent.this.explorationBehaviours.put(message.getExplorationRequestId(), explorationBehaviour);
        				CarAgent.this.explorationRequests.put(message.getExplorationRequestId(), new HashSet<Integer>());
        				
        				CarAgent.this.addBehaviour(explorationBehaviour);
        			}
        			
        			HashSet<Integer> receivedExplorationRequests = CarAgent.this.explorationRequests.get(message.getExplorationRequestId());
        			if(receivedExplorationRequests == null) return true;
        			
        			if(receivedExplorationRequests.contains(message.getSequenceNumber())) return true;
        			
        			receivedExplorationRequests.add(message.getSequenceNumber());
        			
        			if(message.getTtl() == 0) return true;
        			message.addHop();

        			CarAgent.this.log(new ExplorationLog("Diffusion d'une demande d'exploration de " + message.getRequestIssuer().getLocalName()));
        			
        			AgentHelper.sendMessageAround(CarAgent.this, ACLMessage.REQUEST, message);
        			
        			return true;
        		}
        	   
           });
           
           if(!received1 && !received2){
        	   block();
           }
		}

		@Override
		public boolean done() {
			return false;
		}
	}
	
	public class TimerBehaviour extends WakerBehaviour{

		public TimerBehaviour(Agent a, Date wakeupDate) {
			super(a, wakeupDate);
		}
		
		@Override
		public void onWake(){
			System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@TimerBehaviour] Woke up because the car was too slow, will get and send checkpoint local data");
			
			Double progress = CarAgent.this.gps.getCurrentRoadProgess();
			if(progress == null) return;
			
			LocalData partialLocalData = CarAgent.this.recentDataManager.getLocalDataCheckpoint(progress);
			CarAgent.this.mergeLocalData(partialLocalData);
			
			Double remaining = (1 - progress) * CarAgent.this.gps.getMap().getRoadTravelTime(CarAgent.this.gps.getCurrentRoad()) * 2000;
			if(remaining.isInfinite()){
				remaining = (double) largestTimerValue;
			}
			
			Date wakeDate = new Date( (new Date()).getTime() + (long) Math.round(remaining) );
			
			System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@TimerBehaviour] Timer will wake up at " + wakeDate + " if the car is too slow");
			
			CarAgent.this.timerBehaviour = new TimerBehaviour(CarAgent.this, wakeDate);
			CarAgent.this.addBehaviour(CarAgent.this.timerBehaviour);
		}
	}
		
	public class ExplorationBehaviour extends Behaviour{
				
		private final ExplorationRequestMessage initialRequestMessage;
		private final Date endDate;
				
		private HashSet<AID> broadcastedAnswersIssuers = new HashSet<>();
		
		public ExplorationBehaviour(Agent a, ExplorationRequestMessage initialRequestMessage) {
			super(a);
			double distance = Position.evaluateDistance(CarAgent.this.getCurrentPosition(), CarAgent.this.gps.getMap().getIntersectionPosition(initialRequestMessage.getItinerary().getLast()));
			this.endDate = new Date(System.currentTimeMillis() + (long)(distance * 4));
			this.initialRequestMessage = initialRequestMessage;
		}
		
		@Override
		public void action() {
			
			boolean received = AgentHelper.receiveMessageFromAround(CarAgent.this, MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId(initialRequestMessage.getExplorationRequestId())), new MessageVisitor(){
				
				public boolean onExplorationAnswerMessage(ExplorationAnswerMessage message, ACLMessage aclMsg){
					
					if(broadcastedAnswersIssuers.contains(message.getAnswerIssuer())) return true;
					
					broadcastedAnswersIssuers.add(message.getAnswerIssuer());
					
					if(ExplorationBehaviour.this.initialRequestMessage.getRequestIssuer() == CarAgent.this.getAID()){
						CarAgent.this.log(new ExplorationLog("Reponse envoyee par "+ message.getAnswerIssuer().getLocalName() +" "
								+ "reçue pour la demande d'exploration " + ExplorationBehaviour.this.initialRequestMessage.getExplorationRequestId()));
						
						System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@onExplorationAnswerMessage] Answer received from " + message.getAnswerIssuer().getLocalName() + ": " + message.getRecentDatas());
						for(RecentData rd : message.getRecentDatas()){
							CarAgent.this.recentDataManager.merge(rd);
						}
					}
					else{
						if(message.decreaseTtl() <= 0){return true;}
						CarAgent.this.log(new ExplorationLog("Diffusion d'une reponse pour la demande d'exploration " + ExplorationBehaviour.this.initialRequestMessage.getExplorationRequestId()));

						AgentHelper.sendMessageAround(CarAgent.this, ACLMessage.INFORM, ExplorationBehaviour.this.initialRequestMessage.getExplorationRequestId(), message);
					}	
					return true;
				}
			});
			
			if(!received){
				this.block();
			}
			
		}

		@Override
		public void onStart(){
			if(this.initialRequestMessage.getRequestIssuer().equals(CarAgent.this.getAID())){
				p().addSubBehaviour(new ExplorationRequestBroadcastBehaviour(CarAgent.this, 10000, 4, this.initialRequestMessage));
			}
			else
			{
				ExplorationAnswerMessage answerMessage = new ExplorationAnswerMessage();
				answerMessage.setAnswerIssuer(CarAgent.this.getAID());
				answerMessage.setTtl(this.initialRequestMessage.getHops() * 2);
				LinkedList<Integer> itinerary = initialRequestMessage.getItinerary();
				
				for(int i = 0; i < initialRequestMessage.getItinerary().size() - 1; i++){
					Integer road = CarAgent.this.getGPS().getMap().getRoad(itinerary.get(i), itinerary.get(i+1));
					if(road != null){
						answerMessage.addRecentData(CarAgent.this.recentDataManager.getRecentDataForRoad(road));
					}
				}
				
				if(!answerMessage.getRecentDatas().isEmpty()){
					CarAgent.this.log(new ExplorationLog("Reponse envoyee pour la demande d'exploration " + this.initialRequestMessage.getExplorationRequestId()));
					AgentHelper.sendMessageAround(CarAgent.this, ACLMessage.INFORM, initialRequestMessage.getExplorationRequestId(), answerMessage);
				}
			}
		}
		
		public ParallelBehaviour p(){
			return (ParallelBehaviour)this.parent;
		}
		
		@Override
		public int onEnd(){
			CarAgent.this.log(new ExplorationLog("Terminaison de l'exploration " + this.initialRequestMessage.getExplorationRequestId()));
			
			CarAgent.this.previousExplorationRequests.add(this.initialRequestMessage.getExplorationRequestId());
			ParallelBehaviour bhv = CarAgent.this.explorationBehaviours.get(this.initialRequestMessage.getExplorationRequestId());
			CarAgent.this.explorationBehaviours.remove(this.initialRequestMessage.getExplorationRequestId());
			
			if(CarAgent.this.getAID().equals(this.initialRequestMessage.getRequestIssuer())){
				
				LinkedList<Integer> cit = CarAgent.this.gps.getItinerary();
				boolean itineraryChanged = false;
				for(int i = 0; i < cit.size(); i++){
					if(!cit.get(cit.size() - 1 - i).equals(this.initialRequestMessage.getItinerary().get(this.initialRequestMessage.getItinerary().size() - 1 - i))){
						itineraryChanged = true;
						break;
					}
				}
				
				if(itineraryChanged){
					CarAgent.this.log(new ExplorationLog("Lancement d'une vague d'exploration car l'itineraire a ete ameliore"));
					CarAgent.this.initializeExplorationRequest();
				}
			}
			
			CarAgent.this.removeBehaviour(bhv);
			return 0;
		}
		
		@Override
		public boolean done() {
			return System.currentTimeMillis() > this.endDate.getTime();
		}
	}
	
	public class ExplorationRequestBroadcastBehaviour extends TickerBehaviour{
		
		private final int lastSequenceNumber;
		private final ExplorationRequestMessage requestMessage;
		private String conversationId = null;
		private int performative = ACLMessage.REQUEST;
		
		public ExplorationRequestBroadcastBehaviour(Agent a, long period, int lastSequenceNumber, ExplorationRequestMessage broadcastedMessage) {
			super(a, period);
			this.lastSequenceNumber = lastSequenceNumber;
			this.requestMessage = broadcastedMessage;
		}
		
		public void setPerformative(int performative){
			this.performative = performative;
		}
		
		public void setConversationId(String conversationId){
			this.conversationId = conversationId;
		}
		
		private void sendMessage(){
			if(this.requestMessage.increaseSequenceNumber() > this.lastSequenceNumber){
				this.stop();
				return;
			}
			CarAgent.this.log(new ExplorationLog("Envoie d'une requête d'exploration aux vehicules alentours (Sequence "+ requestMessage.getSequenceNumber() +") "
					+ "pour la requête " + requestMessage.getExplorationRequestId()));
			AgentHelper.sendMessageAround(CarAgent.this, this.performative, this.conversationId, this.requestMessage);
		}
		
		@Override
		public void onStart(){}
		
		@Override
		protected void onTick() {
			this.sendMessage();
		}
	}
	
	private void initializeExplorationRequest(){		
		String explorationId = this.getLocalName() + (new Date()).getTime();
		
		LinkedList<Integer> itinerary = this.gps.getItinerary();
		double distance = Position.evaluateDistance(gps.getMap().getIntersectionPosition(itinerary.getFirst()), 
				gps.getMap().getIntersectionPosition(itinerary.getLast()));
		
		ExplorationRequestMessage explorationRequestMessage = new ExplorationRequestMessage();
		explorationRequestMessage.setExplorationRequestId(explorationId);
		explorationRequestMessage.setItinerary(new LinkedList<Integer>(this.gps.getItinerary()));
		explorationRequestMessage.setRequestIssuer(CarAgent.this.getAID());
		explorationRequestMessage.setTtl((int)(distance * 0.03));
		
		this.log(new ExplorationLog("Initialisation d'une requête d'exploration ("+ explorationId +")"));
				
		ParallelBehaviour explorationBehaviour = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
		explorationBehaviour.addSubBehaviour(new ExplorationBehaviour(CarAgent.this, explorationRequestMessage));
		this.explorationBehaviours.put(explorationId, explorationBehaviour);
		CarAgent.this.addBehaviour(explorationBehaviour);
	}
	
	@Override
	protected void onGuiEvent(GuiEvent event) {
		if(event.getType() == carUIEventType.intersectionClicked.ordinal()){
			Integer intersection = (Integer)event.getParameter(0);
			this.gps.setDestination(intersection);
		}
		else if(event.getType() == carUIEventType.explorationRequested.ordinal()){
			System.err.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@explorationRequested] Exploration requested by user");
			this.initializeExplorationRequest();
		}
	}
	
	public void log(Log log){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.logPosted.toString(), null, log);
	}
	
}
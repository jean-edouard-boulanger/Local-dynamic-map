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
import com.ldm.model.manager.RecentDataManager;
import com.ldm.sma.agent.helper.AgentHelper;
import com.ldm.sma.message.RecentDataMessage;
import com.ldm.sma.message.MessageVisitor;
import com.ldm.sma.message.PokeMessage;
import com.ldm.sma.behaviour.DriveBehaviour;
import com.ldm.ui.WindowUI;
import com.ldm.ui.WindowUI.carUIEventType;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiEvent;

import java.util.ArrayDeque;
import java.util.LinkedList;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarAgent extends ShortRangeAgent implements GPSObserver {
	
    private Position currentPosition = new Position();
        
    private GPS gps;
    
    RecentDataManager recentDataManager;
    
	private PropertyChangeSupport propertyChangeCarAgent;
	WindowUI carUI = null;
		
	public CarAgent(){
		super();
		propertyChangeCarAgent = new PropertyChangeSupport(this);
		this.recentDataManager = new RecentDataManager();
	}
	
	public GPS getGPS(){
		return this.gps;
	}
	
	@Override
	public Position getCurrentPosition(){
		return this.currentPosition;
	}
	
	public void setCurrentPosition(Position p){		
		this.currentPosition = p;
		this.gps.setCurrentPosition(p);
	}
	
	@Override
	public void setup(){
		super.setup();
		
		File mapFile = new File("gps.map");
		try {
			this.gps = new GPS(RoadNetworkFactory.BuildFromFile(mapFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		gps.subscribe(this);
		
		this.setCurrentPosition(this.gps.getRandomIntersectionPosition());
		//this.gps.setCurrentPosition(this.gps.getMap().getIntersectionPosition(1));
		this.gps.setDestination(this.gps.getRandomIntersection());
		
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
				
		this.addBehaviour(new HandleMessagesBehaviour(this));
		
		this.addBehaviour(new SendPokeBehaviour(this, 10000));
				
		this.addBehaviour(new DriveBehaviour(this));
	}
	
	@Override
	public void onMessageReceivedFromAround(ACLMessage aclMsg, Position sentAtPosition){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.messageReceived.toString(), null, sentAtPosition);
	}
	
	@Override
	public void onMessageSentAround(ACLMessage aclMsg, Position sentAtPosition){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.messageSent.toString(), null, sentAtPosition);
	}
	
	@Override
	public void onIntersectionPassed(Position intersectionPosition) {
		Integer nir = gps.getNextItineraryRoad();
		LocalData newLocalData = this.recentDataManager.prepareNextLocalData(nir, this.getAID());
		if(newLocalData == null) return;
		
		Pair<RecentData, Boolean> lb = this.recentDataManager.merge(newLocalData);
		if(lb.second == false) return;
		
		RecentDataMessage m = new RecentDataMessage(lb.first);
		AgentHelper.sendMessageAround(this, ACLMessage.PROPAGATE, m);
	}
	
	@Override
	public void onPositionChanged(Position newPosition) {
		propertyChangeCarAgent.firePropertyChange(carUIEventType.carMoved.toString() , null, newPosition);
	}
	
	@Override
	public void onDestinationReached() {
		//this.gps.setDestination(this.gps.getRandomIntersection());
	}
	
	@Override
	public void onItinerarySet(LinkedList<Integer> itinerary){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.itinerarySet.toString() , null, itinerary);
	}
	
	@Override
	public void onWayPointPassed(Integer wayPoint){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.wayPointPassed.toString() , null, wayPoint);
	}
	
	@Override
	public void onRoadChanged(Integer road){
		
	}
	
	@Override
	public void onNavigationStop() {
		
	}
	
	
	public class SendPokeBehaviour extends TickerBehaviour{
		public SendPokeBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			System.out.println("[DEBUG@"+ this.myAgent.getLocalName() +"] Poke sent");
			AgentHelper.sendMessageAround(CarAgent.this, ACLMessage.INFORM, new PokeMessage());
		}
	}
	
	public class HandleMessagesBehaviour extends Behaviour{
		
		public HandleMessagesBehaviour(Agent agent){
			super(agent);
		}
		
		@Override
		public void action() {
           boolean received = AgentHelper.receiveMessageFromAround(CarAgent.this, MessageTemplate.MatchPerformative(ACLMessage.INFORM), new MessageVisitor(){
                public boolean onIRMessage(RecentDataMessage message, ACLMessage aclMsg){
                	return true;
                }
            });		
           
           if(!received){
        	   block();
           }
		}

		@Override
		public boolean done() {
			return false;
		}
	}
	
	@Override
	protected void onGuiEvent(GuiEvent event) {
		if(event.getType() == carUIEventType.intersectionClicked.ordinal()){
			Integer intersection = (Integer)event.getParameter(0);
			this.gps.setDestination(intersection);
		}
	}
}

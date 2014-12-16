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
import java.util.ArrayList;
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
		
		File mapFile = new File("gps.map");
		try {
			this.gps = new GPS(RoadNetworkFactory.BuildFromFile(mapFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		gps.subscribe(this);
		
		this.recentDataManager = new RecentDataManager(gps);
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
				
		//this.setCurrentPosition(this.gps.getRandomIntersectionPosition());
		this.setCurrentPosition(this.gps.getMap().getIntersectionPosition(1));
		this.gps.setDestination(5);
		
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
						
		this.addBehaviour(new DriveBehaviour(this));
		
		//this.addBehaviour(new BroadCastRecentDataBehaviour(this, 20000));
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
		LocalData newLocalData = this.recentDataManager.prepareNextLocalData(gps.getNextItineraryRoad(), this.getAID());
		if(newLocalData == null) return;
		
		Pair<RecentData, Boolean> lb = this.recentDataManager.merge(newLocalData);
		if(lb.second == false) return;
		
		RecentDataMessage m = new RecentDataMessage(lb.first);
		AgentHelper.sendMessageAround(this, ACLMessage.PROPAGATE, m);
		
		System.out.println("[DEBUG@"+ this.getLocalName() +"@onIntersectionPassed] RecentData sent "
				+ "(Road: "+ lb.first.getRoadId() +"  AverageDriveTime: "+ lb.first.getAverageTravelTime() +")");
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
	
	public class BroadCastRecentDataBehaviour extends TickerBehaviour{

		public BroadCastRecentDataBehaviour(Agent a, long period) {
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
	
	public class HandleMessagesBehaviour extends Behaviour{
		
		public HandleMessagesBehaviour(Agent agent){
			super(agent);
		}
		
		@Override
		public void action() {
           boolean received = AgentHelper.receiveMessageFromAround(CarAgent.this, MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE), new MessageVisitor(){
        	   public boolean onRecentDataMessage(RecentDataMessage message, ACLMessage aclMsg){
        		   System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@receiveMessageFromAround] Recent data received from " + aclMsg.getSender().getLocalName()
        		   		+ "(Road: " + message.getRecentDatas().get(0).getRoadId() + " Number contributors: " + message.getRecentDatas().get(0).getContributors().size() + ")");
        		   
        		   RecentDataMessage rdMessage = new RecentDataMessage();
        		   
        		   for(RecentData recentData : message.getRecentDatas()){
        			   Pair<RecentData, Boolean> merged = CarAgent.this.recentDataManager.merge(recentData);
        			   if(merged.second == true){
        				   rdMessage.addRecentData(merged.first);
        			   }
        		   }
        		   
        		   if(rdMessage.size() > 0){
        			   AgentHelper.sendMessageAround(CarAgent.this, ACLMessage.PROPAGATE, rdMessage);
            		   System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"@receiveMessageFromAround] Broadcasted " + rdMessage.size() + " merged recent data");
        		   }
        		   
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

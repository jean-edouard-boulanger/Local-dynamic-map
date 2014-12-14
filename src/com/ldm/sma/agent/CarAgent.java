package com.ldm.sma.agent;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

import com.ldm.model.GPS;
import com.ldm.model.GPSObserver;
import com.ldm.model.factory.RoadNetworkFactory;
import com.ldm.model.geometry.Position;
import com.ldm.model.structure.DL;
import com.ldm.model.structure.IR;
import com.ldm.sma.agent.helper.AgentHelper;
import com.ldm.sma.message.IRMessage;
import com.ldm.sma.message.MessageVisitor;
import com.ldm.sma.behaviour.DriveBehaviour;
import com.ldm.ui.WindowUI;
import com.ldm.ui.WindowUI.carUIEventType;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.gui.GuiEvent;

import java.util.ArrayDeque;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class CarAgent extends ShortRangeAgent implements GPSObserver {
    
    // Tableau stockant les différents IR valides d'un véhicule
    private ArrayList<IR> IRsCollection = new ArrayList<>();
	
    private Position currentPosition = new Position();
    
    private double currentSpeed = 500;
    
    private GPS gps;
    private DL currentDL;
    
	private PropertyChangeSupport propertyChangeCarAgent;
	WindowUI carUI = null;
		
	public CarAgent(){
		super();
		propertyChangeCarAgent = new PropertyChangeSupport(this);
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
	
	public double getCurrentSpeed(){
		return this.currentSpeed;
	}
	
	public void setCurrentSpeed(double currentSpeed){
		this.currentSpeed = currentSpeed;
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
		
		this.addBehaviour(new DriveBehaviour(this));
	}
	
	@Override
	public void onIntersectionPassed(Position intersectionPosition) {
		Date date = new Date();
		if(currentDL != null){
			System.out.println("[Intersection passed save DL]");
			this.currentDL.saveDL(intersectionPosition, date);
			this.aggregateDL(currentDL);
		}
		System.out.println("[Intersection passed create new DL]");
		this.currentDL = new DL(intersectionPosition, date);
		
	}
	
	@Override
	public void onPositionChanged(Position newPosition) {
		propertyChangeCarAgent.firePropertyChange(carUIEventType.carMoved.toString() , null, newPosition);
	}
	
	@Override
	public void onDestinationReached() {
		this.gps.setDestination(this.gps.getRandomIntersection());
	}
	
	@Override
	public void onItinerarySet(ArrayDeque<Integer> itinerary){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.itinerarySet.toString() , null, itinerary);
	}
	
	@Override
	public void onWayPointPassed(Integer wayPoint){
		propertyChangeCarAgent.firePropertyChange(carUIEventType.wayPointPassed.toString() , null, wayPoint);
	}
	
	@Override
	public void onRoadChanged(Integer road){
		
	}
	
	public class HandleMessagesBehaviour extends Behaviour{
		
		public HandleMessagesBehaviour(Agent agent){
			super(agent);
		}
		
		@Override
		public void action() {
			ACLMessage m = receive();
			if(m != null)
				System.out.println(m.getContent());
               boolean received = AgentHelper.receiveMessageFromAround(CarAgent.this, MessageTemplate.MatchPerformative(ACLMessage.INFORM), new MessageVisitor(){
                    public boolean onIRMessage(IRMessage message, ACLMessage aclMsg){
                        aggregateIR(message.getIR());
                        System.out.println("[Receive message :] "+CarAgent.this.IRsCollection.toString());
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
      
    public ArrayList<IR> getIRsCollection(){
        return this.IRsCollection;
    }
    
    // ajout d'un nouveau IR en supprimant l'ancien si invalide
    public IR aggregateIR(IR newIR)
    {
    	System.out.println("==========aggregateIR==============");
        Position pos1 = newIR.getPosDepart();
        Position pos2 = newIR.getPosArrivee();
        
        int ArrayPos = lookForIR(pos1, pos2);
        IR returnedIR = null;
        if ( ArrayPos == -1)
        {
            this.IRsCollection.add(newIR);
            returnedIR = newIR;
            AgentHelper.sendMessageAround(this, ACLMessage.INFORM,new IRMessage (newIR));
        }
        else
        {
        	
            if (this.IRsCollection.get(ArrayPos).isTooOld())
            {
                this.IRsCollection.set(ArrayPos, newIR);
                returnedIR = newIR;
                AgentHelper.sendMessageAround(this, ACLMessage.INFORM,new IRMessage (newIR));
            }
        }
        System.out.println("je suis IR man " +newIR.getAverageTime());
        return returnedIR;
    }
    
    private int lookForIR(Position posD, Position posA)
    {
        int IROffset = -1;
        Iterator<IR> it = IRsCollection.iterator();
        while(it.hasNext())
        {
            IR currentIR = it.next();
            System.out.println("posD = " + posD);
            System.out.println("posA = " + posA);
            System.out.println("currentIR.getPosDepart() = " + currentIR.getPosDepart());
            System.out.println("currentIR.getPosArrivee() = " + currentIR.getPosArrivee());
            if (currentIR.getPosDepart() == posD && currentIR.getPosArrivee() == posA)
            {
                IROffset = IRsCollection.indexOf(currentIR);
                break;
            }
        }
        return IROffset;
    }
    
    public void aggregateDL(DL newDL)
    {
    	System.out.println("==========aggregateDL==============");
        Position pos1 = newDL.getPosDepart();
        Position pos2 = newDL.getPosArrivee();
        
        int ArrayPos = lookForIR(pos1, pos2);
        IR newIR;
        
        if(ArrayPos > 0)
        {
            if(this.IRsCollection.get(ArrayPos).isTooOld())
            {
               newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newDL.getTpsParcours());
            }
            else
            {
                IR oldIR = this.IRsCollection.get(ArrayPos);
                oldIR.updateAverage(newDL.getTpsParcours());
                newIR = oldIR;
                //-----Old
                //long newTemps = (oldIR.getAverageTime() + newDL.getTpsParcours()) / (oldIR.getVehiculesNumber() + 1);
                //newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newTemps);
            }
        }
        else
        {
            newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newDL.getTpsParcours());
            this.IRsCollection.add(newIR);
        }
        if (newIR!=null){
        	System.out.println("[send IR]");
            AgentHelper.sendMessageAround(this, ACLMessage.INFORM,new IRMessage (newIR));
        }
        System.out.println("je suis DL man " +newIR.getAverageTime());
    }
	
	@Override
	protected void onGuiEvent(GuiEvent arg0) {

	}
}

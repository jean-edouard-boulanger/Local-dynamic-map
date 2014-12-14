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
import com.ldm.sma.message.PokeMessage;
import com.ldm.sma.behaviour.DriveBehaviour;
import com.ldm.ui.WindowUI;
import com.ldm.ui.WindowUI.carUIEventType;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiEvent;

import java.util.ArrayDeque;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Iterator;

public class CarAgent extends ShortRangeAgent implements GPSObserver {
    
    // Tableau stockant les différents IR valides d'un véhicule
    private ArrayList<IR> IRsCollection = new ArrayList<>();
	
    private Position currentPosition = new Position();
        
    private GPS gps;
    
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
		
		this.addBehaviour(new SendPokeBehaviour(this, 10000));
				
		this.addBehaviour(new DriveBehaviour(this));
	}
	
	@Override
	public void onIntersectionPassed(Position intersectionPosition) {
		
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
				public boolean onPokeMessage(PokeMessage message, ACLMessage aclMsg){
					System.out.println("[DEBUG@"+ CarAgent.this.getLocalName() +"] Poke received from " + aclMsg.getSender().getLocalName() + " : " + message);
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
        Position pos1 = newIR.getPosDepart();
        Position pos2 = newIR.getPosArrivee();
        
        int ArrayPos = lookForIR(pos1, pos2);
        IR returnedIR = this.IRsCollection.get(ArrayPos);
        if ( ArrayPos == -1)
        {
            this.IRsCollection.add(newIR);
            returnedIR = newIR;
            AgentHelper.sendMessageAround(this, ACLMessage.PROPAGATE,new IRMessage (newIR));
        }
        else
        {
            if (this.IRsCollection.get(ArrayPos).isTooOld())
            {
                this.IRsCollection.set(ArrayPos, newIR);
                returnedIR = newIR;
                AgentHelper.sendMessageAround(this, ACLMessage.PROPAGATE,new IRMessage (newIR));
            }
        }
        return returnedIR;
    }
    
    private int lookForIR(Position posD, Position posA)
    {
        int IROffset = -1;
        Iterator<IR> it = IRsCollection.iterator();
        while(it.hasNext())
        {
            IR currentIR = it.next();
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
        if (newIR!=null)
            AgentHelper.sendMessageAround(this, ACLMessage.PROPAGATE,new IRMessage (newIR));
    }
	
	@Override
	protected void onGuiEvent(GuiEvent arg0) {

	}
}

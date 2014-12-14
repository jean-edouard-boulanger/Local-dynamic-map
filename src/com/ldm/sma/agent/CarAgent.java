package com.ldm.sma.agent;

import java.beans.PropertyChangeSupport;

import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

import com.ldm.model.GPSObserver;
import com.ldm.model.geometry.Position;
import com.ldm.model.structure.DL;
import com.ldm.model.structure.IR;
import com.ldm.sma.agent.helper.AgentHelper;
import com.ldm.sma.message.IRMessage;
import com.ldm.sma.message.MessageVisitor;
import com.ldm.ui.CarUI;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Iterator;

public class CarAgent extends ShortRangeAgent implements GPSObserver {
    
    // Tableau stockant les différents IR valides d'un véhicule
    private ArrayList<IR> IRsCollection = new ArrayList();
	
	private PropertyChangeSupport propertyChangeCarAgent;
	CarUI carUI = null;
		
	public CarAgent(){
		super();
		propertyChangeCarAgent = new PropertyChangeSupport(this);
	}
	
	@Override
	public Position getCurrentPosition(){
		// Temporaire, a modifier
		return new Position(0.0, 0.0);
	}
	
	@Override
	public void setup(){
		super.setup();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JFXPanel();
				javafx.application.Platform.runLater(new Runnable() {
					@Override
					public void run() {
						System.out.println("Starting UI");
						CarAgent.this.carUI = CarUI.startUI(CarAgent.this, propertyChangeCarAgent);
					}
				});
			}
		});
				
		this.addBehaviour(new HandleMessagesBehaviour(this));
	}
	
	@Override
	public void onIntersectionPassed(Position intersectionPosition) {
		
	}
	
	@Override
	public void onDestinationReached() {
		
	}
	
	public class HandleMessagesBehaviour extends OneShotBehaviour{
		
		public HandleMessagesBehaviour(Agent agent){
			super(agent);
		}
		
		@Override
		public void action() {
                    AgentHelper.receiveMessageFromAround(CarAgent.this, null, new MessageVisitor(){
                        public boolean onIRMessage(IRMessage message, ACLMessage aclMsg){
                            aggregateIR(message.getMessage());
                            return true;
                        }
                    });		
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

package com.ldm.sma.agent;

import java.beans.PropertyChangeSupport;

import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

import com.ldm.model.GPSObserver;
import com.ldm.model.geometry.Position;
import com.ldm.model.geometry.Vect;
import com.ldm.model.structure.DL;
import com.ldm.model.structure.IR;
import com.ldm.sma.agent.helper.AgentHelper;
import com.ldm.sma.message.ExampleMessage;
import com.ldm.sma.message.MessageVisitor;
import com.ldm.ui.CarUI;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarAgent extends ShortRangeAgent implements GPSObserver {
    
    // Tableau stockant les différents IR valides d'un véhicule
    private IR[] IRsCollection;
	
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
	public void onWayPointPassed(){
		
	}
	
	@Override
	public void onHeadingChanged(Vect heading){
		
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
						
		}
		
	}
      
    // ajout d'un nouveau IR en supprimant l'ancien si invalide
    public IR aggregateIR(IR newIR)
    {
        Position pos1 = newIR.getPosDepart();
        Position pos2 = newIR.getPosArrivee();
        
        int ArrayPos = lookForIR(pos1, pos2);
        IR returnedIR = this.IRsCollection[ArrayPos];
        if ( ArrayPos == -1)
        {
            int j = this.IRsCollection.length;
            this.IRsCollection[j+1] = newIR;
            returnedIR = newIR;
            // EMISSION IR
        }
        else
        {
            if (this.IRsCollection[ArrayPos].isTooOld())
            {
                this.IRsCollection[ArrayPos] = newIR;
                returnedIR = newIR;
                // EMISSION IR
            }
        }
        return returnedIR;
    }
    
    private int lookForIR(Position posD, Position posA)
    {
        int IROffset = -1;
        for (int i = 0; i < this.IRsCollection.length; i++)
        {
            if (this.IRsCollection[i].getPosDepart() == posD && this.IRsCollection[i].getPosArrivee() == posA)
            {
                IROffset = i;
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
        
        if(ArrayPos > 0)
        {
            if(this.IRsCollection[ArrayPos].isTooOld())
            {
                IR newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newDL.getTpsParcours());
            }
            else
            {
                IR oldIR = this.IRsCollection[ArrayPos];
                long newTemps = (oldIR.getAverageTime() + newDL.getTpsParcours()) / (oldIR.getVehiculesNumber() + 1);
                IR newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newTemps);
            }
        }
        else
        {
            IR newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newDL.getTpsParcours());
            this.IRsCollection[this.IRsCollection.length + 1] = newIR;
        }
        
        // EMISSION IR
    }
	
	@Override
	protected void onGuiEvent(GuiEvent arg0) {

	}
}

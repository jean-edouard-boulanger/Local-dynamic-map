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
import com.ldm.sma.behaviour.DriveBehaviour;
import com.ldm.ui.CarUI;
import com.ldm.ui.CarUI.carUIEventType;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiEvent;

import java.util.ArrayList;
import java.util.Iterator;

public class CarAgent extends ShortRangeAgent implements GPSObserver {
    
    // Tableau stockant les différents IR valides d'un véhicule
    private ArrayList<IR> IRsCollection = new ArrayList<>();
	
    private Position currentPosition = new Position();
    
    private double currentSpeed = 90;
    
    private GPS gps;
    
	private PropertyChangeSupport propertyChangeCarAgent;
	CarUI carUI = null;
		
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
						CarAgent.this.carUI = CarUI.startUI(CarAgent.this, propertyChangeCarAgent);
					}
				});
			}
		});
				
		this.addBehaviour(new HandleMessagesBehaviour(this));
		
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
        IR returnedIR = this.IRsCollection.get(ArrayPos);
        if ( ArrayPos == -1)
        {
            this.IRsCollection.set(ArrayPos, newIR);
            returnedIR = newIR;
            // EMISSION IR
        }
        else
        {
            if (this.IRsCollection.get(ArrayPos).isTooOld())
            {
                this.IRsCollection.set(ArrayPos, newIR);
                returnedIR = newIR;
                // EMISSION IR
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
        
        if(ArrayPos > 0)
        {
            if(this.IRsCollection.get(ArrayPos).isTooOld())
            {
                IR newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newDL.getTpsParcours());
            }
            else
            {
                IR oldIR = this.IRsCollection.get(ArrayPos);
                long newTemps = (oldIR.getAverageTime() + newDL.getTpsParcours()) / (oldIR.getVehiculesNumber() + 1);
                IR newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newTemps);
            }
        }
        else
        {
            IR newIR = new IR(newDL.getPosDepart(), newDL.getPosArrivee(), newDL.getTpsParcours());
            this.IRsCollection.set(ArrayPos, newIR);
        }
        
        // EMISSION IR
    }
	
	@Override
	protected void onGuiEvent(GuiEvent arg0) {

	}
}

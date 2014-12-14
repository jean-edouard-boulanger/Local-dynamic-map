package com.ldm.sma.behaviour;

import com.ldm.model.geometry.Vect;
import com.ldm.model.helper.UnitHelper;
import com.ldm.sma.agent.CarAgent;

import jade.core.behaviours.TickerBehaviour;

public class DriveBehaviour extends TickerBehaviour {

	private static double deltaTime = 0.05;
	
	public DriveBehaviour(CarAgent agent){
		super(agent, 50);
	}

	@Override
	protected void onTick() {		
		if(myCarAgent().getGPS().isNavigationModeOn()){
			myCarAgent().setCurrentSpeed(50);
			
			Vect dx = myCarAgent().getGPS().getHeading().mult(myCarAgent().getCurrentSpeed());
			myCarAgent().setCurrentPosition(myCarAgent().getCurrentPosition().add(dx));	
		}
	}

	private CarAgent myCarAgent(){
		return (CarAgent)this.myAgent;
	}
	
}

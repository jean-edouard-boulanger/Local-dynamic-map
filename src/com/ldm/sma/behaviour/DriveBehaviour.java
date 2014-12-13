package com.ldm.sma.behaviour;

import com.ldm.sma.agent.CarAgent;

import jade.core.behaviours.TickerBehaviour;

public class DriveBehaviour extends TickerBehaviour {

	public DriveBehaviour(CarAgent agent){
		super(agent, 80);
	}

	@Override
	protected void onTick() {

	}

}

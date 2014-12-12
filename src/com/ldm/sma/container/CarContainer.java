package com.ldm.sma.container;

import java.util.Random;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class CarContainer {
	
	public static void main(String[] args){
		Runtime runtime = Runtime.instance();
		Profile profile = null;
		try{
			profile = new ProfileImpl("slave_container.property");
			ContainerController container = runtime.createAgentContainer(profile);
			
			AgentController carAgent = container.createNewAgent("Voiture" + (new Random()).nextInt(), "com.ldm.sma.agent.CarAgent", null);
			
			carAgent.start();
		}
		catch(Exception e){
			System.err.println("Impossible de démarrer la voiture: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
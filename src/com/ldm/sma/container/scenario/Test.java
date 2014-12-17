package com.ldm.sma.container.scenario;

import java.util.Random;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

public class Test {

	public static void main(String[] args){
		Runtime runtime = Runtime.instance();
		Profile mainProfile = null;
		Profile carProfile = null;
		
		try{
			mainProfile = new ProfileImpl("master_container.property");
			AgentContainer masterContainer = runtime.createMainContainer(mainProfile);
			
			carProfile = new ProfileImpl("slave_container.property");
			ContainerController carContainer = runtime.createAgentContainer(carProfile);
			
			AgentController carAgent = carContainer.createNewAgent("Voiture" + (new Random()).nextInt(), "com.ldm.sma.agent.CarAgent", null);
			carAgent.start();
		}
		catch(Exception e){
			System.err.println("Impossible de démarrer le container master: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}

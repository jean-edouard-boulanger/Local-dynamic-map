package com.ldm.sma.container;

import jade.wrapper.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

public class MasterContainer {

	public static void main(String[] args){
		Runtime runtime = Runtime.instance();
		Profile profile = null;
		try{
			profile = new ProfileImpl("master_container.property");
			AgentContainer container = runtime.createMainContainer(profile);
		}
		catch(Exception e){
			System.err.println("Impossible de démarrer le container master: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}

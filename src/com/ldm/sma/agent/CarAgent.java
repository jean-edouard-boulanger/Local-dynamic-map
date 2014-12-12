package com.ldm.sma.agent;

import java.beans.PropertyChangeSupport;

import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

import com.ldm.model.geometry.Position;
import com.ldm.ui.CarUI;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.introspection.SentMessage;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarAgent extends GuiAgent {

	private PropertyChangeSupport propertyChangeCarAgent;
	CarUI carUI = null;
	
	public CarAgent(){
		super();
		propertyChangeCarAgent = new PropertyChangeSupport(this);
	}
	
	public Position getCurrentPosition(){
		// Temporaire, a modifier
		return new Position(0.0, 0.0);
	}
	
	@Override
	public void setup(){
		
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
		
		this.addBehaviour(new DistanceFilterBehaviour(this, 500));
		
		this.addBehaviour(new HandleMessagesBehaviour(this));
	}

	@Override
	protected void onGuiEvent(GuiEvent arg0) {

	}
	
	/**
	 * Simulates the distance between vehicles
	 * Drops a message whenever the distance between the sender car the and receiver is too wide (Greater than maximumDistance)
	 */
	public class DistanceFilterBehaviour extends Behaviour{
		
		private double maximumDistance = 0.;
		
		public DistanceFilterBehaviour(Agent agent, double maximumDistance){
			super(agent);
			this.maximumDistance = maximumDistance;
		}
		
		@Override
		public void action() {
			ACLMessage message = this.myAgent.receive(AgentHelper.filterDistanceProtocol);
			if(message != null){
				message.setProtocol(null);
				
				Position sentAtPosition = AgentHelper.getSentAtPosition(message);
				Position currentPosition = CarAgent.this.getCurrentPosition();
				
				if(sentAtPosition != null && maximumDistance * maximumDistance > Position.evaluateSquareDistance(currentPosition, sentAtPosition)){
					this.myAgent.send(message);
				}
			}
			else{
				this.block();
			}
		}

		@Override
		public boolean done() {
			return false;
		}
	}
	
	public class HandleMessagesBehaviour extends Behaviour{
		
		public HandleMessagesBehaviour(Agent agent){
			super(agent);
		}
		
		@Override
		public void action() {
			
		}

		@Override
		public boolean done() {
			return false;
		}
	}
	
}

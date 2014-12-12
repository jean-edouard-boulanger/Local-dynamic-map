package com.ldm.sma.agent;

import java.beans.PropertyChangeSupport;

import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

import com.ldm.model.geometry.Position;
import com.ldm.ui.CarUI;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class CarAgent extends ShortRangeAgent {
	
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
	protected void onGuiEvent(GuiEvent arg0) {

	}
		
	public class HandleMessagesBehaviour extends OneShotBehaviour{
		
		public HandleMessagesBehaviour(Agent agent){
			super(agent);
		}
		
		@Override
		public void action() {
			CarAgent.this.sendAround(new ACLMessage(ACLMessage.INFORM));
		}
	}
	
}

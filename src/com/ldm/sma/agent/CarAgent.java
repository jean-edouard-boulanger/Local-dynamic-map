package com.ldm.sma.agent;

import java.beans.PropertyChangeSupport;

import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

import com.ldm.ui.CarUI;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

public class CarAgent extends GuiAgent {

	private PropertyChangeSupport propertyChangeCarAgent;
	CarUI carUI = null;
	
	public CarAgent(){
		super();
		propertyChangeCarAgent = new PropertyChangeSupport(this);
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
	}

	@Override
	protected void onGuiEvent(GuiEvent arg0) {

	}
	
}

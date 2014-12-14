package com.ldm.ui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.ldm.sma.agent.CarAgent;
import com.ldm.ui.gps.GpsUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CarUI extends Application implements PropertyChangeListener {

	private CarAgent carAgent;	
	private Stage PrimaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {		
		this.PrimaryStage = primaryStage;
		primaryStage.setTitle(this.carAgent.getLocalName());

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		primaryStage.setWidth(gd.getDisplayMode().getWidth() * 0.8);
		primaryStage.setHeight(gd.getDisplayMode().getHeight() * 0.8);
						
		primaryStage.setResizable(false);
		
		primaryStage.show();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
	}

	public static CarUI startUI(CarAgent carAgent, PropertyChangeSupport propertyChangeCarUI){
		try{
			CarUI application = CarUI.class.newInstance();
			application.setCarAgent(carAgent);
			propertyChangeCarUI.addPropertyChangeListener(application);
			application.start(new Stage());
			
			return application;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void setCarAgent(CarAgent carAgent){
		this.carAgent = carAgent;
	}
	
}

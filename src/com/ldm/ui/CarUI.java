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
		
		final Pane rootPane = new Pane();
		rootPane.setId("root");
		
		final Pane backgroundPane = new Pane();
		backgroundPane.setId("backgroundPane");
		backgroundPane.getChildren().add(rootPane);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		float width = gd.getDisplayMode().getWidth();
		float height = gd.getDisplayMode().getHeight();
		Scene scene = new Scene(backgroundPane, width * 0.8, height * 0.8);
						
		primaryStage.setScene(scene);
		primaryStage.setResizable(true);
		
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

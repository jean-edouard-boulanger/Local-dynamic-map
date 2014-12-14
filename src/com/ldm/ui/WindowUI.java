package com.ldm.ui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.ldm.data.structure.Pair;
import com.ldm.model.geometry.Position;
import com.ldm.sma.agent.CarAgent;
import com.ldm.ui.components.Car;
import com.ldm.ui.components.NavigationMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class WindowUI extends Application implements PropertyChangeListener {

	public enum carUIEventType{
		carMoved
	}
	
	private CarAgent carAgent;
	private Stage PrimaryStage;

	NavigationMap navigationMap;
	
	@Override
	public void start(Stage primaryStage) throws Exception {		
		this.PrimaryStage = primaryStage;
		primaryStage.setTitle(this.carAgent.getLocalName());
		primaryStage.setResizable(false);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		Group root = new Group();
		Scene scene = new Scene(root, gd.getDisplayMode().getWidth() * 0.8, gd.getDisplayMode().getHeight() * 0.8);
		
		double offset = 50.0;
		double mapHeight = scene.getHeight() - 2.0 * offset; 
		double mapWidth = scene.getWidth() - 2.0 * offset;
		
		navigationMap = new NavigationMap(this.carAgent.getGPS().getMap(), mapHeight, mapWidth);
		navigationMap.relocate(50, 50);
		
		root.getChildren().add(navigationMap);
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		/**
		 * Event fired when the car moves.
		 */
		if(evt.getPropertyName().equals(carUIEventType.carMoved.toString())){		
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					Position newPosition = (Position)evt.getNewValue();
								
					navigationMap.setCarPosition(newPosition);
				}
			});
		}
	}

	public static WindowUI startUI(CarAgent carAgent, PropertyChangeSupport propertyChangeCarUI){
		try{
			WindowUI application = WindowUI.class.newInstance();
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

package com.ldm.ui;

import jade.gui.GuiEvent;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;

import com.ldm.model.geometry.Position;
import com.ldm.sma.agent.CarAgent;
import com.ldm.ui.components.NavigationMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class WindowUI extends Application implements PropertyChangeListener {

	public enum carUIEventType{
		carMoved,
		itinerarySet,
		wayPointPassed,
		messageSent,
		intersectionClicked,
		messageReceived
	}
	
	private CarAgent carAgent;
	private Stage primaryStage;

	NavigationMap navigationMap;
	
	@Override
	public void start(Stage primaryStage) throws Exception {	
		this.primaryStage = primaryStage;
		primaryStage.setTitle(this.carAgent.getLocalName());
		primaryStage.setResizable(false);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		Pane root = new Pane();
		Scene scene = new Scene(root, gd.getDisplayMode().getWidth() * 0.60, gd.getDisplayMode().getHeight() * 0.60);
		
		final double offset = 20.0;
		final double mapHeight = scene.getHeight() - 2.0 * offset; 
		final double mapWidth = scene.getWidth() - 2.0 * offset;
		
		navigationMap = new NavigationMap(this.carAgent.getGPS().getMap(), mapHeight, mapWidth);
		navigationMap.relocate(offset, offset);
		
		navigationMap.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {
				if(event.getButton().equals(MouseButton.PRIMARY)){
					if(event.getClickCount() == 2){
						Integer intersection = navigationMap.getClickedIntersection(new Position(event.getSceneX() - offset, event.getSceneY() - offset));
						if(intersection != null){
							GuiEvent  ev = new GuiEvent(WindowUI.this, carUIEventType.intersectionClicked.ordinal());
							ev.addParameter(intersection);
							carAgent.postGuiEvent(ev);
						}
					}
				}
			}
		});
		
		root.getChildren().add(navigationMap);
				
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		
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
		else if(evt.getPropertyName().equals(carUIEventType.itinerarySet.toString())){
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					navigationMap.setItinerary((LinkedList<Integer>)evt.getNewValue());
				}
			});
		}
		else if(evt.getPropertyName().equals(carUIEventType.wayPointPassed.toString())){
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					navigationMap.popNextWayPoint();
				}
			});
		}
		else if(evt.getPropertyName().equals(carUIEventType.messageSent.toString())){
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					navigationMap.notifyMessageSent();
				}
			});
		}
		else if(evt.getPropertyName().equals(carUIEventType.messageReceived.toString())){
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					navigationMap.notifyMessageReceived((Position)evt.getNewValue());
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

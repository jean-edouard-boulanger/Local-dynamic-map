package com.ldm.ui;

import jade.gui.GuiEvent;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;
import java.util.LinkedList;
import java.util.Observable;

import com.ldm.model.geometry.Position;
import com.ldm.model.log.Log;
import com.ldm.sma.agent.CarAgent;
import com.ldm.ui.components.NavigationMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class WindowUI extends Application implements PropertyChangeListener {

	public enum carUIEventType{
		carMoved,
		itinerarySet,
		wayPointPassed,
		messageSent,
		intersectionClicked,
		explorationRequested,
		messageReceived
	}
	
	private CarAgent carAgent;
	private Stage primaryStage;

	private TableView logTableView;
	private final ObservableList<Log> logTableViewData = FXCollections.observableArrayList();
	
	NavigationMap navigationMap;
	
	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage primaryStage) throws Exception {	
		this.primaryStage = primaryStage;
		primaryStage.setTitle(this.carAgent.getLocalName());
		primaryStage.setResizable(false);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		Pane root = new Pane();
		Scene scene = new Scene(root, gd.getDisplayMode().getWidth() * 0.60, gd.getDisplayMode().getHeight() * 0.80);
		root.setStyle("-fx-background-color: white;");
		
		final double offset = 20.0;
		final double mapHeight = 0.7 * scene.getHeight();
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
							
							if(event.isShiftDown()){
								ev = new GuiEvent(WindowUI.this, carUIEventType.explorationRequested.ordinal());
								carAgent.postGuiEvent(ev);
							}
						}
					}
				}
			}
		});
		
		root.getChildren().add(navigationMap);
		
		final double logTableViewWidth = mapWidth;
		final double logTableViewHeight = scene.getHeight() - (4 * offset + mapHeight);
		
		logTableView = new TableView(logTableViewData);
		logTableView.setRowFactory(new Callback<TableView<Log>, TableRow<Log>>() {
	        @Override
	        public TableRow<Log> call(TableView<Log> tableView) {
	            final TableRow<Log> row = new TableRow<Log>() {
	                @Override
	                protected void updateItem(Log row, boolean empty) {
	                    super.updateItem(row, empty);
	                    if (!empty && row.getStyle() != null){
	                        styleProperty().set(row.getStyle());
	                    }
	                }
	            };
	            return row;
	        }
	    });
		
		TableColumn timestampColumn = new TableColumn("Timestamp");
		timestampColumn.setPrefWidth(0.20 * logTableViewWidth);
		timestampColumn.setCellValueFactory(new PropertyValueFactory<Log, String>("formattedLogDate"));
		
		TableColumn messageColumn = new TableColumn("Evenement");
		messageColumn.setPrefWidth(0.80 * logTableViewWidth);
		messageColumn.setCellValueFactory(new PropertyValueFactory<Log, String>("message"));

		
		logTableView.setEditable(false);		
		logTableView.relocate(offset, 3 * offset + mapHeight);
		logTableView.setPrefWidth(mapWidth);
		logTableView.setPrefHeight(logTableViewHeight);
		
		logTableView.getColumns().addAll(timestampColumn, messageColumn);
		
		root.getChildren().add(logTableView);
		
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
					LinkedList<Integer> itinerary = (LinkedList<Integer>)evt.getNewValue();
					WindowUI.this.log(new Log("Modification de l'itinéraire courant: " + itinerary));
					navigationMap.setItinerary(itinerary);
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
	
	private void log(Log log){
		this.logTableViewData.add(log);
	}
}

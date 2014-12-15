package com.ldm.ui.components;

import com.ldm.model.geometry.Position;

import javafx.animation.FillTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Car extends Group {
	
	private Circle carBody;
	
	Position currentPosition;
	
	public Car(){
		carBody = new Circle(0, 0, 4, Color.CRIMSON);
		this.getChildren().add(carBody);
	}
	
	public void setPosition(Position p){
		this.currentPosition = p;
		
		carBody.setCenterX(p.getX());
		carBody.setCenterY(p.getY());
	}
	
	public void notifyMessageSent(){
		FillTransition ft = new FillTransition(Duration.millis(300), this.carBody, Color.RED, Color.CYAN);
		ft.setCycleCount(2);
		ft.onFinishedProperty().set(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				Car.this.carBody.setFill(Color.RED);
			}
		});
		ft.play();
	}
	
	public Position getCurrentPosition(){
		return this.currentPosition;
	}
}

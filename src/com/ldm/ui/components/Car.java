package com.ldm.ui.components;

import com.ldm.model.geometry.Position;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Car extends Group {
	
	private Circle carBody;
	
	Position currentPosition;
	
	public Car(){
		carBody = new Circle(0, 0, 4, Color.RED);
		this.getChildren().add(carBody);
	}
	
	public void setPosition(Position p){
		this.currentPosition = p;
		
		carBody.setCenterX(p.getX());
		carBody.setCenterY(p.getY());
	}
	
	public Position getCurrentPosition(){
		return this.currentPosition;
	}
}

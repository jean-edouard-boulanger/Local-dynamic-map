package com.ldm.model;

import java.util.ArrayList;

import com.ldm.model.geometry.Position;

public class GPS {
	
	private RoadNetwork map = new RoadNetwork();	
	private Position currentPosition = new Position();
	
	private ArrayList<GPSObserver> observers = new ArrayList<>();
	
	public GPS(){}
	
	public GPS(RoadNetwork map){this.map = map;}
	
	public Position getCurrentPosition(){
		return this.currentPosition;
	}
	
	public void setCurrentPosition(Position currentPosition){
		this.currentPosition = currentPosition;
	}
	
	public RoadNetwork getMap(){
		return this.map;
	}
	
	public void setMap(RoadNetwork map){
		this.map = map;
	}
	
	public void subscribe(GPSObserver o){
		this.observers.add(o);
	}
	
}

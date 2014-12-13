package com.ldm.model;

import java.util.ArrayDeque;
import java.util.ArrayList;

import com.ldm.data.algorithm.DijkstraAlgorithm;
import com.ldm.model.geometry.Position;

public class GPS {
	
	private static final Double reachDistanceThreshold = 10.0;
	
	private RoadNetwork map = new RoadNetwork();	
	private Position currentPosition = new Position();
	
	private ArrayList<GPSObserver> observers = new ArrayList<>();
	
	private Integer lastIntersection = null;
	
	private ArrayDeque<Integer> itinerary = null;
	
	public GPS(){}
	
	public GPS(RoadNetwork map){this.map = map;}
	
	public RoadNetwork getMap(){
		return this.map;
	}
	
	public void setMap(RoadNetwork map){
		this.map = map;
	}
	
	public Position getCurrentPosition(){
		return this.currentPosition;
	}
	
	public void setCurrentPosition(Position currentPosition){
		this.currentPosition = currentPosition;
		
		int closestIntersection = this.FindClosestIntersection();
		if(this.getDistanceToIntersection(closestIntersection) < reachDistanceThreshold){
			
		}
		
	}
	
	public void setDestination(int destination){
		if(lastIntersection == null){
			lastIntersection = this.FindClosestIntersection();
		}
		this.itinerary = calculateItinerary(this.lastIntersection, destination);
	}
	
	public ArrayDeque<Integer> calculateItinerary(int i0, int i1){
		DijkstraAlgorithm algorithm = new DijkstraAlgorithm();
		return algorithm.compute(this.map, i0, i1);
	}
	
	public Double getDistanceToIntersection(int i0){
		return Position.evaluateDistance(this.getCurrentPosition(), map.getIntersectionPosition(i0));
	}
	
	public int FindClosestIntersection(){
		ArrayList<Integer> intersections = this.map.getIntersections();
		
		Double minSquareDistance = Double.MAX_VALUE;
		Integer closestIntersection = null;
		
		for(int i : intersections){
			Double cDistance = Position.evaluateSquareDistance(map.getIntersectionPosition(i), this.currentPosition);
			if(cDistance < minSquareDistance){
				closestIntersection = i;
				minSquareDistance = cDistance;
			}
		}
		return closestIntersection;
	}
	
	public void subscribe(GPSObserver o){
		this.observers.add(o);
	}
}

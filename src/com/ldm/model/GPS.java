package com.ldm.model;

import java.util.ArrayDeque;
import java.util.ArrayList;

import com.ldm.data.algorithm.DijkstraAlgorithm;
import com.ldm.model.geometry.Position;
import com.ldm.model.geometry.Vect;

public class GPS {
	
	private static final Double reachDistanceThreshold = 10.0;
	
	private RoadNetwork map = new RoadNetwork();	
	private Position currentPosition = new Position();
	
	private boolean navigationMode;
	
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
			if(this.lastIntersection != closestIntersection){
				this.notifyIntersectionPassed(closestIntersection);
			}
			
			if(!this.isNavigationModeOn()){return;}
			
			if(closestIntersection != itinerary.peek()){return;}
						
			this.itinerary.pop();
			
			if(this.itinerary.size() == 0){
				this.notifyDestinationReached();
				this.navigationMode = false;
			}
			else
			{
				this.notifyWayPointPassed();
			}
		}
		
		if(this.isNavigationModeOn()){
			this.notifyHeadingChanged(this.calculateHeading());
		}
	}
	
	
	/**
	 * Asks the GPS to switch to navigation mode
	 */
	private void startNavigation(){
		this.navigationMode = true;
	}
	
	/**
	 * Asks the GPS to stop navigation mode
	 */
	public void stopNavigation(){
		this.navigationMode = false;
	}
	
	/**
	 * Indicates whether the navigation mode is on or off
	 * @return
	 */
	public boolean isNavigationModeOn(){
		return this.navigationMode;
	}
	
	/**
	 * Set a new destination position and calculates an itinerary from the last passed intersection.
	 * Uses the closest intersection if this last intersection is not known.
	 * Starts the navigation mode.
	 * @param destination Destination intersection identifier
	 */
	public void setDestination(int destination){
		if(lastIntersection == null){
			lastIntersection = this.FindClosestIntersection();
		}
		
		this.itinerary = calculateItinerary(this.lastIntersection, destination);
		
		if(this.itinerary != null){
			this.startNavigation();
		}
	}
		
	/**
	 * Calculates an itinerary between two intersections. Uses the Dijkstra Algorithm to find the shortest path (Travelling time).
	 * @param i0 First intersection identifier
	 * @param i1 Second intersection identifier
	 * @return A queue of intersections (Including the departure and arrival intersections)
	 */
	private ArrayDeque<Integer> calculateItinerary(int i0, int i1){
		DijkstraAlgorithm algorithm = new DijkstraAlgorithm();
		return algorithm.compute(this.map, i0, i1);
	}
	
	/**
	 * Calculates the Cartesian distance between the current position and the given intersection
	 * @param i0 Intersection identifier
	 * @return The distance to the given intersection
	 */
	private Double getDistanceToIntersection(int i0){
		return Position.evaluateDistance(this.getCurrentPosition(), map.getIntersectionPosition(i0));
	}
	
	/**
	 * Calculates a unit vector pointing towards the next heading
	 * If the GPS is not in navigation mode, the returned vector is null
	 * @return A unit vector pointing towards the next heading
	 */
	private Vect calculateHeading(){
		if(!this.isNavigationModeOn()){return null;}
		Integer nextIntersection = this.itinerary.peek();
		if(nextIntersection == null){return null;}
		
		Position nextIntersectionPos = this.map.getIntersectionPosition(nextIntersection);
		Vect heading = new Vect(nextIntersectionPos.getX() - currentPosition.getX(), nextIntersectionPos.getY() - currentPosition.getY());
		return heading.normalize();
	}
	
	/**
	 * @return The closest intersection to the current position
	 */
	private int FindClosestIntersection(){
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
	
	/*
	 * GPSObserver
	 */
	
	/**
	 * Subscribe an observer to the GPS notifications
	 * @param o The observer
	 */
	public void subscribe(GPSObserver o){
		this.observers.add(o);
	}
	
	/**
	 * Notification sent to the observer after the destination is reached
	 */
	public void notifyDestinationReached(){
		for(GPSObserver o : this.observers){
			o.onDestinationReached();
		}
	}
	
	/**
	 * Notification sent to the observer after a way point (Intersection) is passed
	 */
	public void notifyWayPointPassed(){
		for(GPSObserver o : this.observers){
			o.onWayPointPassed();
		}
	}
	
	/**
	 * Notification sent to the observer when the heading changes
	 * @param newHeading
	 */
	public void notifyHeadingChanged(Vect newHeading){
		for(GPSObserver o : this.observers){
			o.onHeadingChanged(newHeading);
		}
	}
	
	/**
	 * Notification sent to the observer when an intersection is passed
	 * @param newHeading
	 */
	public void notifyIntersectionPassed(Integer intersection){
		for(GPSObserver o : this.observers){
			o.onIntersectionPassed(map.getIntersectionPosition(intersection));
		}
	}
}

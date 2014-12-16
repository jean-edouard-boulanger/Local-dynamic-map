package com.ldm.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import com.ldm.data.algorithm.DijkstraAlgorithm;
import com.ldm.model.geometry.Position;
import com.ldm.model.geometry.Vect;

public class GPS {
	
	private static final Double reachDistanceThreshold = 4.0;
	
	private RoadNetwork map = new RoadNetwork();	
	private Position currentPosition = new Position();
	
	private int currentMaximumSpeed = 5;
	
	private boolean navigationMode = false;
	
	private ArrayList<GPSObserver> observers = new ArrayList<>();
	
	private Integer lastIntersection = null;
	
	private LinkedList<Integer> itinerary = new LinkedList<>();
		
	DijkstraAlgorithm pathFindingAlgorithm = new DijkstraAlgorithm();
	
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
	
	public Integer getCurrentMaximumSpeed(){
		return this.currentMaximumSpeed;
	}
	
	public Double getCurrentRoadProgess(){
		if(this.itinerary == null){return null;}
		if(this.lastIntersection == null){return null;}
		if(this.itinerary.peek() == null){return null;}
		if(this.lastIntersection == this.itinerary.peek()){return 1.0;}
		
		Double totalDistance = this.map.getRoadDistance(this.lastIntersection, this.itinerary.getFirst());
		Double travelledDistance = Position.evaluateDistance(this.map.getIntersectionPosition(this.lastIntersection), this.currentPosition);
		
		return travelledDistance / totalDistance;
	}
	
	public Integer getCurrentRoad(){
		if(this.lastIntersection == null){return null;}
		if(this.itinerary == null){return null;}
		if(this.itinerary.peek() == null){return null;}
		return map.getRoad(this.lastIntersection, this.itinerary.peek());
	}
	
	public Integer getNextItineraryRoad(){
		if(!this.isNavigationModeOn()){return null;}
		if(this.itinerary.size() < 2){return null;}
		return map.getRoad(itinerary.get(0), itinerary.get(1));
	}
	
	synchronized public void setNeedsReplan(Integer road){
		if(!this.isNavigationModeOn()) return;
		if(this.itinerary == null) return;
		
		for(int i = 0; i < this.itinerary.size() - 1; i++){
			Integer r = this.map.getRoad(this.itinerary.get(i), this.itinerary.get(i+1));
			if(r != null && r == road){
				LinkedList<Integer> newItinerary = this.calculateItinerary(itinerary.getFirst(), itinerary.getLast());
				if(!newItinerary.equals(this.itinerary)){
					System.out.println("[DEBUG@GPS@setNeedsReplan] Current itinerary replanned: " + newItinerary + " was: " + this.itinerary);
					this.itinerary = newItinerary;
					this.notifyItineraryReplanned(newItinerary);
					return;
				}
			}
		}
	}
	
	synchronized public void setCurrentPosition(Position currentPosition){
		this.currentPosition = currentPosition;
		
		this.notifyPositionChanged(currentPosition);
						
		this.currentMaximumSpeed = this.map.getRoadSpeedLimit(this.getCurrentRoad(), this.getCurrentRoadProgess());
		
		int closestIntersection = this.FindClosestIntersection();				
		if(this.getDistanceToIntersection(closestIntersection) < reachDistanceThreshold){
			if(this.lastIntersection == null || this.lastIntersection != closestIntersection){
				this.lastIntersection = closestIntersection;				
				this.notifyIntersectionPassed(closestIntersection);
			}
			
			if(!this.isNavigationModeOn()){return;}
			
			if(closestIntersection != itinerary.peek()){return;}
			
			this.notifyWayPointPassed(this.itinerary.pop());
						
			Integer newRoad = this.getCurrentRoad();
			if(newRoad != null){
				this.notifyRoadChanged(newRoad);
			}
			
			if(this.itinerary.size() == 0){
				this.lastIntersection = null;
				this.notifyDestinationReached();
			}
			
			if(this.itinerary.size() == 0){
				this.navigationMode = false;
			}
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
	synchronized public void setDestination(int destination){
		Integer lastInter = this.lastIntersection;
		if(lastInter == null){
			lastInter = this.FindClosestIntersection();
		}
		
		Integer firstIntersection = lastInter;
		if(this.isNavigationModeOn() && this.itinerary != null && this.itinerary.peek() != null){
			firstIntersection = this.itinerary.peek();
			this.itinerary.clear();
		}				
		
		this.itinerary = calculateItinerary(firstIntersection, destination);
		if(this.itinerary != null){			
			this.notifyItinerarySet(itinerary);
			this.startNavigation();
		}
	}
		
	/**
	 * Calculates an itinerary between two intersections. Uses the Dijkstra Algorithm to find the shortest path (Travelling time).
	 * @param i0 First intersection identifier
	 * @param i1 Second intersection identifier
	 * @return A queue of intersections (Including the departure and arrival intersections)
	 */
	private LinkedList<Integer> calculateItinerary(int i0, int i1){
		return pathFindingAlgorithm.compute(this.map, i0, i1);
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
	 * @return The distance between the current position and the next way point (Intersection)
	 */
	public Double getDistanceToNextWayPoint(){
		if(!this.isNavigationModeOn()){return null;}
		Integer nextIntersection = this.itinerary.peek();
		if(nextIntersection == null){return null;}
		
		return Position.evaluateDistance(this.getCurrentPosition(), this.map.getIntersectionPosition(nextIntersection));
	}
	
	/**
	 * Calculates a unit vector pointing towards the next heading
	 * If the GPS is not in navigation mode, the returned vector is null
	 * @return A unit vector pointing towards the next heading
	 */
	public Vect getHeading(){
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
	
	/**
	 * @return The position of a random intersection in the map
	 */
	public Position getRandomIntersectionPosition(){
		return new Position(this.map.getIntersectionPosition(this.getRandomIntersection()));
	}

	/**
	 * @return The identifier of a random intersection in the map
	 */
	public int getRandomIntersection(){
		ArrayList<Integer> intersections = this.map.getIntersections();
		Random r = new Random();
		return intersections.get(r.nextInt(intersections.size()));
	}
	
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
		System.out.println("[DEBUG@GPS@setCurrentPosition] notifyDestinationReached");
		for(GPSObserver o : this.observers){
			o.onDestinationReached();
		}
	}
	
	/**
	 * Notification sent to the observer when an intersection is passed
	 */
	public void notifyIntersectionPassed(int intersection){
		System.out.println("[DEBUG@GPS@setCurrentPosition] notifyIntersectionPassed: " + intersection);
		for(GPSObserver o : this.observers){
			o.onIntersectionPassed(new Position(map.getIntersectionPosition(intersection)));
		}
	}
	
	public void notifyPositionChanged(Position newPosition){
		for(GPSObserver o : this.observers){
			o.onPositionChanged(new Position(newPosition));
		}
	}
	
	public void notifyWayPointPassed(Integer intersection){
		for(GPSObserver o : this.observers){
			o.onWayPointPassed(intersection);
		}
	}
	
	public void notifyRoadChanged(Integer road){
		System.out.println("[DEBUG@GPS@setCurrentPosition] notifyRoadChanged: " + road);
		for(GPSObserver o : this.observers){
			o.onRoadChanged(road);
		}
	}
	
	public void notifyItinerarySet(LinkedList<Integer> itinerary){
		System.out.println("[DEBUG@GPS@setCurrentPosition] notifyItinerarySet: " + itinerary);
		for(GPSObserver o : this.observers){
			o.onItinerarySet(new LinkedList<Integer>(itinerary));
		}
	}
	
	public void notifyItineraryReplanned(LinkedList<Integer> itinerary){
		System.out.println("[DEBUG@GPS@setCurrentPosition] notifyItinerarySet: " + itinerary);
		for(GPSObserver o : this.observers){
			o.onItineraryReplanned(new LinkedList<Integer>(itinerary));
		}
	}
	
}

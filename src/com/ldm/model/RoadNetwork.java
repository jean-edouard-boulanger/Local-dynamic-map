package com.ldm.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.ldm.model.geometry.Position;

import grph.in_memory.InMemoryGrph;

public class RoadNetwork extends InMemoryGrph {

	HashMap<Integer, Double> travelTimes = new HashMap<>();
	HashMap<Integer, Position> positions = new HashMap<>();
	HashMap<Integer, Integer> speedLimits = new HashMap<>();
	
	
	public void addRoad(int r, int i0, int i1, int speedLimit){
		if(!this.containsVertex(i0) || !this.containsVertex(i1)){
			System.out.println("[WARNING@RoadNetwork] Can't add road " + r + ": missing intersection " + i0 + " or " + i1);
			return;
		}
		
		this.addDirectedSimpleEdge(i0, r, i1);
		this.setRoadSpeedLimit(r, speedLimit);
		
		this.setRoadTravelTime(r, Position.evaluateDistance(getIntersectionPosition(i0), getIntersectionPosition(i1)) / speedLimit);
	}
	
	public void addBidirectionalRoad(int r1, int r2, int i0, int i1, int speedLimit){
		this.addRoad(r1, i0, i1, speedLimit);
		this.addRoad(r2, i1, i0, speedLimit);
	}
	
	public ArrayList<Integer> getRoads(){
		return this.getEdges().toIntegerArrayList();
	}
	
	public void addIntersection(int i, Position p){
		this.addVertex(i);
		this.positions.put(i, p);
	}
		
	public ArrayList<Integer> getIntersections(){
		return this.getVertices().toIntegerArrayList();
	}
	
	public void setRoadTravelTime(int r, double travelTime){
		if(!this.containsEdge(r)){return;}
		this.travelTimes.put(r, travelTime);	
	}
	
	public Double getRoadTravelTime(int r){
		if(!this.containsEdge(r)){return null;}
		return this.travelTimes.get(r);		
	}
	
	public void setIntersectionPosition(int v, Position p){
		if(!this.containsVertex(v)){return;}
		this.positions.put(v, p);
	}
	
	public Position getIntersectionPosition(int v){
		if(!this.containsVertex(v)){return null;}
		return this.positions.get(v);
	}
	
	public void setRoadSpeedLimit(int e, int speedLimit){
		if(!this.containsEdge(e)){return;}
		this.speedLimits.put(e, speedLimit);
	}
	
	public Integer getRoadSpeedLimit(int e){
		if(!this.containsEdge(e)){return null;}
		return this.speedLimits.get(e);
	}
}

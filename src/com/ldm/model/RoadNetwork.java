package com.ldm.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.ldm.data.structure.Pair;
import com.ldm.model.geometry.Position;

import grph.in_memory.InMemoryGrph;

public class RoadNetwork extends InMemoryGrph {

	HashMap<Integer, Double> travelTimes = new HashMap<>();
	HashMap<Integer, Position> positions = new HashMap<>();
	HashMap<Integer, Integer> speedLimits = new HashMap<>();
	
	public void addRoad(int i0, int i1, int speedLimit){
		if(!this.containsVertex(i0) || !this.containsVertex(i1)){
			System.out.println("[WARNING@RoadNetwork] Can't add road, missing intersection " + i0 + " or " + i1);
			return;
		}
		
		int roadId = this.addDirectedSimpleEdge(i0, i1);
		this.setRoadSpeedLimit(roadId, speedLimit);
		
		this.setRoadTravelTime(roadId, Position.evaluateDistance(getIntersectionPosition(i0), getIntersectionPosition(i1)) / speedLimit);
	}
	
	public void addBidirectionalRoad(int i0, int i1, int speedLimit){
		this.addRoad(i0, i1, speedLimit);
		this.addRoad(i1, i0, speedLimit);
	}
	
	public ArrayList<Integer> getRoads(){
		return this.getEdges().toIntegerArrayList();
	}
	
	public ArrayList<Integer> getOutNeighborIntersections(int i){
		if(!this.hasIntersection(i)){return null;}
		return this.getOutNeighbors(i).toIntegerArrayList();
	}
	
	public void addIntersection(int i, Position p){
		this.addVertex(i);
		this.positions.put(i, p);
	}
		
	public ArrayList<Integer> getIntersections(){
		return this.getVertices().toIntegerArrayList();
	}
	
	public boolean hasIntersection(int i){
		return this.containsVertex(i);
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
	
	public Integer getRoad(int i0, int i1){
		if(!this.hasRoad(i0, i1)){return null;}
		int[] edges = this.getEdgesConnecting(i0, i1).toIntArray();
		if(edges.length == 0){return null;}
		return edges[0]; 
	}
	
	public boolean hasRoad(int i0, int i1){
		return this.getEdgesConnecting(i0, i1).size() > 0;
	}
	
	public Pair<Position, Position> getExtremePositions(){
		Position lowest = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
		Position highest = new Position(Double.MIN_VALUE, Double.MIN_VALUE);
		
		ArrayList<Integer> intersections = this.getIntersections();
		for(int i : intersections){
			Position intersectionPosition = this.getIntersectionPosition(i);
			if(intersectionPosition.getX() < lowest.getX()){lowest.setX(intersectionPosition.getX());}
			if(intersectionPosition.getY() < lowest.getY()){lowest.setY(intersectionPosition.getY());}
			
			if(intersectionPosition.getX() > highest.getX()){highest.setX(intersectionPosition.getX());}
			if(intersectionPosition.getY() > highest.getY()){highest.setY(intersectionPosition.getY());}
		}
		return new Pair<Position, Position>(lowest, highest);
	}
}

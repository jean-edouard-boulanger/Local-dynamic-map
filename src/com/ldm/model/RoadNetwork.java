package com.ldm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ldm.data.structure.Pair;
import com.ldm.model.geometry.Position;
import com.ldm.model.helper.UnitHelper;

import grph.in_memory.InMemoryGrph;

public class RoadNetwork extends InMemoryGrph {

	private final double defaultProximityThreshold = 1000.0;
	
	HashMap<Integer, Double> travelTimes = new HashMap<>();
	HashMap<Integer, Expirable<Double>> unoficialTravelTimes = new HashMap<>();
	
	HashMap<Integer, Position> positions = new HashMap<>();
	HashMap<Integer, Integer> speedLimits = new HashMap<>();
	HashMap<Integer, Disruption> roadDisruptions = new HashMap<>();
	
	public void addRoad(int i0, int i1, int speedLimit){
		if(!this.containsVertex(i0) || !this.containsVertex(i1)){
			System.out.println("[WARNING@RoadNetwork] Can't add road, missing intersection " + i0 + " or " + i1);
			return;
		}
		
		int roadId = this.addDirectedSimpleEdge(i0, i1);
		this.setRoadSpeedLimit(roadId, speedLimit);
		
		this.setRoadTravelTime(roadId, Position.evaluateDistance(getIntersectionPosition(i0), getIntersectionPosition(i1)) / UnitHelper.kmhTOms(speedLimit));
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
	
	public void setRoadTravelTime(Integer r, double travelTime){
		if(r == null){return;}
		if(!this.containsEdge(r)){return;}
		this.travelTimes.put(r, travelTime);	
	}
	
	public Double getRoadTravelTime(Integer r){
		if(r == null){return null;}
		if(!this.containsEdge(r)){return null;}
		
		Double travelTime = this.getRoadUnoficialTravelTime(r);
		if(travelTime != null){
			return travelTime;
		}
		
		return this.travelTimes.get(r);		
	}
	
	public void setRoadUnoficialTravelTime(Integer r, double unoficialTravelTime, Date expireDate){
		if(!this.hasRoad(r)){return;}
		
		Double travelTime = this.getRoadTravelTime(r);
		if(unoficialTravelTime > travelTime){
			System.out.println("[DEBUG@RoadNetwork@setRoadUnoficialTravelTime] Unoficial travel time set for road " + r + " because greater ("+ unoficialTravelTime +" > "+ travelTime +")");
			this.unoficialTravelTimes.put(r, new Expirable<Double>(unoficialTravelTime, expireDate));
		}
	}
	
	public Double getRoadUnoficialTravelTime(Integer r){
		if(!this.hasRoad(r)){return null;}
		Expirable<Double> t = this.unoficialTravelTimes.get(r);
		if(t == null){return null;}
		
		return t.getData();
	}
	
	public Double getRoadDistance(int i0, int i1){
		if(!this.hasRoad(i0, i1)){return null;}
		return Position.evaluateDistance(this.getIntersectionPosition(i0), this.getIntersectionPosition(i1));
	}
	
	public Pair<Integer, Integer> getRoadIntersections(Integer r){
		if(r == null){return null;}
		if(!this.hasRoad(r)){return null;}
		
		ArrayList<Integer> e = this.getVerticesIncidentToEdge(r).toIntegerArrayList();
		if(e.get(0) == null || e.get(1) == null)
			return null;
		
		if(this.hasRoad(e.get(0), e.get(1)))
			return new Pair<Integer, Integer>(e.get(0), e.get(1));
		
		return new Pair<Integer, Integer>(e.get(1), e.get(0));
	}
	
	public void setIntersectionPosition(int v, Position p){
		if(!this.containsVertex(v)){return;}
		this.positions.put(v, p);
	}
	
	public Position getIntersectionPosition(int v){
		if(!this.containsVertex(v)){return null;}
		return new Position(this.positions.get(v));
	}
	
	public void setRoadSpeedLimit(int e, int speedLimit){
		if(!this.containsEdge(e)){return;}
		this.speedLimits.put(e, speedLimit);
	}
	
	public Integer getRoadSpeedLimit(Integer r){
		if(r == null){return 5;}
		if(!this.containsEdge(r)){return 5;}
		return this.speedLimits.get(r);
	}
	
	public Integer getRoadSpeedLimit(Integer r, Double progress){
		Integer usualSpeedLimit = this.getRoadSpeedLimit(r);
		if(usualSpeedLimit == null){return null;}
		if(progress == null){return usualSpeedLimit;}
		
		Disruption drp = this.roadDisruptions.get(r);
		if(drp == null){return usualSpeedLimit;}
		
		if(progress >= drp.getStartsAt() && progress < drp.getEndsAt()){
			Double ms = usualSpeedLimit.doubleValue();
			ms = ms - drp.getDisruptionLevel() * ms;
			return ms.intValue();
		}
		return usualSpeedLimit;
	}
	
	public boolean isRoadBothDirection(int i0, int i1){
		return this.hasRoad(i1, i0);
	}
	
	public void addRoadDisruption(int i0, int i1, Disruption d){
		Integer r = this.getRoad(i0, i1);
		this.addRoadDisruption(r, d);
	}
	
	public void addRoadDisruption(Integer r, Disruption d){
		if(r == null){return;}
		if(!this.hasRoad(r)){return;}
		this.roadDisruptions.put(r, d);
	}
	
	public boolean hasDisruption(Integer r){
		if(r == null){return false;}
		if(!this.hasRoad(r)){return false;}
		return this.roadDisruptions.containsKey(r);
	}
	
	public Disruption getRoadDisruption(int i0, int i1){
		Integer road = this.getRoad(i0, i1);
		return this.getRoadDisruption(road);
	}
	
	public Disruption getRoadDisruption(Integer r){
		if(r == null){return null;}
		if(!this.hasRoad(r)){return null;}
		return this.roadDisruptions.get(r);
	}
		
	public Integer getRoad(int i0, int i1){
		if(!this.hasRoad(i0, i1)){return null;}
		int[] edges = this.getEdgesConnecting(i0, i1).toIntArray();
		if(edges.length == 0){return null;}
		return edges[0]; 
	}
	
	public boolean hasRoad(int r){
		return this.containsEdge(r);
	}
	
	public boolean hasRoad(int i0, int i1){
		return this.getEdgesConnecting(i0, i1).size() > 0;
	}
	
	public ArrayList<Integer> getAllIntersectionsNear(Position p, double distanceLimit){
		ArrayList<Integer> intersections = this.getIntersections();
		ArrayList<Integer> closeIntersections = new ArrayList<Integer>();
		
		for(Integer inter : intersections){
			if(Position.evaluateDistance(p, this.getIntersectionPosition(inter)) < distanceLimit){
				closeIntersections.add(inter);
			}
		}
		return closeIntersections;
	}
	
	public ArrayList<Integer> getAllIntersectionsNear(Position p){
		return this.getAllIntersectionsNear(p, defaultProximityThreshold);
	}
	
	public ArrayList<Integer> getAllRoadsNear(Position p, double distanceLimit){
		ArrayList<Integer> nearIntersections = this.getAllIntersectionsNear(p, distanceLimit);
		ArrayList<Integer> closeRoads = new ArrayList<Integer>();
		
		Integer road;
		for(Integer ni0 : nearIntersections){
			for(Integer ni1 : nearIntersections){
				road = this.getRoad(ni0, ni1);
				if(road != null) closeRoads.add(road);
			}
		}
		return closeRoads;
	}

	public ArrayList<Integer> getAllNearRoads(Position p){
		return this.getAllRoadsNear(p, defaultProximityThreshold);
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

package com.ldm.data.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ldm.data.structure.Pair;
import com.ldm.model.RoadNetwork;

public class DijkstraAlgorithm {

	public List<Integer> compute(RoadNetwork g, Integer vd, Integer va) {
		List<Integer> path = new LinkedList<>();
		
		if(!g.containsAPath(vd, va)){
			return null;
		}
		
		List<Integer> unvisitedIntersections = g.getIntersections();
		List<Integer> visitedIntersections = new ArrayList<>();
		
		HashMap<Integer, Pair<Integer, Double>> shortestPaths = new HashMap<>();
		for(int inter : unvisitedIntersections){
			shortestPaths.put(inter, new Pair<Integer, Double>(null, Double.MAX_VALUE));
		}
		shortestPaths.get(vd).second = 0.0;
		
		Integer currentVertex = vd;		
		while(!unvisitedIntersections.isEmpty() && currentVertex != null){
						
			unvisitedIntersections.remove(currentVertex);
			visitedIntersections.add(currentVertex);
			
			/*
			System.out.println("Current vertex: " + currentVertex);
			System.out.println("Unvisited: " + unvisitedIntersections);
			System.out.println("Visited: " + visitedIntersections);
			*/
			
			ArrayList<Integer> outNeighbors = g.getOutNeighbors(currentVertex).toIntegerArrayList();
			
			Integer procEdge = null;
			for(int procVertex : outNeighbors){
				
				if(visitedIntersections.contains(procVertex)){
					continue;
				}
								
				procEdge = g.getEdgesConnecting(currentVertex, procVertex).toIntArrayList().get(0);
				double weight = shortestPaths.get(currentVertex).second + g.getRoadTravelTime(procEdge);
								
				if(weight < shortestPaths.get(procVertex).second){
					shortestPaths.get(procVertex).second = weight;
					shortestPaths.get(procVertex).first = currentVertex;
				}
			}
			
			/*
			for(Map.Entry<Integer, Pair<Integer, Double>> shortestPath : shortestPaths.entrySet() ){
				String distanceStr = (shortestPath.getValue().second == Double.MAX_VALUE) ? "oo" : shortestPath.getValue().second.toString();
				String predStr = (shortestPath.getValue().first == null) ? "-" : shortestPath.getValue().first.toString();
				System.out.println(shortestPath.getKey().toString() + " : " + predStr + "     " + distanceStr);
			}
			*/
			
			double minWeight = Double.MAX_VALUE;
			Integer closestVertex = null;
			for(int procVertex : unvisitedIntersections){
				if(shortestPaths.get(procVertex).second < minWeight){
					closestVertex = procVertex;
					minWeight = shortestPaths.get(procVertex).second;
				}
			}
			
			currentVertex = closestVertex;
		}
		
		Integer currentNode = va;
		while(currentNode != vd){
			path.add(currentNode);
			currentNode = shortestPaths.get(currentNode).first;
		}
		path.add(vd);
		
		Collections.reverse(path);
		
		return path;
	}

}

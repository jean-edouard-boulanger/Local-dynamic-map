package com.ldm.data.algorithm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.ldm.data.structure.RoadNetwork;

public class DijkstraAlgorithm {

	public List<Integer> compute(RoadNetwork g, int vd, int va) {
		List<Integer> path = new LinkedList<>();
		
		List<Integer> unvisitedIntersections = g.getIntersections();
		List<Integer> visitedIntersections = new ArrayList<>();
		
		return path;
	}

}

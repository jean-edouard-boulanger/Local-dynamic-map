package com.ldm.test;

import java.io.File;
import java.io.IOException;

import grph.in_memory.InMemoryGrph;

import com.ldm.data.algorithm.DijkstraAlgorithm;
import com.ldm.model.RoadNetwork;
import com.ldm.model.factory.RoadNetworkFactory;
import com.ldm.model.geometry.Position;

public class Dijkstra {

	public static void main(String[] args) {
		
		RoadNetwork rn = null;
		try {
			rn = RoadNetworkFactory.BuildFromFile(new File("gps.map"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(Integer i : rn.getRoads()){
			System.out.println(i + " : " + rn.getRoadIntersections(i));
		}
	}
}

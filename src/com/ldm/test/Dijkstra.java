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
		
		/*rn.addIntersection(1, new Position(0.0, 50.0));
		rn.addIntersection(2, new Position(100.0, 0.0));
		rn.addIntersection(3, new Position(80.0, 100.0));
		rn.addIntersection(4, new Position(140.0, 45.0));
		rn.addIntersection(5, new Position(180.0, 0.0));
		rn.addIntersection(6, new Position(180.0, 120.0));
		rn.addIntersection(7, new Position(230.0, 55.0));

		rn.addBidirectionalRoad(1, 2, 50);
		rn.addBidirectionalRoad(1, 3, 50);
		rn.addBidirectionalRoad(1, 4, 50);
		rn.addBidirectionalRoad(2, 4, 50);
		rn.addBidirectionalRoad(2, 5, 50);
		rn.addBidirectionalRoad(3, 4, 50);
		rn.addBidirectionalRoad(3, 6, 5);
		rn.addBidirectionalRoad(4, 5, 50);
		rn.addBidirectionalRoad(4, 7, 50);
		rn.addBidirectionalRoad(4, 6, 50);
		rn.addBidirectionalRoad(5, 7, 50);
		rn.addBidirectionalRoad(6, 7, 50);*/
				
		DijkstraAlgorithm da = new DijkstraAlgorithm();
		System.out.println(da.compute(rn, 1, 7));
	}
}

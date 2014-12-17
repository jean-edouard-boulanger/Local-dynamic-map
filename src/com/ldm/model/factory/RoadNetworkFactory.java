package com.ldm.model.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.ldm.model.Disruption;
import com.ldm.model.RoadNetwork;
import com.ldm.model.geometry.Position;

public class RoadNetworkFactory {
	
	public static RoadNetwork BuildFromFile(File f) throws IOException{
		RoadNetwork map = new RoadNetwork();
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		
		ImportMode mode = null;
		
		while((line = br.readLine()) != null){
			
			if(line.charAt(0) == '#'){continue;}
			
			if(line.equals("@ROAD@")){
				mode = ImportMode.road;
				continue;
			}
			else if(line.equals("@INTERSECTION@")){
				mode = ImportMode.intersection;
				continue;
			}
			else if(line.equals("@DISRUPTION@")){
				mode = ImportMode.disruption;
				continue;
			}
			
			String[] attrs = line.split(":");
			
			if(mode == ImportMode.road){
				Integer id0 = Integer.parseInt(attrs[0]);
				Integer id1 = Integer.parseInt(attrs[1]);
				Integer speedLimit = Integer.parseInt(attrs[2]);
				
				map.addRoad(id0, id1, speedLimit);
				if(attrs.length >= 4 && attrs[3].equals("b")){
					map.addRoad(id1, id0, speedLimit);
				}
			}
			else if(mode == ImportMode.intersection){
				Integer id = Integer.parseInt(attrs[0]);
				Position position = new Position(Float.parseFloat(attrs[1]), Float.parseFloat(attrs[2]));
				
				map.addIntersection(id, position);
			}
			else if(mode == ImportMode.disruption){
				Integer id0 = Integer.parseInt(attrs[0]);
				Integer id1 = Integer.parseInt(attrs[1]);
				Double startsAt = Double.parseDouble(attrs[2]);
				Double endsAt = Double.parseDouble(attrs[3]);
				Double disruptionLevel = Double.parseDouble(attrs[4]);
				
				map.addRoadDisruption(id0, id1, new Disruption(startsAt, endsAt, disruptionLevel));
				if(attrs.length >= 6 && attrs[5].equals("b")){
					map.addRoadDisruption(id0, id1, new Disruption(endsAt, startsAt, disruptionLevel));
				}
			}
		}
		br.close();
		
		return map;
	}
	
}

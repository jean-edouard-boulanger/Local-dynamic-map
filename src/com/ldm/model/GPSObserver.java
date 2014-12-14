package com.ldm.model;

import java.util.ArrayDeque;

import com.ldm.model.geometry.Position;
import com.ldm.model.geometry.Vect;

public interface GPSObserver {
	
	public void onPositionChanged(Position newPosition);
	
	public void onIntersectionPassed(Position intersectionPosition);
	
	public void onDestinationReached();
	
	public void onItinerarySet(ArrayDeque<Integer> itinerary);
	
	public void onWayPointPassed(Integer intersection);
	
	public void onRoadChanged(Integer road);
	
}
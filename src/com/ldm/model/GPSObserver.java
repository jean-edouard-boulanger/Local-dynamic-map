package com.ldm.model;

import java.util.LinkedList;
import com.ldm.model.geometry.Position;

public interface GPSObserver {
	
	public void onPositionChanged(Position newPosition);
	
	public void onIntersectionPassed(Position intersectionPosition);
	
	public void onDestinationReached();
	
	public void onItinerarySet(LinkedList<Integer> itinerary);
	
	public void onItineraryReplanned(LinkedList<Integer> itinerary);
	
	public void onNavigationStop();
	
	public void onWayPointPassed(Integer intersection);
	
	public void onRoadChanged(Integer road);
	
}
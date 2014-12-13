package com.ldm.model;

import com.ldm.model.geometry.Position;
import com.ldm.model.geometry.Vect;

public interface GPSObserver {
	
	public void onIntersectionPassed(Position intersectionPosition);
	
	public void onWayPointPassed();
	
	public void onHeadingChanged(Vect newHeading);
	
	public void onDestinationReached();
	
}
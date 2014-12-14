package com.ldm.model;

import com.ldm.model.geometry.Position;
import com.ldm.model.geometry.Vect;

public interface GPSObserver {
	
	public void onPositionChanged(Position newPosition);
	
	public void onIntersectionPassed(Position intersectionPosition);
	
	public void onDestinationReached();
	
}
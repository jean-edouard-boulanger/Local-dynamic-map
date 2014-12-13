package com.ldm.model;

import com.ldm.model.geometry.Position;

public interface GPSObserver {
	public void onIntersectionPassed(Position intersectionPosition);
	public void onDestinationReached();
}
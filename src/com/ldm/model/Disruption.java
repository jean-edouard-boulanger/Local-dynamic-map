package com.ldm.model;

public class Disruption {

	private double startsAt = 0.0;
	private double endsAt = 0.0;
	private double disruptionLevel = 0.0;
	
	public Disruption(){}
	
	public Disruption(double startsAt, double endsAt, double disruptionLevel){
		this.startsAt = enRange(startsAt);
		this.endsAt = enRange(endsAt);
		this.disruptionLevel = enRange(disruptionLevel);
	}
	
	public double enRange(double value){
		if(value > 1.0){return 1.0;}
		if(value < 0.0){return 0.0;}
		return value;
	}

	public double getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(double startsAt) {
		this.startsAt = enRange(startsAt);
	}

	public double getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(double endsAt) {
		this.endsAt = enRange(endsAt);
	}

	public double getDisruptionLevel() {
		return disruptionLevel;
	}

	public void setDisruptionLevel(double disruptionLevel) {
		this.disruptionLevel = enRange(disruptionLevel);
	}
	
}

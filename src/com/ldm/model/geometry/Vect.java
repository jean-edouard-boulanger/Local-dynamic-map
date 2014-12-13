package com.ldm.model.geometry;

public class Vect {
	public double dx;
	public double dy;
	
	public Vect(){}
	
	public Vect(double dx, double dy){
		this.dx = dx;
		this.dy = dy;
	}
	
	public Vect normalize(){
		double norm = Math.sqrt(dx * dx + dy * dy);
		this.dx /= norm;
		this.dy /= norm;
		return this;
	}
	
	public String toString(){
		return "("+ dx +","+ dy +")";
	}
}

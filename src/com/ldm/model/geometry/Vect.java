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
		if(norm != 0){
			this.dx /= norm;
			this.dy /= norm;	
		}
		return this;
	}
	
	public Vect mult(double factor){
		this.dx *= factor;
		this.dy *= factor;
		return this;
	}
	
	public Vect getMultipliedBy(double factor){
		return new Vect(this.dx * factor, this.dy * factor);
	}
	
	public String toString(){
		return "("+ dx +","+ dy +")";
	}
}

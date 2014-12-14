package com.ldm.model.geometry;

public class Position {
	private double x = 0.0;
	private double y = 0.0;
	
	public Position(){}
	
	public Position(double x, double y){
		this.x = x;
		this.y = y;
	}

	public Position(Position other){
		this(other.x, other.y);
	}
	
	public double getX() {return x;}

	public void setX(double x) {this.x = x;}

	public double getY() {return y;}

	public void setY(double y) {this.y = y;}
	
	public Position add(Vect v){
		this.x += v.dx;
		this.y += v.dy;
		return this;
	}
	
	public Position getAddedTo(Vect u){
		return new Position(this.x + u.dx, this.y + u.dy);
	}
	
	public static Double evaluateSquareDistance(Position p1, Position p2){
		if(p1 == null || p2 == null){return null;}
		
		return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
	}
	
	public static Double evaluateDistance(Position p1, Position p2){
		Double d = evaluateSquareDistance(p1, p2);
		if(d == null){return null;}
		return Math.sqrt(d);
	}
	
	public String toString(){
		return "("+ x +","+ y +")";
	}
	
}
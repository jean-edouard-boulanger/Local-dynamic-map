package com.ldm.ui.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ldm.data.structure.Pair;
import com.ldm.model.RoadNetwork;
import com.ldm.model.geometry.Position;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class NavigationMap extends Group {

	final RoadNetwork map;
	Map<Integer, Line> roads;
	Map<Integer, Circle> intersections;
	Car car;
	
	private double height;
	private double width;
	
	private double scaleX;
	private double scaleY;
	
	public NavigationMap(RoadNetwork map, double height, double width){
		this.map = map;
		
		this.height = height;
		this.width = width;
		
		Pair<Position, Position> extremePositions = this.map.getExtremePositions();
		this.scaleX = this.width / extremePositions.second.getX();
		this.scaleY = this.height / extremePositions.second.getY();	
		
		this.car = new Car();
		this.roads = new HashMap<>();
		this.intersections = new HashMap<>();
		
		this.draw();
	}
	
	/**
	 * @return The height of the navigation map
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @return The width of the navigation map
	 */
	public double getWidth() {
		return width;
	}
	
	/**
	 * Sets the position of the car in the navigation map. The coordinates are automatically scaled to the new frame.
	 * @param p
	 */
	public void setCarPosition(Position p){
		this.car.setPosition(this.getScaledPosition(p));
	}
	
	/**
	 * Initially draws the complete map
	 */
	private void draw(){
		
		ArrayList<Integer> inters = map.getIntersections();
		for(int inter : inters){
			Position interPos = getScaledPosition(map.getIntersectionPosition(inter));
			
			Circle interCircle = new Circle(interPos.getX(), interPos.getY(), 7);
			interCircle.setFill(Color.LIGHTGREY);
			this.getChildren().add(interCircle);
			
			this.intersections.put(inter, interCircle);
			
			ArrayList<Integer> neighbors = this.map.getOutNeighborIntersections(inter);
			
			for(int neighbor : neighbors){
				
				Position neighborPos = getScaledPosition(this.map.getIntersectionPosition(neighbor));
				
				Line roadLine = new Line(interPos.getX(), interPos.getY(), neighborPos.getX(), neighborPos.getY());
				roadLine.setStrokeWidth(10);
				roadLine.setStroke(Color.LIGHTGREY);
				
				this.getChildren().add(roadLine);
			}
			
		}
		
		this.getChildren().add(this.car);
		this.car.toFront();
	}
	
	/**
	 * @param p The old position
	 * @return The scaled position
	 */
	public Position getScaledPosition(Position p){
		return new Position(p.getX() * this.scaleX, p.getY() * this.scaleY);
	}
}

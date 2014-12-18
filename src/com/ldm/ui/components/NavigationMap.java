package com.ldm.ui.components;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.ldm.data.structure.Pair;
import com.ldm.model.Disruption;
import com.ldm.model.RoadNetwork;
import com.ldm.model.geometry.Position;
import com.ldm.model.geometry.Vect;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

public class NavigationMap extends Group {

	final RoadNetwork map;
	
	Map<Integer, Line> roads;
	Map<Integer, Circle> intersections;
	Map<Integer, Line> disruptions;
	
	Rectangle background;
	
	LinkedList<Line> itinerary;
	
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
		this.itinerary = new LinkedList<Line>();
		this.disruptions = new HashMap<>();
		
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
		Position newPosition = this.getScaledPosition(p);
		this.car.setPosition(newPosition);
		
		if(!this.itinerary.isEmpty()){
			this.itinerary.peek().setStartX(newPosition.getX());
			this.itinerary.peek().setStartY(newPosition.getY());
		}
		
		this.car.toFront();
	}
	
	public void clearItinerary(){
		for(Line l : this.itinerary){
			this.getChildren().remove(l);
		}
		this.itinerary.clear();
	}
	
	public void setItinerary(LinkedList<Integer> itinerary){
				
		this.clearItinerary();
		
		int currentIntersection = itinerary.pop();
		Position intPos = this.getScaledPosition(map.getIntersectionPosition(currentIntersection));
		
		Line firstLine = new Line(car.getCurrentPosition().getX(), car.getCurrentPosition().getY(), intPos.getX(), intPos.getY());
		firstLine.setStroke(Color.BLUEVIOLET);
		firstLine.setStrokeWidth(3);
		this.itinerary.addLast(firstLine);
		this.getChildren().add(firstLine);
		
		while(!itinerary.isEmpty()){
			int nextIntersection = itinerary.pop();
			
			Position i0pos = this.getScaledPosition(map.getIntersectionPosition(currentIntersection));
			Position i1pos = this.getScaledPosition(map.getIntersectionPosition(nextIntersection));
			
			Line itinerarySubLine = new Line(i0pos.getX(), i0pos.getY(), i1pos.getX(), i1pos.getY());
			itinerarySubLine.setStroke(Color.BLUEVIOLET);
			itinerarySubLine.setStrokeWidth(3);
			
			this.itinerary.addLast(itinerarySubLine);
			this.getChildren().add(itinerarySubLine);
			
			currentIntersection = nextIntersection;
		}
		this.car.toFront();
	}
	
	public void popNextWayPoint(){
		if(!this.itinerary.isEmpty()){
			this.getChildren().remove(this.itinerary.pop());
		}
	}
	
	/**
	 * Initially draws the complete map
	 */
	private void draw(){
		
		this.background = new Rectangle();
		this.background.setHeight(this.height);
		this.background.setWidth(this.width);
		this.background.setFill(Color.WHITE);
		
		this.getChildren().add(background);
		
		ArrayList<Integer> inters = map.getIntersections();
		for(int inter : inters){
			Position interPos = getScaledPosition(map.getIntersectionPosition(inter));
			
			Circle interCircle = new Circle(interPos.getX(), interPos.getY(), 4);
			interCircle.setFill(Color.LIGHTGREY);
			this.getChildren().add(interCircle);
			
			this.intersections.put(inter, interCircle);
			
			ArrayList<Integer> neighbors = this.map.getOutNeighborIntersections(inter);
			
			for(int neighbor : neighbors){
				
				Position neighborPos = getScaledPosition(this.map.getIntersectionPosition(neighbor));
				
				Color strokeColor = (this.map.isRoadBothDirection(inter, neighbor)) ? Color.GREY : Color.LIGHTGREY;
				int strokeWidth = (this.map.isRoadBothDirection(inter, neighbor)) ? 5 : 3;
				
				Line roadLine = new Line(interPos.getX(), interPos.getY(), neighborPos.getX(), neighborPos.getY());
				roadLine.setStrokeWidth(strokeWidth);
				roadLine.setStroke(strokeColor);
				
				Integer road  = map.getRoad(inter, neighbor);
				this.roads.put(road, roadLine);
				
				this.getChildren().add(roadLine);
				
				Disruption d = this.map.getRoadDisruption(road);
				if(d != null){
					Vect u = new Vect(neighborPos.getX() - interPos.getX(), neighborPos.getY() - interPos.getY());
					u.normalize();
					double distance = Position.evaluateDistance(neighborPos, interPos);
					
					double startFactor = distance * d.getStartsAt();
					double endFactor = distance * d.getEndsAt();
					
					Position startLinePos = interPos.getAddedTo(u.getMultipliedBy(startFactor));
					Position endLinePos = interPos.getAddedTo(u.getMultipliedBy(endFactor));
					
					Line disruptionLine = new Line(startLinePos.getX(), startLinePos.getY(), endLinePos.getX(), endLinePos.getY());
					
					if(d.getDisruptionLevel() < 0.95){
						disruptionLine.setStroke(Color.rgb(255, (int)Math.round(165 * (1 - d.getDisruptionLevel())), 0, 0.4));
					}else{
						disruptionLine.setStroke(Color.rgb(0, 0, 0, 0.5));
					}
					
					disruptionLine.setStrokeWidth(10);
					disruptionLine.setStrokeLineCap(StrokeLineCap.ROUND);
					
					this.getChildren().add(disruptionLine);
					this.disruptions.put(road, disruptionLine);					
				}
			}
		}
		
		for(Line l : this.disruptions.values()){
			l.toFront();
		}
		
		this.getChildren().add(this.car);
		this.car.toFront();
		this.background.toBack();
	}
	
	public Integer getClickedIntersection(Position clicked){
		double smallestSqDistance = Double.MAX_VALUE;
		Integer closestIntersection = null;
		
		for(Map.Entry<Integer, Circle> inter : this.intersections.entrySet()){
			double sqDistance = Position.evaluateDistance(clicked, new Position(inter.getValue().getCenterX(), inter.getValue().getCenterY()));
			if(sqDistance < smallestSqDistance){
				closestIntersection = inter.getKey();
				smallestSqDistance = sqDistance;
			}
		}
		
		if(smallestSqDistance > 13){closestIntersection = null;}
		return closestIntersection;
	}
	
	public void notifyMessageReceived(Position p){
		Position sp = this.getScaledPosition(p);
		
		Group g = new Group();
		Circle c = new Circle(sp.getX(), sp.getY(), 4, Color.YELLOW);
		g.getChildren().add(c);
		
		FadeTransition ft = new FadeTransition(Duration.millis(1000), g);
		ft.setFromValue(1.0);
		ft.setToValue(0.0);
		ft.setDelay(Duration.millis(800));
		ft.onFinishedProperty().set(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				NavigationMap.this.getChildren().remove(c);
			}
		});
		
		g.toFront();
		car.toFront();
		
		this.getChildren().add(g);
		
		this.car.notifyMessageReceived();
		ft.play();
	}
	
	public void notifyMessageSent(){
		this.car.notifyMessageSent();
	}
	
	/**
	 * @param p The old position
	 * @return The scaled position
	 */
	public Position getScaledPosition(Position p){
		return new Position(p.getX() * this.scaleX, p.getY() * this.scaleY);
	}
}

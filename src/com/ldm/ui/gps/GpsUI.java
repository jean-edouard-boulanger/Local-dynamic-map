package com.ldm.ui.gps;

import com.ldm.model.GPS;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public class GpsUI extends Pane {
	
	private GPS gps;
	
	private Canvas mapCanvas;
	
	public GpsUI(GPS gps){
		this.gps = gps;
		this.mapCanvas = new Canvas(this.getWidth(), this.getHeight());
		
		GraphicsContext gc = mapCanvas.getGraphicsContext2D();
		
		System.out.println(this.getWidth());
				
		this.getChildren().add(mapCanvas);
	}
	
	private void drawMap(){
		GraphicsContext gc = mapCanvas.getGraphicsContext2D();
		gc.fillOval(this.getWidth() / 2, this.getHeight() / 2,  50, 50);
	}
	
}

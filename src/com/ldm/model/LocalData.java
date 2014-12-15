package com.ldm.model;

import java.util.Date;

import jade.core.AID;

public class LocalData {

	private Date startDate = new Date();
	private Date endDate;
	
	private Integer roadId = null;
	
	private double progressStart = 0.0;
	private double progressEnd;
	
	private AID issuer;
	
	public LocalData(){}
	
	public LocalData(Integer roadId, AID issuer){
		this.roadId = roadId;
		this.issuer = issuer;
	}
	
	public LocalData(Integer roadId, AID issuer, Date startTime, double progressStart){
		this.issuer = issuer;
		this.startDate = startTime;
		this.roadId = roadId;
		this.progressStart = progressStart;
	}

	public Date getStartTime() {
		return startDate;
	}

	public void setStartTime(Date startTime) {
		this.startDate = startTime;
	}

	public Date getEndTime() {
		return endDate;
	}

	public void setEndTime(Date endTime) {
		this.endDate = endTime;
	}

	public Integer getRoadId() {
		return roadId;
	}

	public void setRoadId(Integer roadId) {
		this.roadId = roadId;
	}

	public double getProgressStart() {
		return progressStart;
	}

	public void setProgressStart(double progressStart) {
		this.progressStart = progressStart;
	}

	public double getProgressEnd() {
		return progressEnd;
	}

	public void setProgressEnd(double progressEnd) {
		this.progressEnd = progressEnd;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public AID getIssuer() {
		return issuer;
	}

	public void setIssuer(AID issuer) {
		this.issuer = issuer;
	}
	
	public double extrapolateTravelTime(){
		if(progressEnd == progressStart){return Double.MAX_VALUE;}
		double baseTravelTime = (endDate.getTime() - startDate.getTime()) / 1000;
		return baseTravelTime / (progressEnd - progressStart);
	}
}

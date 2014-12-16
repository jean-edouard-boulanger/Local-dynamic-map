package com.ldm.model.manager;

import jade.core.AID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ldm.data.structure.Pair;
import com.ldm.model.GPS;
import com.ldm.model.LocalData;
import com.ldm.model.RecentData;

public class RecentDataManager {
	
	private HashMap<Integer, RecentData> recentDatas = new HashMap<>();
	
	GPS gps;
	
	LocalData currentLocalData = null;
	
	public RecentDataManager(GPS gps){this.gps = gps;}
	
	synchronized public LocalData prepareNextLocalData(Integer road, AID issuer){
		if(this.currentLocalData == null){
			if(road == null || issuer == null) {return null;}
			this.currentLocalData = new LocalData(road, issuer);
			return null;
		}
		else{
			this.currentLocalData.setEndDate(new Date());
			this.currentLocalData.setProgressEnd(1.0);
			LocalData tempLocalData = this.currentLocalData;
			
			if(road == null){
				this.currentLocalData = null;
				return tempLocalData;
			}
			
			this.currentLocalData = new LocalData(road, issuer);
			return tempLocalData;
		}
	}
	
	synchronized public LocalData getLocalDataCheckpoint(double progressEnd){
		if(this.currentLocalData == null) return null;
		
		this.currentLocalData.setProgressEnd(progressEnd);
		this.currentLocalData.setEndTime(new Date());
		LocalData tmpLocalData = this.currentLocalData;
		
		this.currentLocalData = new LocalData(tmpLocalData.getRoadId(), tmpLocalData.getIssuer());
		this.currentLocalData.setProgressStart(progressEnd);
		
		return tmpLocalData;
	}
	
	public LocalData popCurrentLocalData(){
		LocalData tempLocalData = this.currentLocalData;
		this.currentLocalData = null;
		return tempLocalData;
	}
	
	public void dropCurrentLocalData(){
		this.currentLocalData = null;
	}
	
	public RecentData getRecentDataForRoad(Integer road){
		RecentData r = this.recentDatas.get(road);
		if(r != null && r.isAlive()){
			return r;
		}
		return null;
	}

	public ArrayList<RecentData> getRecentDataForRoads(ArrayList<Integer> roads){
		ArrayList<RecentData> rds = new ArrayList<>();
		RecentData rd = null;
		for(Integer road : roads){
			rd = this.getRecentDataForRoad(road);
			if(rd != null){rds.add(rd);}
		}
		return rds;
	}

	public Pair<RecentData, Boolean> merge(LocalData localData){
		if(localData == null){return new Pair<RecentData, Boolean>(null, false);}
		
		RecentData rd = this.recentDatas.get(localData.getRoadId());
		if(rd == null || rd.isExpired()){
			rd = new RecentData();
			this.recentDatas.put(localData.getRoadId(), rd);
		}
				
		boolean needSend = rd.merge(localData);
		gps.getMap().setRoadUnoficialTravelTime(rd.getRoadId(), rd.getAverageTravelTime(), rd.getExpireDate());

		return new Pair<RecentData, Boolean>(rd, needSend);
	}
	
	public Pair<RecentData, Boolean> merge(RecentData recentData){
		if(recentData == null || recentData.isExpired()){return new Pair<RecentData, Boolean>(null, false);}
		
		RecentData rd = this.recentDatas.get(recentData.getRoadId());
		if(rd == null || rd.isExpired()){
			rd = new RecentData();
			this.recentDatas.put(recentData.getRoadId(), rd);
		}
				
		boolean needSend = rd.merge(recentData);		
		gps.getMap().setRoadUnoficialTravelTime(rd.getRoadId(), rd.getAverageTravelTime(), rd.getExpireDate());
		
		return new Pair<RecentData, Boolean>(rd, needSend);
	}
}

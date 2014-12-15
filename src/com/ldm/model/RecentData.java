package com.ldm.model;

import jade.core.AID;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import org.omg.CosNaming.IstringHelper;

import com.ldm.model.helper.CollectionsHelper;

public class RecentData {

	final private static int timeout = 600000;
	
	private Integer roadId;
	
	private Date createDate = new Date();
	
	private double averageTravelTime;
	
	private HashSet<AID> contributors = new HashSet<>();
	
	public RecentData(){}
	
	public RecentData(Integer roadId, double averageTravelTime){
		this.roadId = roadId;
		this.averageTravelTime = averageTravelTime;
	}
	
	public Integer getRoadId() {
		return roadId;
	}

	public void setRoadId(Integer roadId) {
		this.roadId = roadId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public double getAverageTravelTime() {
		return averageTravelTime;
	}

	public void setAverageTravelTime(double averageTravelTime) {
		this.averageTravelTime = averageTravelTime;
	}

	public HashSet<AID> getContributors() {
		return contributors;
	}

	public void setContributors(HashSet<AID> contributors) {
		this.contributors = contributors;
	}
	
	public boolean hasContributor(AID contributor){
		return this.contributors.contains(contributor);
	}
	
	public boolean isExpired(){
		if(this.createDate == null){return true;}
		return this.createDate.getTime() + timeout < (new Date()).getTime();
	}
	
	public boolean isAlive(){
		return !this.isExpired();
	}
	
	public boolean merge(LocalData localData){
		if(localData == null){return false;}
		
		if(this.contributors.size() == 0){
			this.contributors.add(localData.getIssuer());
			this.averageTravelTime = localData.extrapolateTravelTime();
			this.roadId = localData.getRoadId();
		}
		else
		{
			this.averageTravelTime = (this.averageTravelTime * this.contributors.size() + localData.extrapolateTravelTime()) 
					/ (this.contributors.size() + 1);
			this.contributors.add(localData.getIssuer());
		}
		return true;
	}
	
	public boolean merge(RecentData recentData){
		if(recentData == null){return false;}
		if(recentData == this){return false;}
		
		if(this.contributors.size() == 0){
			this.roadId = recentData.roadId;
			this.contributors = recentData.contributors;
			this.createDate = recentData.createDate;
			this.averageTravelTime = recentData.averageTravelTime;
		}
		else{
			int nbCommonContributors = CollectionsHelper.countCommonElements(this.contributors, recentData.getContributors());
			
			this.averageTravelTime = (this.averageTravelTime * this.contributors.size() + 
					recentData.averageTravelTime * (recentData.contributors.size() - nbCommonContributors)) / 
					(this.contributors.size() + (recentData.contributors.size() - nbCommonContributors));
			
			CollectionsHelper.merge(contributors, recentData.contributors);
		}
		return true;
	}
	
	@Override
	public boolean equals(Object o){
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof RecentData)) return false;
		RecentData other = (RecentData) o;
		if(this.contributors.size() != other.contributors.size()) return false;
		
		for(AID aid : this.contributors){
			if(other.contributors.contains(aid)){
				return false;
			}
		}
		return true;
	}
	
}

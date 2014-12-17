package com.ldm.sma.message;

import java.util.ArrayList;

import com.ldm.model.RecentData;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ExplorationAnswerMessage extends Message {

	int ttl = 0;
	AID answerIssuer;
	ArrayList<RecentData> recentDatas = new ArrayList<>();
	
	public ExplorationAnswerMessage(){}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public AID getAnswerIssuer() {
		return answerIssuer;
	}

	public void setAnswerIssuer(AID answerIssuer) {
		this.answerIssuer = answerIssuer;
	}

	public ArrayList<RecentData> getRecentDatas() {
		return recentDatas;
	}

	public void addRecentData(RecentData rd){
		if(rd == null) return;
		this.recentDatas.add(rd);
	}
	
	public void setRecentDatas(ArrayList<RecentData> recentDatas) {
		this.recentDatas = recentDatas;
	}
	
	public int decreaseTtl(){
		return --this.ttl;
	}
	
	@Override
	public boolean accept(MessageVisitor visitor, ACLMessage aclMsg) {
		return visitor.onExplorationAnswerMessage(this, aclMsg);
	}
	
}

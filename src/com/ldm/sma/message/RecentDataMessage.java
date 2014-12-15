package com.ldm.sma.message;
import java.util.ArrayList;

import com.ldm.model.RecentData;

import jade.lang.acl.ACLMessage;

public class RecentDataMessage extends Message {

	ArrayList<RecentData> recentDatas = new ArrayList<RecentData>();
	
	public RecentDataMessage() {}
	
	public RecentDataMessage(RecentData recentData){ recentDatas.add(recentData); }
	
	public RecentDataMessage(ArrayList<RecentData> recentDatas){ this.recentDatas = recentDatas; }
	
	@Override
	public boolean accept(MessageVisitor visitor, ACLMessage aclMsg) {
		return visitor.onRecentDataMessage(this, aclMsg);
	}

	public ArrayList<RecentData> getRecentDatas() {
		return recentDatas;
	}

	public void setRecentDatas(ArrayList<RecentData> recentDatas) {
		this.recentDatas = recentDatas;
	}	
}


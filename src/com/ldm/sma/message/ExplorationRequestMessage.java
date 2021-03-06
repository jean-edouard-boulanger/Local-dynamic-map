package com.ldm.sma.message;

import java.util.LinkedList;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ExplorationRequestMessage extends Message {

	private AID requestIssuer;
	private String explorationRequestId;
	
	
	private LinkedList<Integer> itinerary;
	
	private int hops = 1;
	private int ttl;
	private int sequenceNumber = 0;

	
	public ExplorationRequestMessage(){}

	public AID getRequestIssuer() {
		return requestIssuer;
	}

	public void setRequestIssuer(AID requestIssuer) {
		this.requestIssuer = requestIssuer;
	}

	public String getExplorationRequestId() {
		return explorationRequestId;
	}

	public void setExplorationRequestId(String explorationRequestId) {
		this.explorationRequestId = explorationRequestId;
	}

	public LinkedList<Integer> getItinerary() {
		return itinerary;
	}

	public void setItinerary(LinkedList<Integer> itinerary) {
		this.itinerary = itinerary;
	}

	public int getHops() {
		return hops;
	}

	public void setHops(int hops) {
		this.hops = hops;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public void addHop(){
		this.ttl--;
		this.hops++;
	}
	
	public int increaseSequenceNumber(){
		return ++this.sequenceNumber;
	}
	
	public int getSequenceNumber(){
		return this.sequenceNumber;
	}
	
	public void setSequenceNumber(int sequenceNumber){
		this.sequenceNumber = sequenceNumber;
	}
	
	@Override
	public boolean accept(MessageVisitor visitor, ACLMessage aclMsg) {
		return visitor.onExplorationRequestMessage(this, aclMsg);
	}
}

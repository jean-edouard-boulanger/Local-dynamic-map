package com.ldm.sma.agent;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldm.model.geometry.Position;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class ShortRangeAgent extends GuiAgent {

	private static final String sentAtPositionParameterName = "sent_at_position";
	private static final String filterDistanceProtocolName = "filter_distance";
	private static final MessageTemplate filterDistanceProtocol = MessageTemplate.MatchProtocol(filterDistanceProtocolName);
	private static final String SRATypeName = "SRA";
	
	private double range = 400;
	private DFAgentDescription SRATemplate = null;
	
	public abstract Position getCurrentPosition();
	
	public abstract void onMessageReceivedFromAround(ACLMessage aclMsg, Position sentAtPosition);
	public abstract void onMessageSentAround(ACLMessage aclMsg, Position sentAtPosition);
	
	public ShortRangeAgent(){
		super();
	}
	
	public void setup(){
		super.setup();
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType(SRATypeName);
		sd.setName(this.getLocalName() + "-" + SRATypeName);
		
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
	}
	
	private List<AID> findAllSRA(){
		if(this.SRATemplate == null){
			SRATemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(SRATypeName);
			SRATemplate.addServices(sd);
		}
		
		List<AID> allSRA = new ArrayList<>();
		try{
			DFAgentDescription[] results = DFService.search(this, SRATemplate);
			for(DFAgentDescription dfad : results){
				allSRA.add(dfad.getName());
			}
			return allSRA;
			
		}catch(FIPAException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void setRange(double FOVDistance){
		this.range = FOVDistance;
	}
	
	public double getRange(){
		return this.range;
	}
	
	public void sendAround(ACLMessage arg0){
		arg0.setProtocol(filterDistanceProtocolName);
		
		Position position = this.getCurrentPosition();
		setSentAtPosition(arg0, position);
		
		arg0.clearAllReceiver();
		List<AID> allSRA = this.findAllSRA();
		for(AID aid : allSRA){
			if(!aid.equals(this.getAID())){
				arg0.addReceiver(aid);
			}
		}
		
		this.onMessageSentAround(arg0, position);
		this.send(arg0);
	}
	
	public ACLMessage receiveFromAround(MessageTemplate pattern){
		if(pattern == null){
			pattern = MessageTemplate.MatchAll();
		}
		
		ACLMessage received = this.receive(MessageTemplate.and(pattern, filterDistanceProtocol));
		if(received == null){
			return null;
		}
		
		received.setProtocol(null);
		
		Position sentAtPosition = getSentAtPosition(received);
		
		if(sentAtPosition == null){return null;}
		
		double distance = Position.evaluateDistance(this.getCurrentPosition(), sentAtPosition);		
		if(sentAtPosition != null && distance < range){
			this.onMessageReceivedFromAround(received, sentAtPosition);
			return received;
		}
		
		return null;
	}
	
	public ACLMessage receiveFromAround(){
		return this.receiveFromAround(MessageTemplate.MatchAll());
	}
	
	private static Position getSentAtPosition(ACLMessage m){
		Position p = null;
		
		String serializedPosition = m.getUserDefinedParameter(sentAtPositionParameterName);
		if(serializedPosition == null){
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			p = (Position)mapper.readValue(serializedPosition, Position.class);
		} catch (IOException e) {
			return null;
		}
		return p;
	}
	
	private static void setSentAtPosition(ACLMessage m, Position currentPosition){
		StringWriter sw = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(sw, currentPosition);
			m.addUserDefinedParameter(sentAtPositionParameterName, sw.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

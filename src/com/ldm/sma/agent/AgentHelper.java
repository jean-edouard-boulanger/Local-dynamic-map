package com.ldm.sma.agent;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldm.model.geometry.Position;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgentHelper {

	public static final String sendPositionParameterName = "send_position";
	
	public static final String filterDistanceProtocolName = "filter_distance";
	public static final MessageTemplate filterDistanceProtocol = MessageTemplate.MatchProtocol(filterDistanceProtocolName);
	
	public static Position getSentAtPosition(ACLMessage m){
		Position p = null;
		
		String serializedPosition = m.getUserDefinedParameter(sendPositionParameterName);
		if(serializedPosition == null){
			System.err.println("[Warning] Champ " + sendPositionParameterName + " vide");
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			p = (Position)mapper.readValue(serializedPosition, Position.class);
		} catch (IOException e) {
			System.err.println("[Error] Impossible de dé-serialiser la position `"+ serializedPosition +"`: " + e.getMessage());
			e.printStackTrace(); 
			return null;
		}
		return p;
	}	
}

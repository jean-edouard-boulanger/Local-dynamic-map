package com.ldm.sma.agent.helper;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldm.model.geometry.Position;
import com.ldm.sma.agent.ShortRangeAgent;
import com.ldm.sma.message.Message;
import com.ldm.sma.message.MessageVisitor;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgentHelper {

	public static boolean receiveMessage(Agent agent, MessageTemplate template, MessageVisitor visitor){
		ACLMessage aclmsg = agent.receive(template);
		return handleMessage(aclmsg, visitor);
	}
	
	public static boolean receiveMessageFromAround(ShortRangeAgent agent, MessageTemplate template, MessageVisitor visitor){
		ACLMessage aclmsg = agent.receiveFromAround(template);
		return handleMessage(aclmsg, visitor);
	}
	
	private static boolean handleMessage(ACLMessage aclmsg, MessageVisitor visitor){
		if(aclmsg == null)
			return false;
		
		Message message;
		try {
			message = Message.fromJson(aclmsg.getContent());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		if(visitor == null)
			return false;
		
		if(!message.accept(visitor, aclmsg))
			return false;
		
		return true;
	}
}

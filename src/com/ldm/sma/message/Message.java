package com.ldm.sma.message;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.lang.acl.ACLMessage;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")  
public abstract class Message {
	public abstract boolean accept(MessageVisitor visitor, ACLMessage aclMsg);

	public static Message fromJson(String JSONSerializedMessage) throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return (Message)mapper.readValue(JSONSerializedMessage, Message.class);
	}
	
	public String toJson() throws IOException{
		StringWriter sw = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(sw, this);
		return sw.toString();
	}
}

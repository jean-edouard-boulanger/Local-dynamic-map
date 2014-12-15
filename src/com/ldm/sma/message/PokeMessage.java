package com.ldm.sma.message;

import jade.lang.acl.ACLMessage;

public class PokeMessage extends Message {
		
	@Override
	public String toString(){
		return "POKE";
	}

	@Override
	public boolean accept(MessageVisitor visitor, ACLMessage aclMsg) {
		return visitor.onPokeMessage(this, aclMsg);
	}
	
}

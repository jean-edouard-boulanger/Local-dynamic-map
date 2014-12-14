package com.ldm.sma.message;

import jade.lang.acl.ACLMessage;

public class MessageVisitor{ 
	
	public boolean onIRMessage(IRMessage message, ACLMessage aclMsg){return false;}
	
	public boolean onPokeMessage(PokeMessage message, ACLMessage aclMsg){return false;}
	
}

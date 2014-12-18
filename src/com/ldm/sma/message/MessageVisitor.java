package com.ldm.sma.message;

import jade.lang.acl.ACLMessage;

public class MessageVisitor{ 
	
    public boolean onRecentDataMessage(RecentDataMessage message, ACLMessage aclMsg){return false;}

	public boolean onExplorationRequestMessage(ExplorationRequestMessage message, ACLMessage aclMsg){return false;}
	public boolean onExplorationAnswerMessage(ExplorationAnswerMessage message, ACLMessage aclMsg){return false;}
	
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ldm.sma.message;
import com.ldm.model.structure.IR;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author franck
 */

public class IRMessage extends Message {

	IR message;
        
	public IRMessage(IR message){
		this.message = message;
	}

	public IR getMessage() {
		return message;
	}

	public void setMessage(IR message) {
		this.message = message;
	}

	@Override
	public boolean accept(MessageVisitor visitor, ACLMessage aclMsg) {
		return visitor.onIRMessage(this, aclMsg);
	}
}


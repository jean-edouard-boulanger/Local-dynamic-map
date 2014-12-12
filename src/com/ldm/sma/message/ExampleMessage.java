package com.ldm.sma.message;

import jade.lang.acl.ACLMessage;

public class ExampleMessage extends Message {

	int exampleField1;
	String exampleField2;
	
	public ExampleMessage(int exampleField1, String exampleField2){
		this.exampleField1 = exampleField1;
		this.exampleField2 = exampleField2;
	}

	public int getExampleField1() {
		return exampleField1;
	}

	public void setExampleField1(int exampleField1) {
		this.exampleField1 = exampleField1;
	}

	public String getExampleField2() {
		return exampleField2;
	}

	public void setExampleField2(String exampleField2) {
		this.exampleField2 = exampleField2;
	}
	
	@Override
	public boolean accept(MessageVisitor visitor, ACLMessage aclMsg) {
		return visitor.onExampleMessage(this, aclMsg);
	}
}

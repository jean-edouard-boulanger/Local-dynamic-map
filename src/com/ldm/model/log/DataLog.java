package com.ldm.model.log;

public class DataLog extends Log {

	Integer roadId;
	double travelTime;
	
	public DataLog(){super();}
	public DataLog(String message){super(message);}
	
	@Override
	public String getStyle(){
		return "-fx-background-color: #FCBDC4";
	}	
}

package com.ldm.model.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	private Date logDate = new Date();
	private String message;
	private final String style = null;
	
	public Log(){}
	
	public Log(String message){this.message = message;}
	
	public Log(Date logDate, String message){
		this.logDate = logDate;
		this.message = message;
	}

	public Date getLogDate() {
		return logDate;
	}

	public String getFormattedLogDate(){
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		return formatter.format(this.logDate);
	}
	
	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getStyle(){
		return this.style;
	}
	
}

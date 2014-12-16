package com.ldm.model;

import java.util.Date;

public class Expirable<T> {
	
	T data;
	Date expireDate = new Date();
	
	public Expirable(T data){this.data = data;}
	
	public Expirable(T data, Date expireDate){
		this.data = data;
		this.expireDate = expireDate;
	}
	
	public void setData(T data){
		this.data = data;
	}
	
	public T getData(){
		if((new Date()).getTime() < expireDate.getTime()){
			return this.data;
		}
		return null;
	}
	
	public void setExpireDate(Date expireDate){
		this.expireDate = expireDate;
	}
	
}

package com.ldm.data.structure;

public class Pair<U,V> {
	public U first;
	public V second;
	
	public Pair(U first, V second){
		this.first = first;
		this.second = second;
	}
	
	@Override
	public String toString(){
		return "< "+ this.first.toString() +" ; "+ this.second.toString() + " >";
	}
}

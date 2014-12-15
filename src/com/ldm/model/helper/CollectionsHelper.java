package com.ldm.model.helper;

import java.util.Collection;

public class CollectionsHelper {

	public static <T> int countCommonElements(Collection<T> c1, Collection<T> c2){
		int nbCommon = 0;
		for(T e : c1){
			if(c2.contains(e)){
				nbCommon++;
			}
		}
		return nbCommon;
	}
	
	public static <T> Collection<T> merge(Collection<T> c1, Collection<T> c2){
		for(T e : c2){
			if(!c1.contains(e)){
				c1.add(e);
			}
		}
		return c1;
	}
}

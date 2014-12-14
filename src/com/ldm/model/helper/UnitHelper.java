package com.ldm.model.helper;

public class UnitHelper {

	/**
	 * @param kmh Kilometers per hour
	 * @return Meters per second
	 */
	public static double kmhTOms(double kmh){
		return (kmh / 3600) * 1000;
	}
	
	/**
	 * @param s Seconds
	 * @return Milliseconds
	 */
	public static double sTOms(double s){
		return s / 1000;
	}
	
	/**
	 * @param ms Milliseconds
	 * @return Seconds
	 */
	public static double msTOs(double ms){
		return ms * 1000;
	}
	
}

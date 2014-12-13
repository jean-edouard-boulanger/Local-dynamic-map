package com.ldm.model.structure;

import java.util.Date;

import com.ldm.model.geometry.Position;

public class DL {
	 final private long DL_TIMEOUT = 2000;
	
	 private Position posDepart = new Position(0,0);
	 private Position posArrivee = new Position(0,0);
	 private Date HeureDébut;
	 private Date HeureFin;
	 
	 public DL(Position pos, Date tps){
		 this.setPosDepart(pos);
		 this.setHeureDébut(tps);
	 }
	 
	 public void saveDL(Position pos, Date tps){
		 this.posArrivee=pos;
		 this.HeureFin = tps;
		 
	 }
	 
	 public long getTpsParcours(){
		 if(HeureDébut != null && HeureFin != null){
			 return (HeureFin.getTime() - HeureDébut.getTime()); 
		 }
		 return 0;
	 }

	public Position getPosDepart() {
		return posDepart;
	}

	public void setPosDepart(Position posDepart) {
		this.posDepart = posDepart;
	}

	public Position getPosArrivee() {
		return posArrivee;
	}

	public void setPosArrivee(Position posArrivee) {
		this.posArrivee = posArrivee;
	}

	public Date getHeureDébut() {
		return HeureDébut;
	}

	public void setHeureDébut(Date heureDébut) {
		HeureDébut = heureDébut;
	}

	public Date getHeureFin() {
		return HeureFin;
	}

	public void setHeureFin(Date heureFin) {
		HeureFin = heureFin;
	}

	public long getDL_TIMEOUT() {
		return DL_TIMEOUT;
	}


	    
}

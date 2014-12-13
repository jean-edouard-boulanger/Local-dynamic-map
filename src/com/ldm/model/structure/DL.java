package com.ldm.model.structure;

import java.util.Date;

import com.ldm.model.geometry.Position;

public class DL {
	 final private long DL_TIMEOUT = 2000;
	
	 private Position posDepart = new Position(0,0);
	 private Position posArrivee = new Position(0,0);
	 private Date HeureDebut;
	 private Date HeureFin;
	 
	 public DL(Position pos, Date tps){
		 this.setPosDepart(pos);
		 this.setHeureDebut(tps);
	 }
	 
	 public void saveDL(Position pos, Date tps){
		 this.posArrivee=pos;
		 this.HeureFin = tps;
	 }
	 
	 public long getTpsParcours(){
		 if(HeureDebut != null && HeureFin != null){
			 return (HeureFin.getTime() - HeureDebut.getTime()); 
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

	public Date getHeureDebut() {
		return HeureDebut;
	}

	public void setHeureDebut(Date heureDebut) {
		HeureDebut = heureDebut;
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

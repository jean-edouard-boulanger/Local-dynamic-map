package com.ldm.model.structure;

import java.util.Date;

import com.ldm.model.geometry.Position;

public class DL {
	 final private long DL_TIMEOUT = 2000;
	
	 private Position posDepart = new Position(0,0);
	 private Position posArrivee = new Position(0,0);
	 private Date heureDebut;
	 private Date heureFin;
	 
	 public DL(Position pos, Date tps){
		 this.setPosDepart(pos);
		 this.heureDebut =tps;

	 }
	 
	 public void saveDL(Position pos, Date tps){
		 this.posArrivee=pos;
		 this.heureFin = tps;
		 
	 }
	 
	 public long getTpsParcours(){

		 if(heureDebut != null && heureFin != null){
			 return (heureFin.getTime() - heureDebut.getTime()); 
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
		return heureDebut;
	}

	public void setHeureDebut(Date heureDebut) {
		this.heureDebut = heureDebut;
	}

	public Date getHeureFin() {
		return heureFin;
	}

	public void setHeureFin(Date heureFin) {
		this.heureFin = heureFin;
	}

	public long getDL_TIMEOUT() {
		return DL_TIMEOUT;
	}


	    
}

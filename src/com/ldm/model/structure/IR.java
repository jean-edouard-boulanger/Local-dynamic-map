package com.ldm.model.structure;

import com.ldm.model.geometry.Position;
import java.util.Date;

/**
 *
 * @author Wafflewrath - Jiheu SALE NOOOOOOOOB !
 */
public class IR {
    final private long IR_TIMEOUT = 2000;
    
    private Position posDepart = new Position(0,0);
    private Position posArrivee = new Position(0,0);
    private Date creationTimestamp = new Date();
    private double averageTime = 0;
    private int vehiculesNumber = 0;
    private boolean isTimout = false;
    
    // default
    public IR() {}
    
    // full constructor
    public IR(Position pos1, Position pos2, Date date, double time, int number, boolean timeout)
    {
        this.posDepart = pos1;
        this.posArrivee = pos2;
        this.creationTimestamp = date;
        this.averageTime = time;
        this.vehiculesNumber = number;
        this.isTimout = timeout;
    }
    
    // fastforward constructor
    public IR(Position pos1, Position pos2, Date date, double time)
    {
        this.posDepart = pos1;
        this.posArrivee = pos2;
        this.creationTimestamp = date;
        this.averageTime = time;
        this.vehiculesNumber = 1;
        this.isTimout = false;
    }
    
    public Position getPosDepart()
    {
        return this.posDepart;
    }
    
    public Position getPosarrivee()
    {
        return this.posArrivee;
    }
    
    public Date getCreationTimestamp()
    {
        return this.creationTimestamp;
    }
    
    public double getAverageTime()
    {
        return this.averageTime;
    }
    
    public int getVehiculesNumber()
    {
        return this.vehiculesNumber;
    }
    
    public boolean getIsTimeout()
    {
        return this.isTimout;
    }
    
    
    public boolean isOlderThan(IR newIR)
    {        
        if ((newIR.creationTimestamp.getTime() - this.creationTimestamp.getTime()) > this.IR_TIMEOUT)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

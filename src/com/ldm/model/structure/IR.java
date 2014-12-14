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
    private long averageTime = 0;
    private int vehiculesNumber = 0;
    private boolean isTimout = false;
    
    // default
    public IR() {}
    
    // full constructor
    public IR(Position pos1, Position pos2, Date date, long time, int number, boolean timeout)
    {
        this.posDepart = pos1;
        this.posArrivee = pos2;
        this.creationTimestamp = date;
        this.averageTime = time;
        this.vehiculesNumber = number;
        this.isTimout = timeout;
    }
    
    // fastforward constructor
    public IR(Position pos1, Position pos2, long time)
    {
        this.posDepart = pos1;
        this.posArrivee = pos2;
        this.creationTimestamp = new Date();
        this.averageTime = time;
        this.vehiculesNumber = 1;
        this.isTimout = false;
    }
    
    public Position getPosDepart()
    {
        return this.posDepart;
    }
    
    public Position getPosArrivee()
    {
        return this.posArrivee;
    }
    
    public Date getCreationTimestamp()
    {
        return this.creationTimestamp;
    }
    
    public long getAverageTime()
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
    
    public void updateAverage(Long l){
        this.vehiculesNumber++;
        this.averageTime = (this.getAverageTime() + l) / (this.getVehiculesNumber()); 
    }
    
    public boolean isTooOld()
    {        
        Date currentDate = new Date();
        if (this.creationTimestamp.getTime() + this.IR_TIMEOUT > currentDate.getTime())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

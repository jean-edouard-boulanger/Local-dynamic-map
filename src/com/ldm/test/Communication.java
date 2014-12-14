/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ldm.test;

import com.ldm.model.geometry.Position;
import com.ldm.model.structure.DL;
import com.ldm.sma.agent.CarAgent;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author franck
 */
public class Communication {
    public static void main(String[] args) {    
        //we build the Agents
        CarAgent agent1 = new CarAgent();
        CarAgent agent2 = new CarAgent();
        
        //we set IR for them
        Long l = new Date().getTime();
        DL dl1 = new DL(new Position(1,1), new Date(l));
        
        dl1.saveDL(new Position(1,3), new Date(l+15));
        agent1.aggregateDL(dl1);
        
        System.out.println(agent2.getIRsCollection());
    }
}

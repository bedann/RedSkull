/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.animation.Interpolator;

/**
 *
 * @author Monroe
 */
public class Bounce extends Interpolator{
    
    
    double amplitude = 0.2;
    double freq = 20;

    public Bounce() {
    }
    
    

    public Bounce(double amplitude, double freq) {
        this.amplitude = amplitude;
        this.freq = freq;
    }
    
    
    @Override
    protected double curve(double t) {
        return (float)(-1*Math.pow(Math.E,-t/amplitude)*Math.cos(freq*t)+1);
    }
        
    
}

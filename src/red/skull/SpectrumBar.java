/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Monroe
 */
public class SpectrumBar extends VBox{
    
    private static double SPACING = 1.0;
    private double ASPECT_RATIO = 3.0;
    private double MIN_BAR_HEIGHT = 3.0;
    private double lastWidth = 0;
    private double lastHeight = 0.0;
    private double bar_count = 0;
    private double maxValue = 0;
    
    public SpectrumBar(double maxValue, double bar_count) {
        this.bar_count = bar_count;
        this.maxValue = maxValue;
        for(int i = 0; i<bar_count; i++){
            
            int c = (int)((double)i/bar_count*255.0);
            
            Rectangle r = new Rectangle();
            r.setVisible(false);
            r.setStyle("-fx-background-color: red");
            r.setArcHeight(2);
            r.setArcWidth(2);
            
            this.getChildren().add(r);
        }
        
        
        this.getStyleClass().add("spec");
        this.setSpacing(SPACING);
        this.setAlignment(Pos.BOTTOM_CENTER);
    }

    @Override
    protected void layoutChildren() {
        if (lastWidth != getWidth() || lastHeight != getHeight()) {
            double  spacing = SPACING * (bar_count - 1);
            double barHeight = (getHeight() - getVerticalPadding() - spacing) / bar_count;
            double barWidth = Math.min(barHeight * ASPECT_RATIO, getWidth() - getHorizontalPadding());

            for (Node node: getChildren()) {
              Rectangle  r = (Rectangle)node;
              r.setWidth(barWidth);
              r.setHeight(barHeight);
            }

            lastWidth = getWidth();
            lastHeight = getHeight();
        }
        super.layoutChildren(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected double computePrefHeight(double width) {
        return computeHeight(5); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected double computePrefWidth(double height) {
        return computeWidthForHeight(5);
    }

    @Override
    protected double computeMinHeight(double width) {
        return computeHeight(MIN_BAR_HEIGHT); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected double computeMinWidth(double height) {
        return computeWidthForHeight(MIN_BAR_HEIGHT); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setValue(double value) {
    double barsLit = Math.min(bar_count,(int) Math.round(value / maxValue * bar_count));
    ObservableList<Node> childList = getChildren();
    for (int i = 0; i<childList.size(); i++) {
        childList.get(i).setVisible(i > bar_count - barsLit);
    }
  }
    
    private double computeHeight(double barHeight){
    double vPadding = this.getVerticalPadding();
    double barHeights = barHeight * bar_count;
    double spacing = SPACING * (bar_count - 1);
    return barHeights + spacing + vPadding;
  }
    
    private double getVerticalPadding(){
    Insets padding = this.getPadding();
    return padding.getTop() + padding.getBottom();
  }
    
    private double getHorizontalPadding(){
    Insets padding = this.getPadding();
    return padding.getLeft()+ padding.getRight();
  }
    
    
    private double computeWidthForHeight(double barHeight){
        return barHeight * ASPECT_RATIO + getHorizontalPadding();
    }

    
}

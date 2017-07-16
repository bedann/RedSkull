/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.PopOver;

/**
 *
 * @author Monroe
 */
public interface MiniListener {
     public void bindTimers(Label ellapsed,Label total);
    
    public void bindVolume(Slider slider);
    
    public void bindImage(ImageView image);
    
    public void bindSeeker(DoubleProperty progress);
    
    public void bindPlayButton(BooleanProperty binder);
    
    public void bindNextButton(BooleanProperty binder);
    
    public void bindPrevButton(BooleanProperty binder);
    
    public void setName(String name);
    
    public void updateUi(boolean playing);
    
    public PopOver popOver();
    
    void show();
    
    void close();
    
    void setTitle(String name);
    
    void setImage(Image image);
    
    public void setPlayer(Player player);
    
}

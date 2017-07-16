/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.NotificationPane;

/**
 *
 * @author Monroe
 */
public class Notify extends NotificationPane{
    
    ImageView error = new ImageView(new Image(getClass().getResourceAsStream("images/error.png")));
    ImageView tick = new ImageView(new Image(getClass().getResourceAsStream("images/tick.png")));

    public Notify() {
        this.setOnMouseClicked(e->{this.hide();});
        this.getStyleClass().add(STYLE_CLASS_DARK);
        this.showingProperty().addListener((obs,o,n)->{
            if(n){
                new Timeline(new KeyFrame(Duration.millis(5000),e->{
                    this.hide();
                })).play();
            }
        });
    }
    
    
    
    public void show(boolean error,String text){
        this.show(text, error?this.error:this.tick);
    }
    
}

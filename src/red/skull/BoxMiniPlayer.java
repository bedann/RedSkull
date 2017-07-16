/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Monroe
 */
public class BoxMiniPlayer extends BaseMiniPlayer {
    
    
    HBox controlsLayer1 = new HBox(10);
    HBox controlsLayer2 = new HBox(10);
    VBox controls = new VBox(20);
    StackPane fade = new StackPane();
    private final int play_button_size = 30;

    public BoxMiniPlayer(Stage stage) {
        this.mainStage = stage;
        this.image.setFitWidth(200);
        this.image.setPreserveRatio(true);
       play.setGraphic(image("play-arrow",play_button_size));
        
        progress.getStyleClass().add("progress-bar");
        
        
        HBox.setMargin(next,new Insets(20,0,0,0));
        HBox.setMargin(previous,new Insets(20,0,0,0));
        
        controlsLayer1.getChildren().addAll(previous,play,next);
        controlsLayer1.setAlignment(Pos.CENTER);
        
        controlsLayer2.getChildren().addAll(volume,expand);
        controlsLayer2.setAlignment(Pos.CENTER);
        
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: orangered");
        ellapsed.setStyle("-fx-text-fill: #cecece");
        total.setStyle(ellapsed.getStyle());
        name.setPadding(new Insets(10));
        name.setAlignment(Pos.CENTER);
        name.setMaxHeight(40);
        
        
        controls.setPadding(new Insets(20));
        controls.getChildren().addAll(controlsLayer1,controlsLayer2);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-background-color:#b3000000;");
        
        
        fade.setStyle("-fx-background-color:#000000;");
        fade.setOpacity(0.7);
        
        main.setPrefHeight(200);
        main.setMaxWidth(200);
        main.setStyle("-fx-background-color:#0b0b0b;");
        main.getChildren().addAll(image,fade,name,controls,progress);
        StackPane.setAlignment(progress, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(name, Pos.BOTTOM_CENTER);
        StackPane.setMargin(name, new Insets(0,10,10,10));
        
        
        final ScaleTransition scale = new ScaleTransition(Duration.millis(200),controls);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        main.setOnMouseEntered(e->{
            if(controls.getScaleX()<1){
                scale.setByX((1-controls.getScaleX()));
                scale.setByY((1-controls.getScaleY()));
                scale.play();
            }
            fade.setOpacity(0.9);
            controls.setScaleX(1);
            controls.setScaleY(1);
        });
        main.setOnMouseExited(e->{
            scale.setByX(scale_factor);
            scale.setByY(scale_factor);
            if(controls.getScaleX()==1)scale.play();
            fade.setOpacity(0.7);
        });
        this.setX(bounds.getWidth()-200);
        this.setY(bounds.getHeight()-200);
        
        final Timeline t1 = new Timeline();
        final KeyFrame show = new KeyFrame(Duration.millis(100),e->{
            Event.fireEvent(main, new MouseEvent(MouseEvent.MOUSE_ENTERED,
            scene.getX(), scene.getY(), scene.getX(), scene.getY(), MouseButton.PRIMARY, 1,
            true, true, true, true, true, true, true, true, true, true, null));
        });
         final KeyFrame hide = new KeyFrame(Duration.millis(500),e->{
            Event.fireEvent(main, new MouseEvent(MouseEvent.MOUSE_EXITED,
            scene.getX(), scene.getY(), scene.getX(), scene.getY(), MouseButton.PRIMARY, 1,
            true, true, true, true, true, true, true, true, true, true, null));
        });
        
        this.showingProperty().addListener((obs,o,n)->{
            t1.getKeyFrames().clear();
            if(n){
                t1.getKeyFrames().add(hide);
            }else{
                t1.getKeyFrames().add(show);
            }
            t1.play();
        });
    }

    @Override
    public void updatePorgress(Double p) {
        progress.setProgress(p);
    }
    
    
    @Override
    public void updateUi(boolean playing){
        play.setGraphic(image(playing?"pause-button":"play-arrow",play_button_size));
    }
    
    
}

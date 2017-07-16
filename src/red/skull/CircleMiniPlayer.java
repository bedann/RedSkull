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
import javafx.beans.property.BooleanProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.swing.event.HyperlinkEvent;
import org.controlsfx.control.PopOver;

/**
 *
 * @author Monroe
 */
public class CircleMiniPlayer extends BaseMiniPlayer{
    
    ProgressIndicator progress = new ProgressIndicator();
    HBox controlsLayer1 = new HBox(10);
    HBox controlsLayer2 = new HBox(10);
    VBox controls = new VBox(20);
    ImageView fade = image("faded_circle",180);
    StackPane content = new StackPane();
    Circle circle = new Circle();
    private final int play_button_size = 30;
    

    public CircleMiniPlayer(Stage mainStage) {
        this.mainStage = mainStage;
       play.setGraphic(image("play-arrow",play_button_size));
       this.modifyStyleSheet("mini");
        
        progress.getStyleClass().add("progress-indi");
        progress.setPrefSize(200, 200);
        progress.setStyle("-fx-progress-color: red");
        progress.setVisible(false);
        
        circle.setRadius(90);
        HBox.setMargin(next,new Insets(20,0,0,0));
        HBox.setMargin(previous,new Insets(20,0,0,0));
        
        controlsLayer1.getChildren().addAll(previous,play,next);
        controlsLayer1.setAlignment(Pos.CENTER);
        
        controlsLayer2.getChildren().addAll(volume,expand);
        controlsLayer2.setAlignment(Pos.CENTER);
        
        name.setWrapText(false);
        name.setMaxWidth(100);
        name.setStyle("-fx-text-fill: orangered");
        ellapsed.setStyle("-fx-text-fill: #cecece");
        total.setStyle(ellapsed.getStyle());
//        name.setPadding(new Insets(20));
        
        
        controls.setPadding(new Insets(20));
        controls.getChildren().addAll(controlsLayer1,controlsLayer2);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-background-color:#b3000000;");
        
     
        
        content.getChildren().addAll(circle,fade,controls,name);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(5));
        StackPane.setAlignment(name, Pos.BOTTOM_CENTER);
        StackPane.setMargin(name, new Insets(20));
        
        main.setPrefHeight(200);
        StackPane.setMargin(content, new Insets(0,0,18,0));
        main.setStyle("-fx-background-color:transparent;");
        main.getChildren().addAll(progress,content);
        main.setAlignment(Pos.CENTER);
        
        final ScaleTransition scale = new ScaleTransition(Duration.millis(200),controls);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        main.setOnMouseEntered(e->{
            if(controls.getScaleX()<1){
                scale.setByX((1-controls.getScaleX()));
                scale.setByY((1-controls.getScaleY()));
                scale.play();
            }
                fade.setOpacity(1);
        });
        main.setOnMouseExited(e->{
            scale.setByX(scale_factor);
            scale.setByY(scale_factor);
            if(controls.getScaleX()==1)scale.play();
            if(Player.random_image){
                fade.setOpacity(1);
            }else{
                fade.setOpacity(0.5);
            }
        });
        this.setX(bounds.getWidth()-200);
        this.setY(bounds.getHeight()-200);
        
        progress.indeterminateProperty().addListener((obs,o,n)->{
            progress.setVisible(!n);
        });
        
       
        
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
    public void bindImage(ImageView image) {
        image.imageProperty().addListener((obs,o,n)->{
            if(n != null){
                ImagePattern pattern = new ImagePattern(n);
                circle.setFill(pattern);
                if(Player.random_image){
                    fade.setOpacity(1);
                }else{
                    fade.setOpacity(0.5);
                }
            }
        });
    }

    @Override
    public void setImage(Image image) {
         //To change body of generated methods, choose Tools | Templates.
        ImagePattern pattern = new ImagePattern(image);
        circle.setFill(pattern);
    }
    
    
    
    @Override
    public void updateUi(boolean playing){
        play.setGraphic(image(playing?"pause-button":"play-arrow",play_button_size));
    }

    @Override
    public void updatePorgress(Double p) {
        progress.setProgress(p);
    }
    
    
    
    
}

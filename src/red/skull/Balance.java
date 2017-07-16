/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.gluonhq.charm.glisten.animation.PulseTransition;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Monroe
 */
public class Balance extends StackPane{
    
    Label title = new Label("BALANCE");
    Button back = new Button(null,icon("back3",30));
    ImageView leftSpeaker = icon("speaker2",150),rightSpeaker = icon("speaker2",150);
    ImageView lefti = icon("left",170),righti = icon("right",170);
    HBox cloaks = new HBox(0,lefti,righti);
    ProgressBar leftBar = new ProgressBar(0.5),rightBar = new ProgressBar(0.5);
    HBox bars = new HBox(0,leftBar,rightBar);
    StackPane show = new StackPane(bars,cloaks);
    Label text = new Label("Slide the green knob left or right\nto shift the balance\nThe rate of the speakers is affected by the balance\n");
    HBox speakers = new HBox(20,leftSpeaker,rightSpeaker);
    public static Slider balance = new Slider(0,1,0.5);
    VBox main = new VBox(10,title,speakers,balance,text);
    PulseTransition leftPulse = new PulseTransition(leftSpeaker);
    PulseTransition rightPulse = new PulseTransition(rightSpeaker);

    public Balance() {
        text.setTextAlignment(TextAlignment.CENTER);
        back.setOnMouseEntered(e->{back.setGraphic(icon("back3_pressed",30));});
        back.setOnMouseExited(e->{back.setGraphic(icon("back3",30));});
        back.getStyleClass().add("back");
        back.setOnAction(e->{
            RedSkull.pane.setPinnedSide(null);
        });
        
        rightSpeaker.setEffect(new Glow(0));
        leftSpeaker.setEffect(new Glow(0));
        
        leftPulse.setRate(4);
        leftPulse.setAutoReverse(true);
        leftPulse.setCycleCount(Timeline.INDEFINITE);
//        leftPulse.play();
        rightPulse.setRate(4);
        rightPulse.setAutoReverse(true);
        rightPulse.setCycleCount(Timeline.INDEFINITE);
//        rightPulse.play();
        
        HBox.setHgrow(balance, Priority.ALWAYS);
        VBox.setMargin(balance, new Insets(10,50,0,50));
        
        speakers.setAlignment(Pos.CENTER);
        main.setAlignment(Pos.TOP_CENTER);
        cloaks.setAlignment(Pos.CENTER);
        bars.setAlignment(Pos.CENTER);
        rightBar.setMinSize(170,170);
        leftBar.setMinSize(170,170);
        leftBar.setScaleX(-1);
        show.setMaxHeight(170);
        title.setStyle("-fx-text-fill: #808080; -fx-font-weight: bold");

        
        rightBar.progressProperty().addListener((obs,o,n)->{
                if(n.doubleValue()>o.doubleValue()){
                    leftPulse.setRate(5-(n.doubleValue()*5));
                }else{
                    leftPulse.setRate(5-(n.doubleValue()*5)*1.1);
                }
            System.out.println(leftPulse.getRate());
        });
        leftBar.progressProperty().addListener((obs,o,n)->{
            rightPulse.setRate(5-(n.doubleValue()*5));
        });
        
        balance.valueProperty().addListener((obs,o,n)->{
            System.out.println(n.doubleValue());
            if(n.doubleValue()>0.5){ //THE RIGHT
                leftBar.setProgress(0);
                leftSpeaker.setEffect(new Glow(0));
                rightBar.setProgress((n.doubleValue()));
                if(n.doubleValue()>o.doubleValue()){//when increase
                    rightSpeaker.setEffect(new Glow(((Glow)rightSpeaker.getEffect()).getLevel()+0.015));
                }else{                              //when decreased
                    rightSpeaker.setEffect(new Glow(((Glow)rightSpeaker.getEffect()).getLevel()-0.015));
                }
            }else{                  //THE LEFT
                rightBar.setProgress(0);
                rightSpeaker.setEffect(new Glow(0));
                leftBar.setProgress(1-(n.doubleValue()));
                if(n.doubleValue()<o.doubleValue()){//when increase
                    leftSpeaker.setEffect(new Glow(((Glow)leftSpeaker.getEffect()).getLevel()+0.015));
                }else{                              //when decreased
                    leftSpeaker.setEffect(new Glow(((Glow)leftSpeaker.getEffect()).getLevel()-0.015));
                }
            }
        });
        
        this.getStylesheets().add(getClass().getResource("css/equalizer.css").toExternalForm());
        this.getChildren().addAll(show,main);
        this.getStyleClass().add("bal");
        text.getStyleClass().add("label2");
        this.setPrefWidth(450);
        balance.getStyleClass().add("seeker");
        StackPane.setMargin(main, new Insets(0,0,0,0));
        StackPane.setMargin(show, new Insets(-130,0,0,0));
        StackPane.setAlignment(back, Pos.TOP_RIGHT);
    }
    
    public void noMedia(boolean media){
        if(media){
            leftPulse.play();
            rightPulse.play();
        }else{
            leftPulse.pause();
            rightPulse.pause();
        }
    }
    
     public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setPreserveRatio(true);
        image.setFitHeight(size);
        return image;
    }
    
    
}

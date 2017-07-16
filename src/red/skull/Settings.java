/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Monroe
 */
public class Settings extends StackPane{
    
    Button back = new Button(null,icon("back3",30));
    public static TabPane tabs;
    Equalizer equalizer;
    Balance balance;
    General gen;

    public Settings(Player player,Stage stage,Top top) {
        equalizer = player.getEqualizer();
        balance = player.getBalance();
        gen = new General(stage,top);
        
        Tab general = new Tab("General",gen);
        Tab eq = new Tab("Equalizer",equalizer);
        Tab bal = new Tab("Balance",balance);
        
        tabs = new TabPane(general,eq,bal);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        this.getChildren().addAll(tabs,back);
        this.getStylesheets().add(getClass().getResource("css/settings.css").toExternalForm());
        StackPane.setAlignment(back, Pos.TOP_RIGHT);
        
        back.getStyleClass().add("back");
        back.setOnAction(e->{
            RedSkull.pane.setPinnedSide(null);
        });
         back.setOnMouseEntered(e->{back.setGraphic(icon("back3_pressed",30));});
        back.setOnMouseExited(e->{back.setGraphic(icon("back3",30));});
    }
    
    
    
    
    
    
    
    
    public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setPreserveRatio(true);
        image.setFitHeight(size);
        return image;
    }
    
    
    
}

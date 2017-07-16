/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Monroe
 */
public class CyberSpace extends StackPane{
    
    Button back = new Button(null,icon("back3",30));
    TabPane tabs = new TabPane();
    Tab net,eqs;
    Browser browser;
    Network network;
    Player player;

    public CyberSpace(Player player,PlayLists playlists) {
        this.player = player;
        browser = new Browser(playlists);
        network = new Network(player);
        net = new Tab("Songs",browser);
        eqs = new Tab("Equalizers",network);
        
        
        tabs.getTabs().addAll(net,eqs);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
       
        
        this.getChildren().addAll(tabs,back);
        browser.setPrefSize(450, 400);
        network.setPrefSize(450, 400);
        this.getStyleClass().add("root");
        this.getStylesheets().add(getClass().getResource("css/windows.css").toExternalForm());
        back.getStyleClass().add("back");
        StackPane.setAlignment(back, Pos.TOP_RIGHT);
        
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

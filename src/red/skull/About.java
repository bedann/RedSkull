/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 *
 * @author Monroe
 */
public class About extends Stage{
    
    ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("images/preloader2.jpg")));

    public About() {
        logo.setFitHeight(200);
        logo.setPreserveRatio(true);
        
        Label name = new Label("The  Red  Skull");
        name.setStyle("-fx-text-fill: #4f4f4f");
        name.setFont(Font.loadFont(getClass().getResource("res/thunder.ttf").toExternalForm(), 18));
        
        Label info = new Label("Red Skull is trial project.\nThe aim of this project was to"
                + " test the media classes available in javafx.\n Updates to this software are still taking place and feedback is encouraged."
                + "\n");
        info.setAlignment(Pos.CENTER);
        info.setWrapText(true);
        info.setTextAlignment(TextAlignment.CENTER);
        
        VBox vb = new VBox(10,logo,info);
        vb.setPadding(new Insets(10));
        vb.setAlignment(Pos.CENTER);
        
        Scene scene = new Scene(vb);
        scene.getStylesheets().add(getClass().getResource("css/popup.css").toExternalForm());
        this.setScene(scene);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.setTitle("About!");
    }
    
    
    
}

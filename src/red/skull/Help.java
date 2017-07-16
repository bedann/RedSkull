/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Monroe
 */
public class Help extends Stage{
    
    GridPane grid = new GridPane();
    ObservableList<Label> shortCuts = FXCollections.observableArrayList();
    ObservableList<Label> scDescs = FXCollections.observableArrayList();

    public Help() {
        
        shortCuts.addAll(
                new Label("ENTER"),
                new Label("N"),
                new Label("P"),
                new Label("M"),
                new Label("V"),
                new Label("E"),
                new Label("B"),
                new Label("I"),
                new Label("S"),
                new Label("Ctrl + UP"),
                new Label("Ctrl + DOWN"),
                new Label("Ctrl + RIGHT"),
                new Label("Ctrl + LEFT")
        );
        scDescs.addAll(
                new Label("Pause or Play the media playback"),
                new Label("Play the next media"),
                new Label("Play the previous media"),
                new Label("Mute"),
                new Label("Show video if it exists"),
                new Label("Open equalizer settings"),
                new Label("Open Balance settings"),
                new Label("Open Network to import or share Eqs"),
                new Label("Take screenshot"),
                new Label("Increase volume"),
                new Label("Decrease volume"),
                new Label("Forward +5 seconds"),
                new Label("Rewind -5 seconds")
        );
        
        for(int i = 0; i<shortCuts.size(); i++){
            Label s = shortCuts.get(i);
            s.setStyle("-fx-text-fill: #808080; -fx-font-weight: bold");
            Label d = scDescs.get(i);
            d.setStyle("-fx-text-fill: #cecece;");
            grid.add(s, 0, i+2);
            grid.add(d, 1, i+2);
        }
        
        Label nb = new Label("NB: These shortcuts only work when media is playing");
        nb.setStyle("-fx-text-fill: red");
        grid.add(nb, 0, 0);
        GridPane.setColumnSpan(nb, 2);
        GridPane.setMargin(nb,new Insets(10));
        
        
        grid.add(new Separator(Orientation.HORIZONTAL), 0, 1,2,1);
        
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: #0b0b0b");
        Scene scne = new Scene(grid);
        scne.setFill(Color.BLACK);
        grid.setPadding(new Insets(20));
        this.setScene(scne);
        this.setResizable(false);
        this.setTitle("HELP");
    }    
    
    
}

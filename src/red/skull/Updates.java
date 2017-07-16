/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Monroe
 */
public class Updates extends Stage{
    
    Label curVer = new Label("Current Version:");
    Label latestVer = new Label("Latest Version:");
    Label cv = new Label("_");
    Label lv = new Label("waiting for network...");
    TextField url = new TextField("waiting for network...");
    Label featuresL = new Label("Latest Features:");
    Button update = new Button("UPDATE NOW");
    ListView<String> features = new ListView<>();
    GridPane grid = new GridPane();
    ProgressBar pb = new ProgressBar();
    Db db = new Db();
    Service dl;
    Item download;
    
    public Updates() {
        
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("images/icon1.png")));
        icon.setFitHeight(30);icon.setPreserveRatio(true);
        Label name = new Label("The  Red  Skull");
        name.setStyle("-fx-text-fill: #4f4f4f");
        name.setFont(Font.loadFont(getClass().getResource("res/thunder.ttf").toExternalForm(), 18));
        cv.getStyleClass().add("values");
        lv.getStyleClass().add("values");
        url.getStyleClass().add("url");
        url.setEditable(false);
        features.setMaxHeight(100);
                
        
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(5);
        grid.setPadding(new Insets(20));
        
        grid.add(new HBox(10,icon,name), 0, 0,2,1);
        grid.add(curVer, 0, 1);
        grid.add(latestVer, 0, 2);
        grid.add(cv, 1, 1);
        grid.add(lv, 1, 2);
        grid.add(featuresL, 0, 3);
        grid.add((features), 1, 3,3,4);
        grid.add(url, 0, 8,3,1);
        grid.add(update, 3, 8);
        grid.add(pb, 0, 9,8,1);
        
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setMinHeight(6);
        
        dl = new DownloadService(download,"C:/The Red Skull/The Red Skull","exe");
        dl.setOnSucceeded(e->{
            update.setDisable(false);
            try {
                Runtime.getRuntime().exec("cmd /c start C:/The Red Skull/The Red Skull/setup.exe");
            } catch (IOException ex) {
                Logger.getLogger(RedSkull.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        dl.setOnCancelled(e->{
            update.setDisable(false);
        });
        pb.progressProperty().bind(dl.progressProperty());
        pb.visibleProperty().bind(dl.runningProperty());
        
        update.setDisable(true);
        update.setVisible(false);
        
//        pb.setVisible(false);
        
        update.setOnAction(e->{
            dl.start();
            update.setDisable(true);
        });
        
        cv.setText("v "+db.getSetting("version"));
        url.deselect();
        
        this.showingProperty().addListener((obs,o,n)->{
            if(n){
                Db.fire.child("red_skull").child("updates").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot ds) {
                    Version v = ds.getValue(Version.class);
                    if(v != null){
                        String[] fs = v.getFeatures().split(",");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                url.setText(v.getUrl());
                                lv.setText("v "+v.getVersion()+" ("+v.getRelease()+")");
                                Double cur = Double.parseDouble(db.getSetting("version"));
                                Double nu = Double.parseDouble(v.getVersion());
                                if(cur<nu){
                                    update.setVisible(true);
                                    update.setDisable(false);
                                }
                                download = new Item("The Red skull","exe",v.getUrl());
                                features.getItems().clear();
                            }
                        });
                        for(String s:fs){
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    features.getItems().add(s);
                                }
                            });
                        }
                    }
                }
                @Override
                public void onCancelled(FirebaseError fe) {
                }
            });
        
            }
        });
        
        Scene scene = new Scene(grid);
        scene.getStylesheets().add(getClass().getResource("css/popup.css").toExternalForm());
        this.setScene(scene);
        getIcons().add(new Image(getClass().getResourceAsStream("images/icon1.png")));
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        this.setTitle("Version Control");
    }
    
    
    
    
}

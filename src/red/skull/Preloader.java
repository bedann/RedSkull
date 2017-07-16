/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Monroe
 */
public class Preloader extends Stage{

    Db db = new Db();
    ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("images/preloader2.jpg")));
    BooleanProperty loaded = new SimpleBooleanProperty(false);
    Label cur = new Label("Loading music files..."),loading = new Label("Loading music files...");
    ProgressBar progress = new ProgressBar();
    VBox vb = new VBox(5,loading,cur,progress);
    
    public Preloader() {
        logo.setFitHeight(200);
        logo.setFitWidth(350);
        vb.setAlignment(Pos.CENTER);
        progress.setPrefWidth(340);
        cur.setStyle("-fx-text-fill: orangered");
        loading.setStyle("-fx-text-fill: #808080");
        StackPane stack = new StackPane(logo,vb);
        StackPane.setMargin(vb, new Insets(120,0,0,0));
        StackPane.setAlignment(vb, Pos.BOTTOM_CENTER);
        
        Scene scene = new Scene(stack,350,200);
        scene.getStylesheets().add(getClass().getResource("css/main.css").toExternalForm());
        this.setScene(scene);
        this.initStyle(StageStyle.TRANSPARENT);
        this.getIcons().add(new Image(getClass().getResourceAsStream("images/icon1.png")));
    }
    
    public boolean load(){
        this.startService();
        return loaded.get();
    }
    
    
    
    
    public void startService(){
        Loader loader = new Loader();
        progress.progressProperty().bind(loader.progressProperty());
        loader.setOnSucceeded(e->{loaded.set(true);close();System.out.println(Top.searchItems.size());});
        loader.setOnFailed(e->{loaded.set(false);close();System.err.println("SERVICE FAILED ");});
        loader.setOnCancelled(e->{loaded.set(false);close();System.err.println("SERVICE CANCELLED ");});
        cur.textProperty().bind(loader.messageProperty());
        loader.restart();
        
    }
    
    
    
    
}

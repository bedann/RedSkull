/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.io.File;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Monroe
 */
public class NewItem extends Stage{
    
    Player player;
    File file;
    Button play = new Button("PLAY SONG");
    Label label = new Label(),name = new Label(),year = new Label(),album = new Label(),artist = new Label(),genre = new Label();
    ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/icon1.png")));
    MapChangeListener<String, Object> metaListener;
    Media audioMedia;

    public NewItem(Player player, File mfile) {
        this.player = player;
        this.file = mfile;
        
        
       
        name.setStyle("-fx-font-weight: bold");
        image.setFitHeight(150);
        image.setPreserveRatio(true);
        Reflection rf = new Reflection();
        rf.setFraction(0.2);
        rf.setBottomOpacity(0.05);
        rf.setTopOpacity(0.3);
        rf.setTopOffset(0);
        image.setEffect(rf);
        
        
        
        name.setWrapText(true);
       
        album.setStyle("-fx-text-fill: #808080");
        year.setStyle("-fx-text-fill: #808080");
        genre.setStyle("-fx-text-fill: #808080");
        artist.setStyle("-fx-text-fill: #cecece;");
        
        VBox vb = new VBox(10,image,name,new VBox(artist,album,year,genre),play);
        vb.setPadding(new Insets(20));
        vb.setMaxWidth(300);
        vb.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vb);
        scene.getStylesheets().add(getClass().getResource("css/popup.css").toExternalForm());
        this.setScene(scene);
        this.initModality(Modality.APPLICATION_MODAL);
        
        populate(mfile);
    }
    
    public void populate(File mfile){
        this.file = mfile;
        if(audioMedia != null && metaListener != null){
            audioMedia.getMetadata().removeListener(metaListener);
        }
        if(Player.isVideo(file)){
            this.setTitle("New video added");
        }else{
            this.setTitle("New song added");
        }
        name.setText(file.getName());
        
        metaListener = new MapChangeListener<String, Object>() {
        @Override
        public void onChanged(MapChangeListener.Change<? extends String, ? extends Object> ch) {
          if (ch.wasAdded()) {
            handleMetadata(ch.getKey(), ch.getValueAdded());
          }
        }
        };
        audioMedia = new Media(file.toURI().toString());
        audioMedia.getMetadata().addListener(metaListener);
        
        play.setOnAction(e->{
            for(Item item:PlayLists.items){
                if(item.getName().equals(file.getName())){
                    player.playRecent(item);
                    audioMedia.getMetadata().removeListener(metaListener);
                    close();
                    break;
                }
            }
        });
    }
    
    private void handleMetadata(String key, Object value) {
        if (key.equals("album")) {
          album.setText(value.toString());
        } else if (key.equals("artist")) {
          artist.setText(value.toString());
        } if (key.equals("title")) {
          name.setText(value.toString());
        } if (key.equals("year")) {
          year.setText(value.toString());
        } if (key.equals("image")) {
          image.setImage((Image)value);
        } if (key.equals("genre")) {
          genre.setText(value.toString());
        }
  }

    
    
    
    
}

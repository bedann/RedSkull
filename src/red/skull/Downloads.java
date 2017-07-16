/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.jfoenix.controls.JFXButton;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Monroe
 */
public class Downloads extends VBox{
    
    ListView<Item> list = new ListView();
    JFXButton back = new JFXButton("X");
    Label dl = new Label("Downloads");
    public static Map<String,Service> services = new HashMap<>();

    public Downloads() {
        super(5);
        list.setCellFactory((ListView<Item> l)->new Cell());
        list.setMinWidth(260);
        list.getStyleClass().add("root");
        back.setStyle("-fx-text-fill:#cecece");
        
        list.getItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                if(list.getItems().isEmpty()){
                    PlayLists.sidesPane.setPinnedSide(null);
                }
            }
        });
        list.setPlaceholder(new Label("No current downloads"));
        
        HBox space = new HBox();
        HBox h = new HBox(10,dl,space,back);
        h.setAlignment(Pos.CENTER);
        HBox.setHgrow(space, Priority.ALWAYS);
        this.getChildren().addAll(h,list);
        this.getStyleClass().add("root");
        this.getStylesheets().add(getClass().getResource("css/download.css").toExternalForm());
        back.setOnAction(e->{
            PlayLists.sidesPane.setPinnedSide(null);
        });
        
    }
    
    
    public void addDownload(Item url){
        list.getItems().add(url);
    }
    
    
    class Cell extends ListCell<Item>{
        ProgressBar progress = new ProgressBar();
        Label percent = new Label();
        Label name = new Label();
        Button close = new Button("X");
        HBox hb = new HBox(5,percent,progress,close);
        VBox vb = new VBox(-2,name,hb);
        String path;

        public Cell() {
            hb.setAlignment(Pos.CENTER);
            HBox.setHgrow(progress, Priority.ALWAYS);
            vb.setPadding(new Insets(5));
            name.setStyle("-fx-text-fill:#fff;-fx-font-size:12px");
            progress.setMinWidth(170);
            progress.setPrefHeight(5);
//            hb.setMaxWidth(200);
            close.setStyle("-fx-background-color: transparent;-fx-text-fill:cecece");
            percent.setStyle("-fx-text-fill:cecece");
        }

        
        
        @Override
        protected void updateItem(Item item, boolean empty) {
            super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
            if(!empty && item!=null){
                name.setText(item.getName());
                
                if(!services.containsKey(item.getName())){
                    Service dl = new DownloadService(item,new Db().getSetting("downloadDir"),item.getDesc());
                    progress.progressProperty().bind(dl.progressProperty());
                    percent.textProperty().bind(dl.messageProperty());
                    services.put(item.getName(),dl);
                    
                    dl.setOnSucceeded(e->{
                        list.getItems().remove(item);});
                    dl.start();
                    close.setOnAction(e->{
                        String path = new Db().getSetting("downloadDir");
                        path = path+"/"+item.getName()+".mp3";
                        new File(path).delete();
                        dl.cancel();
                    });
                }else{
                    Service dl = services.get(item.getName());
                    progress.progressProperty().bind(dl.progressProperty());
                    percent.textProperty().bind(dl.messageProperty());dl.setOnSucceeded(e->{
                        list.getItems().remove(item);});
                    close.setOnAction(e->{dl.cancel();
                        list.getItems().remove(item);});
                }
                setGraphic(vb);
            }else{
                setGraphic(null);
            }
        }
        
    }
    
    
    
    
    public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setPreserveRatio(true);
        image.setFitHeight(size);
        return image;
    }
    
    
}

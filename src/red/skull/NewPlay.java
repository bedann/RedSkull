/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.io.File;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import static red.skull.PlayLists.list;
import static red.skull.RedSkull.notify;

/**
 *
 * @author Monroe
 */
public class NewPlay extends StackPane{

    Wtext name = new Wtext("Playlist name"),desc = new Wtext("Description");
    Button save = new Button(null,icon("save1",50));
    Button clear = new Button("Clear list");
    Button browse = new Button(null,icon("browse1",30));
    Button back = new Button(null,icon("back3",30));
    ListView<Item> list = new ListView<>();
    HBox bns = new HBox(10,browse,save);
    Label place = new Label("Browse or drag music files here");
    VBox vb = new VBox(20,list,name,desc,bns);
    Db db;
    
    public NewPlay() {
        db = new Db();
        browse.setOnMouseEntered(e->{browse.setGraphic(icon("browse1_pressed",30));});
        browse.setOnMouseExited(e->{browse.setGraphic(icon("browse1",30));});
        save.setOnMouseEntered(e->{save.setGraphic(icon("save1_pressed",50));});
        save.setOnMouseExited(e->{save.setGraphic(icon("save1",50));});
        back.setOnMouseEntered(e->{back.setGraphic(icon("back3_pressed",30));});
        back.setOnMouseExited(e->{back.setGraphic(icon("back3",30));});
        clear.setOnMouseEntered(e->{clear.setStyle("-fx-background-color: #808080; -fx-text-fill: #ffffff");});
        clear.setOnMouseExited(e->{clear.setStyle("-fx-background-color: #101010; -fx-text-fill: #ffffff");});
        clear.setStyle("-fx-background-color: #101010; -fx-text-fill: #ffffff");
        back.setOnAction(e->{
            RedSkull.pane.setPinnedSide(null);
        });
        bns.setAlignment(Pos.CENTER);
        name.setPromptText("Playlist title");
        desc.setPromptText("Playlist description");
        place.setWrapText(true);
        place.setStyle("-fx-text-fill: #cecece");
        list.setMaxSize(290,200);
        list.setPlaceholder(place);
        list.getStyleClass().add("lists2");
        list.setCellFactory((ListView<Item> l)->new Cell());
        list.itemsProperty().addListener((obs,o,n)->{});
        
        clear.setOnAction(e->{list.getItems().clear();});
        
        browse.setOnAction(e->{
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Music files (*.mp3)", "*.mp3"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video files (*.mp4)", "*.mp4"));
            List<File> files = fc.showOpenMultipleDialog(null);
            if(files != null){
                for(File f: files){
                    list.getItems().add(new Item(f.getName(),f.length()+"",f.getAbsolutePath()));
                }
            }
        });
        
        save.setOnAction(e->{
            if(list.getItems().isEmpty()){
                RedSkull.notify.show(true, "List is empty");
            }else if(!name.getText().isEmpty()){
                save();
            }else{
                RedSkull.notify.show(true, "Please add a name");
            }
        });
        
        this.setOnDragOver(e->{
            Dragboard db = e.getDragboard();
            if(db.hasFiles()){
                e.acceptTransferModes(TransferMode.COPY);
            }else{
                e.consume();
            }
        });
        this.setOnDragDropped(e->{
            Dragboard db = e.getDragboard();
            boolean success = false;
            if(db.hasFiles()){
                success = true;
                for(File f: db.getFiles()){
                    if(f.getName().toLowerCase().endsWith(".mp3") || Player.isVideo(f)){
                        Item itm = new Item(f.getName(),f.length()+"",f.getAbsolutePath());
                        if(!list.getItems().contains(itm))list.getItems().add(itm);
                    }else{
                        notify.show(true, "I can only accept mp3 or video files");
                    }
                }
            }
            e.setDropCompleted(success);
            e.consume();
        });
        
        vb.setAlignment(Pos.TOP_CENTER);
        vb.setPadding(new Insets(30,50,0,50));
        this.setPrefHeight(500);
        this.getStylesheets().add(getClass().getResource("css/windows.css").toExternalForm());
        this.getChildren().addAll(vb,back,clear);
        this.getStyleClass().add("root");
        this.setPrefWidth(500);
        StackPane.setAlignment(back, Pos.TOP_RIGHT);
        StackPane.setAlignment(clear, Pos.TOP_CENTER);
        StackPane.setMargin(clear, new Insets(10,0,0,0));
    }
    
    public void save(){
        db.addPlayList(name.getText(), desc.getText(), list.getItems());
        list.getItems().clear();
        RedSkull.notify.show(false, name.getText()+" Saved!");
        name.clear();
        desc.clear();
        PlayLists.reload();
    }
    
    class Cell extends ListCell<Item>{
        HBox hb = new HBox(10);
        Label l = new Label();
        HBox sp = new HBox();
        Button remove = new Button(null,icon("remove1",20));
        public Cell() {
            HBox.setHgrow(sp, Priority.ALWAYS);
            l.setStyle("-fx-text-fill: #808080");
            l.setMaxWidth(210);
            l.setMinWidth(210);
            hb.setAlignment(Pos.CENTER);
            remove.setMaxSize(30, 30);
            hb.getChildren().addAll(l,remove);
        }
        
        @Override
        public void updateItem(Item item, boolean empty){
            super.updateItem(item, empty);
            if(item != null && !empty){
                hb.setMaxWidth(250);
                l.setText(item.getName());
                remove.setOnMouseClicked(e->{
                    list.getItems().remove(item);
                });
                setGraphic(hb);
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.awt.Desktop;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.HiddenSidesPane;
import static red.skull.Player.index;
import static red.skull.Player.index2;
import static red.skull.RedSkull.notify;
import static red.skull.Top.searchItems;

/**
 *
 * @author Monroe
 */
public class PlayLists extends StackPane{
    
    public static ObservableList<Item> selection = FXCollections.observableArrayList();
    public static ObservableList<Item> items = FXCollections.observableArrayList();
    public static ListView<Item> list = new ListView(items);
    public static GridView<String> albums = new GridView();
    public static BooleanProperty playSelected = new SimpleBooleanProperty(); //Whether tab2 is selected/ the playlists tab
    public static ObservableList<Item> playitems = FXCollections.observableArrayList();
    public static ListView<Item> playlist = new ListView(playitems);
    Button open = new Button(null,icon("open",25));
    Button save = new Button(null,icon("save3",25));
    Button reload = new Button(null,icon("back4",25));
    Button up = new Button(null,icon("up",15));
    Button down = new Button(null,icon("down",15));
    HBox sp = new HBox();
    public static Label title = new Label("Now playing"),title2 = new Label("Playlists");
    public static TabPane tabs = new TabPane();
    Tab tab1,tab2,tab3;
    HBox top =new HBox(0,title,sp,up,down,reload,save);
    private static FilenameFilter filter;
    private static File openFile = null;
    Player player;
    Db db;
    
    public static HiddenSidesPane sidesPane = new HiddenSidesPane();
    Downloads downloads;

    public PlayLists(Player player) {
        super();
        this.player = player;
        db = new Db();
        downloads = new Downloads();
        HBox.setHgrow(sp, Priority.ALWAYS);
        top.setAlignment(Pos.CENTER_RIGHT);
        top.setMaxSize(100, 25);
        items.addAll(Top.searchItems);
        try{title.setText("All songs ("+items.size()+")");}catch(Exception e){}
        filter = (File f, String name)->{return name.toLowerCase().endsWith(".mp3") || Player.isVideo(f);};
        
        up.getStyleClass().add("bns");
        open.getStyleClass().add("bns");
        open.setTooltip(new Tooltip("Open file location"));
        up.setTooltip(new Tooltip("Move up"));
        down.getStyleClass().add("bns");
        down.setTooltip(new Tooltip("Move down"));
        up.setOnAction(e->{
            System.out.println(index);
            Item item = tab2.isSelected()?playlist.getSelectionModel().getSelectedItem():list.getSelectionModel().getSelectedItem();
            int i = 0;
            if(item != null){
                i = tab2.isSelected()?playlist.getSelectionModel().getSelectedIndex():list.getSelectionModel().getSelectedIndex();
                if(tab2.isSelected() && i != 0){
                    playlist.getItems().remove(i);
                    playlist.getItems().add(i-1,item);
                    playlist.getSelectionModel().clearAndSelect(i-1);
                    if((i-1) == index2){
                        index2+=1;
                    }else if((i+1) == index2){
                        index2-=1;
                    }
                }else if(i != 0){
                    list.getItems().remove(i);
                    list.getItems().add(i-1,item);
                    list.getSelectionModel().clearAndSelect(i-1);
                    if((i-1) == index){
                        index+=1;
                    }else if((i+1) == index){
                        index-=1;
                    }
                }
            }
            System.out.println(index);
        });
        down.setOnAction(e->{
            Item item = tab2.isSelected()?playlist.getSelectionModel().getSelectedItem():list.getSelectionModel().getSelectedItem();
            int i = 0;
            if(item != null){
                i = tab2.isSelected()?playlist.getSelectionModel().getSelectedIndex():list.getSelectionModel().getSelectedIndex();
                if(tab2.isSelected() && i != playlist.getItems().size()-1){
                    playlist.getItems().remove(i);
                    playlist.getItems().add(i+1,item);
                    playlist.getSelectionModel().clearAndSelect(i+1);
                    if((i+1) == index2){
                        index2-=1;
                    }else if((i-1) == index2){
                        index2+=1;
                    }
                }else if(i !=  (list.getItems().size()-1)){
                    list.getItems().remove(i);
                    list.getItems().add(i+1,item);
                    list.getSelectionModel().clearAndSelect(i+1);
                    if((i+1) == index){
                        index-=1;
                    }else if((i-1) == index){
                        index+=1;
                    }
                }
            }
        });
        reload.getStyleClass().add("bns");
        save.setTooltip(new Tooltip("Save playlist"));
        reload.setTooltip(new Tooltip("Reload playlists"));
        reload.setOnAction(e->{
             if(tab2.isSelected()){
                 reload();
             }
        });
        save.getStyleClass().add("bns");
        save.setOnAction(e->{
              if(tab1.isSelected()){
                if(!selection.isEmpty()){
                    db.addPlayList(getName(selection), "", selection);
                    reload();
                }else if(!list.getItems().isEmpty()){
                    db.addPlayList(getName(list.getItems()), "", list.getItems());
                    reload();
                }else{
                    notify.show(true, "Nothing to save fam");
                }
              }else{
                  ObservableList<Item> items = playlist.getItems();
                  if(!items.isEmpty()){
                      if(items.get(0).getPath() != null){
                          String name = getPlayListName();
                          if(name != null){
                              db.updatePlayList(name, items);
                              reloadPlayList(name);
                          }else{
                              name = getName(items);
                              db.addPlayList(name, null, items);
                              title2.setText(name+" ("+items.size()+")");
                              reload();
                          }
                      }else{
                          System.out.println("Path is null");
                      }
                  }
              }
        });
        open.setOnAction(e->{
                Desktop ds = Desktop.getDesktop();
                try {
                    ds.open(new File(list.getSelectionModel().getSelectedItem().getPath()).getParentFile());
                } catch (IOException ex) {
                    Logger.getLogger(PlayLists.class.getName()).log(Level.SEVERE, null, ex);
                }
        });
        
        initLists();
        
        Button all = new Button(null,icon("knob",15));
        all.setTooltip(new Tooltip("Select all"));
        all.setOnAction(e->{
            if(list.getSelectionModel().getSelectedItem() == null){
                list.getSelectionModel().selectAll();
            }else{
                list.getSelectionModel().clearSelection();
            }
        });
        all.setStyle("-fx-background-color: null");
        Button pin = new Button(null,icon("pin",15));
        pin.setTooltip(new Tooltip("Continue from selected location"));
        pin.setOnAction(e->{
            if(list.getSelectionModel().getSelectedItem() != null){
                Player.index = list.getSelectionModel().getSelectedIndex();
            }else{
                notify.show(true, "Nothing selected");
            }
        });
        pin.setStyle("-fx-background-color: null");
        HBox tt = new HBox(title,all,pin,open);
        tt.setAlignment(Pos.CENTER);
        tab1 = new Tab("Songs",new VBox(tt,list));
        tab2 = new Tab("Playlists",new VBox(title2,playlist));
        tab3 = new Tab("Albums",new VBox(albums));
        playSelected.bind(tab2.selectedProperty());
        ((VBox)tab1.getContent()).setAlignment(Pos.CENTER);
        ((VBox)tab2.getContent()).setAlignment(Pos.CENTER);
        tabs.getTabs().addAll(tab1,tab2,tab3);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        
        sidesPane.setTriggerDistance(0);
        sidesPane.setContent(new VBox(tabs,top));
        sidesPane.setRight(downloads);
        
        
        this.getChildren().addAll(sidesPane);
        this.getStyleClass().add("root");
        this.getStylesheets().add(getClass().getResource("css/main.css").toExternalForm());
        StackPane.setMargin(tabs, new Insets(0,0,10,0));
        StackPane.setAlignment(top, Pos.TOP_RIGHT);
        down.setCursor(Cursor.HAND);
        save.setCursor(Cursor.HAND);
        all.setCursor(Cursor.HAND);
        open.setCursor(Cursor.HAND);
        up.setCursor(Cursor.HAND);
        pin.setCursor(Cursor.HAND);
        reload.setCursor(Cursor.HAND);
        
    }
    
    
    public void addDownload(Item url){
        if(url.getName() == null || url.getName().isEmpty())return;
        downloads.addDownload(url);
        sidesPane.setPinnedSide(Side.RIGHT);
    }
    
    
    public static void reload(){
        Db db = new Db();
        ObservableList<Item> items = db.getPlayListNames();
        title2.setText("Playlists ("+items.size()+")");
        playitems.clear();
        playitems.addAll(items);
        PlayLists.playlist.setItems(playitems);
        Player.index2 = 0;
    }
    
    public static void reloadPlayList(String name){
        Db db = new Db();
        ObservableList<Item> items = db.getPlayList(name);
        if(!items.isEmpty()){
            title2.setText(name+" ("+items.size()+")");
            playitems.clear();
            playitems.addAll(items);
            PlayLists.playlist.setItems(playitems);
            Player.index2 = 0;
        }
    }
    
    
    public void initLists(){
        title.setStyle("-fx-text-fill: #009688");
        title.setMaxWidth(260);
        title.setAlignment(Pos.CENTER);
        list.setMinWidth(260);
        title.setPadding(new Insets(5));
        list.itemsProperty().addListener((obs,o,n)->{
            title.setText(n.size()+" song"+(n.size() == 1?"":"s"));
        });
        open.setVisible(list.getSelectionModel().getSelectedItem() != null);
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{
            selection.setAll(list.getSelectionModel().getSelectedItems());
            open.setVisible(n != null);
        });
        
        list.getStyleClass().add("lists2");
        list.setCellFactory((ListView<Item> l)->new Cell());
        Label place= new Label("Load songs here\nOR\nDrag and drop items here");
        place.setWrapText(true);
        place.setTextAlignment(TextAlignment.CENTER);
        place.setPadding(new Insets(20));
        place.setAlignment(Pos.CENTER);
        list.setPlaceholder(place);
        
        title2.setStyle(title.getStyle());
        title2.setTextAlignment(TextAlignment.CENTER);
        title2.setPadding(title.getPadding());
        playlist.getStyleClass().add("lists2");
        playlist.setCellFactory((ListView<Item> l)->new Cell2());
        Label place2= new Label("Playlists will be displayed here\nYou can drop music files here\nand save them as a playlist");
        place2.setWrapText(true);
        place2.setPadding(new Insets(20));
        place2.setAlignment(Pos.CENTER);
        place2.setTextAlignment(TextAlignment.CENTER);
        playlist.setPlaceholder(place2);
        playitems.addAll(db.getPlayListNames());
        
        
        albums.setCellFactory(new Callback<GridView<String>, GridCell<String>>() {
            public GridCell<String> call(GridView<String> gridView) {
                return new AlbumCell();
            }
        });
        albums.setCellWidth(110);
        albums.setCellHeight(120);
        albums.setMaxWidth(260);
        albums.setMinWidth(260);
        albums.setHorizontalCellSpacing(5);
        albums.setVerticalCellSpacing(5);
//        albums.setItems(FXCollections.observableArrayList("Lil wayne","Eko dida","ds","sdsd","sdsfs","sdsfsdf"));
    }
    
    public static void open(File file){
        Db db = new Db();
        openFile = file;
        items.clear();
        PlayLists.list.setItems(PlayLists.items);
        Player.index = 0;
        boolean include = false,hasSubs = false, add_search = (db.getSetting("load").equals("false"));//search items were never added = true
        if(file.isDirectory()){
            for(File f: file.listFiles()){
                if(f.isDirectory()){
                    hasSubs = true;
                    include = inCludeSubFolders();
                    break;
                }
            }
            for(File f: file.listFiles(filter)){
                Item itm = new Item(f.getName(),f.length()+"",f.getAbsolutePath());
                if(!items.contains(itm))items.add(itm);
                if(add_search){searchItems.add(new Item(f.getName(),f.length()+"",f.getAbsolutePath()));}
            }
            if(include){
                searchInFolders(file,add_search);
            }
        }else{
        items.clear();
            if(file.getName().toLowerCase().endsWith(".mp3") || Player.isVideo(file)){
                Item itm = new Item(file.getName(),file.length()+"",file.getAbsolutePath());
                if(!items.contains(itm))items.add(itm);
                if(add_search){searchItems.add(new Item(file.getName(),file.length()+"",file.getAbsolutePath()));}
            }
        }
        title.setText(file.getName()+" ("+items.size()+")");
        list.refresh();
        if(items.size()<1){
            notify.show(true, "No music files in here");
        }
    }
    
    private static void searchInFolders(File file,boolean add_search){
        for(File f:file.listFiles()){
            if(f.isDirectory()){
                isDirectory(f,add_search);
            }
        }
    }
    
    private static void isDirectory(File f,boolean add_search){
        for(File m: f.listFiles(filter)){
            Item itm = new Item(m.getName(),m.length()+"",m.getAbsolutePath());
                if(!items.contains(itm))items.add(itm);
                if(add_search){searchItems.add(new Item(m.getName(),m.length()+"",m.getAbsolutePath()));}
        }
        for(File folder:f.listFiles()){
            if(folder.isDirectory()){
                isDirectory(folder,add_search);
            }
        }
    }
    
    
    class Cell extends ListCell<Item>{
        Label l = new Label();
        public Cell() {
            l.setStyle("-fx-text-fill: #8c8c8c;");
        }
        @Override
        public void updateItem(Item item, boolean empty){
            super.updateItem(item, empty);
            if(item != null && !empty){
                l.setMaxWidth(200);
                l.setText(item.getName());
                int i = list.getItems().indexOf(item);
                this.setOnMouseClicked(e->{
                     if(e.getClickCount()>1){
                         if(item.getPath() == null){
                            list.setItems(db.getPlayList(item.getName()));
                        }else{
                            index = i;
                            player.openSelected();
                        }
                     }
                });
                setGraphic(l);
                
                
                this.setOnDragDetected(e->{
                    Dragboard db = this.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent cb = new ClipboardContent();
                    cb.putFiles(Arrays.asList(new File(item.getPath())));
                    db.setContent(cb);
                    e.consume();
                });
                
            }else{
                setGraphic(null);
            }
        }
    }
    
    class Cell2 extends ListCell<Item>{

        Label l = new Label();
        Button close = new Button(null,icon("remove1",20));
        HBox sp = new HBox();
        HBox hb = new HBox(l,sp,close);
        public Cell2() {
            close.setStyle("-fx-background-color: null");
            HBox.setHgrow(sp, Priority.ALWAYS);
            hb.setAlignment(Pos.CENTER);
            hb.setMaxHeight(20);
            this.setMaxHeight(20);
            l.setStyle("-fx-text-fill: #8c8c8c");
        }
        
        @Override
        public void updateItem(Item item, boolean empty){
            super.updateItem(item, empty);
            if(item != null && !empty){
                l.setMaxWidth(190);
                l.setText(item.getName());
                int i = playlist.getItems().indexOf(item);
                close.setCursor(Cursor.HAND);
                close.setOnAction(e->{
                    if(item.getPath() == null){
                        try{db.deletePlayList(item.getName());}catch(Exception ehh){}
                        reload();
                    }else{
                        try{db.deleteSong(item.getDesc(), item.getPath());}catch(Exception he){}
                        playlist.getItems().remove(item);
                        playlist.refresh();
                        title2.setText(item.getDesc() +" ("+playlist.getItems().size()+")");
                    }
                });
                this.setGraphic(hb);
                this.setOnMouseClicked(e->{
                     if(e.getClickCount()>1){
                         if(item.getPath() == null){
                            reloadPlayList(item.getName());
                        }else{
                            Player.index2 = i;
                            player.openSelected();
                        }
                     }
                });
                if(item.getPath() == null){
                    l.setText(l.getText()+" ("+db.getPlayList(item.getName()).size()+")");
                }
            }else{
                this.setGraphic(null);
            }
        }
    }
    
     public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setPreserveRatio(true);
        image.setFitHeight(size);
        return image;
    }
    
     
    public String getPlayListName(){
        String name = null;
        for(Item item: playlist.getItems()){
            if(item.getDesc() != null && (item.getDesc().toLowerCase().matches("[a-z_]*"))){
                name = item.getDesc();
                break;
            }
        }
        return name;
    }
    
         String n = null;
     public String getName(ObservableList<Item> items){
         n = null;
         Label l = new Label();
                 l.setText("A title is required\nDouble click to remove item");
         Wtext name = new Wtext("");
         ListView<Item> list = new ListView<>(items);
         list.setCellFactory((ListView<Item> lk)->new Cell3(items));
         list.setMaxSize(200, 200);
         name.setPromptText("Enter Playlist title");
         VBox vb = new VBox(10,l,list,name);
         vb.setPadding(new Insets(20));
         Scene scene = new Scene(vb);
         scene.getStylesheets().add(getClass().getResource("css/popup.css").toExternalForm());
         Stage stage = new Stage();
         stage.setAlwaysOnTop(true);
         stage.setResizable(false);
         stage.setTitle("Add playlist");
         name.textProperty().addListener((obs,o,n)->{
             if(n != null){
                 stage.setTitle(n);
             }
         });
         name.setOnAction(e->{
             n = name.getText();
             stage.close();
         });
         stage.setScene(scene);
         stage.setOnCloseRequest(e->{
             e.consume();
             n = name.getText();
                 stage.close();
         });
         stage.showAndWait();
         return n;
     }
    
     
     static class Cell3 extends ListCell<Item>{
        ObservableList<Item> items;
        Label l = new Label();
        public Cell3(ObservableList<Item> items) {
            this.items = items;
            l.getStyleClass().remove("label");
        }
         
        @Override
        public void updateItem(Item item, boolean empty){
            super.updateItem(item, empty);
            if(item != null && !empty){
                l.setMaxWidth(150);
                l.setText(item.getName());
                this.setOnMouseClicked(e->{
                     if(e.getClickCount()>1){
                         items.remove(item);
                     }
                });
                setGraphic(l);
            }else{
                setGraphic(null);
            }
        }
    }
    
    private static  boolean include = false;
    public static boolean inCludeSubFolders(){
        Stage stage = new Stage();
         stage.setAlwaysOnTop(true);
        Label l = new Label("Include subfolders?");
        Button yes = new Button("Include");
        Button no = new Button("Ignore");
        HBox hb = new HBox(10,yes,no);
        hb.setAlignment(Pos.CENTER);
        VBox vb = new VBox(10,l,hb);
        vb.setPadding(new Insets(10,30,10,30));
        vb.setAlignment(Pos.CENTER);
        yes.setOnAction(e->{include = true;stage.close();});
        no.setOnAction(e->{include = false;stage.close();});
        Scene scene = new Scene(vb);
        stage.setScene(scene);
        stage.showAndWait();
        
        return include;
    }
    
    
}

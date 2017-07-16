/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static red.skull.PlayLists.list;
import static red.skull.PlayLists.playlist;
import static red.skull.PlayLists.reload;
import static red.skull.PlayLists.reloadPlayList;
import static red.skull.PlayLists.selection;
import static red.skull.PlayLists.tabs;
import static red.skull.PlayLists.title2;
import static red.skull.RedSkull.notify;

/**
 *
 * @author Monroe
 */
public class Top extends VBox{
    public static ObservableList<Item> searchItems = FXCollections.observableArrayList();
    public static ObservableList<Item> searchResults = FXCollections.observableArrayList();
    Button close,minimise;
    public static HBox space,main;
    Stage stage;
    public TextField search = new TextField();
    public static MenuBar menubar;
    RadioMenuItem loop,loop_cur,openShots,showVideo, onTop, fullscreen;
    Player player;
    Help mHelp;
    About about;
    Updates update;
    Db db;
    
    
    public Top(Stage stage,Player player) {
        super(5);
        this.player = player;
        db = new Db();
        this.stage = stage;
        update = new Updates();
        about  = new About();
        mHelp = new Help();
        main = new HBox(10);
        space = new HBox(search);
        search.setPromptText("Search music...");
        space.setAlignment(Pos.CENTER_RIGHT);
        menubar = new MenuBar();
        menubar.getStyleClass().add("menubar");
        HBox.setHgrow(space, Priority.ALWAYS);
        ////PLAYLISTS
//        MenuItem openPlaylist = new MenuItem("Open");
        MenuItem addNew = new MenuItem("Add new");
        MenuItem reload = new MenuItem("Reload Playlists");
        reload.setOnAction(e->{
            PlayLists.tabs.getSelectionModel().select(1);
            PlayLists.reload();});
        MenuItem reloadFile = new MenuItem("Reload Previous files");
        reloadFile.setOnAction(e->{
                PlayLists.tabs.getSelectionModel().select(0);
                PlayLists.list.setItems(PlayLists.items);
                Player.index = 0;});
        addNew.setOnAction(e->{RedSkull.pane.setPinnedSide(Side.TOP);});
        ////File
        MenuItem open = new MenuItem("Open single file");
        MenuItem openFolder = new MenuItem("Open directory");
        final Menu recent = new Menu("Recently played");
        MenuItem save = new MenuItem("Save playlist");
        save.setOnAction(e->{save();});
        MenuItem closeApp = new MenuItem("Close     ");
        closeApp.setOnAction(e->{stage.close();});
        //SETTINGS
        RadioMenuItem includeVideo = new RadioMenuItem("Include Videos in Search");
        includeVideo.setSelected(db.getSetting("includeVideo").equals("true"));
        includeVideo.selectedProperty().addListener((obs,o,n)->{
            db.saveSetting("includeVideo", n.toString());
            Tooltip  tip= new Tooltip("Needs a restart");
            tip.setAutoHide(true);
            tip.show(stage);
        });
        loop = new RadioMenuItem("Loop songs");
        loop.setSelected(db.getSetting("loop").equals("true"));
        loop.selectedProperty().addListener((obs, o, n)->{
            db.saveSetting("loop", n.toString());
            System.out.println(n.toString());
        });
        loop_cur = new RadioMenuItem("Loop current song");
        loop_cur.setSelected(db.getSetting("loop-curr").equals("true"));
        loop_cur.selectedProperty().addListener((obs, o, n)->{
            db.saveSetting("loop-curr", n.toString());
            System.out.println(n.toString());
        });
        MenuItem equalizer = new MenuItem("Equalizer");
        equalizer.setOnAction(e->{
            MediaPlayer mp = player.getMediaPlayer();
            if(mp != null){
                RedSkull.pane.setPinnedSide(Side.LEFT);
                Settings.tabs.getSelectionModel().select(1);
            }else{
                notify.show(false, "No media playing fam");
            }
        });
        MenuItem balance = new MenuItem("Balance");
        balance.setOnAction(e->{
            RedSkull.pane.setPinnedSide(Side.LEFT);
                Settings.tabs.getSelectionModel().select(2);
        });
        MenuItem more = new MenuItem("More...");
        more.setOnAction(e->{
            RedSkull.pane.setPinnedSide(Side.LEFT);
                Settings.tabs.getSelectionModel().select(0);
        });
       
        MenuItem takeShot = new MenuItem("Take screenshot");
        takeShot.setOnAction(e->{
            if(player.getVplayer() != null){
                player.getVplayer().screenShot();
            }else{
                notify.show(true, "A video must be playing");
            }
        });
        MenuItem viewShots = new MenuItem("View screenshots");
        viewShots.setOnAction(e->{
            Desktop ds = Desktop.getDesktop();
            File f = new File(db.getSetting("shotsDir"));
            if(!f.exists()){
                return;
            }
            try {
                ds.open(f);
            } catch (IOException ex) {
                Logger.getLogger(Top.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        MenuItem shotsDir = new MenuItem("Change directory");
        shotsDir.setOnAction(e->{
            String url = getShotsDir();
            if(url != null){
                db.saveSetting("shotsDir", url);
            }
        });
        openShots = new RadioMenuItem("Always open screenshots");
        openShots.setSelected(db.getSetting("openShots").equals("true"));
        openShots.selectedProperty().addListener((obs,o,n)->{
            db.saveSetting("openShots", n.toString());
        });
        Menu screenshots = new Menu("Screenshots",null,openShots,viewShots,shotsDir);
      
        ///MEDIA
        showVideo = new RadioMenuItem("Always show video");
        showVideo.setSelected(db.getSetting("video").equals("true"));
        fullscreen = new RadioMenuItem("Always full screen");
        fullscreen.setSelected(db.getSetting("fullscreen").equals("true"));
        showVideo.selectedProperty().addListener((obs,o,n)->{
            fullscreen.setDisable(!n);
            db.saveSetting("video", n.toString());
        });
        onTop = new RadioMenuItem("Always on top");
        onTop.setSelected(db.getSetting("onTop").equals("true"));
        onTop.selectedProperty().addListener((obs,o,n)->{
            db.saveSetting("onTop", n.toString());
            if(player.getVplayer() != null){
                player.getVplayer().setAlwaysOnTop(n);
            }
        });
        fullscreen.selectedProperty().addListener((obs,o,n)->{
            db.saveSetting("fullscreen", n.toString());
        });
        MenuItem showCurr = new MenuItem("Show current video");
        showCurr.setOnAction(e->{
            player.showVideo();
        });
        Menu video = new Menu("Video Player");
        video.getItems().addAll(showCurr,showVideo,fullscreen,onTop,takeShot);
        ///////////////HELP
        MenuItem showHelp = new MenuItem("Short cuts");
        showHelp.setOnAction(e->{
            mHelp.show();
        });
        MenuItem updates = new MenuItem("Updates");
        updates.setOnAction(e->{
            update.show();
        });
        MenuItem abt = new MenuItem("About");
        abt.setOnAction(e->{
            about.show();
        });
        
        MenuItem eqs = new MenuItem("Equalizers");
        eqs.setOnAction(e->{
            RedSkull.pane.setPinnedSide(Side.BOTTOM);
        });
        MenuItem songs = new MenuItem("Songs");
        songs.setOnAction(e->{
            RedSkull.pane.setPinnedSide(Side.BOTTOM);
        });
        
        Menu file = new Menu("_File");
        file.getItems().addAll(open,openFolder,recent,save,closeApp);
        Menu playlist = new Menu("_Playlists");
        playlist.getItems().addAll(addNew,reload,reloadFile);
        Menu media = new Menu("Media");
        Menu settings = new Menu("_Settings");
        settings.getItems().addAll(screenshots,media,new SeparatorMenuItem(),equalizer,balance,more);
        Menu audioSettings = new Menu("Audio Player",null,loop,loop_cur);
        media.getItems().addAll(video,audioSettings);
        Menu help = new Menu("Help");
        help.getItems().addAll(showHelp,updates,abt);
        Menu internet = new Menu("Internet");
        internet.getItems().addAll(songs,eqs);
        menubar.getMenus().addAll(file,playlist,settings,internet,help);
        
        
        
        file.showingProperty().addListener((obs,o,n)->{
            if(n){
                recent.getItems().clear();
                ObservableList<Item> recents = db.getRecents();
                System.out.println(recents.size()+" is the size");
                for(Item item:recents){
                    String name = item.getName();
                    MenuItem menu = new MenuItem(name.length()>40?name.substring(0,40)+"...":name);
                    menu.setOnAction(es->{
                        player.playRecent(item);
                    });
                    recent.getItems().add(menu);
                }
            }
        });
        
        
        
        openFolder.setOnAction(e->{
            DirectoryChooser dc = new DirectoryChooser();
            File f = dc.showDialog(stage);
            if(f != null){
                PlayLists.open(f);
            }
            PlayLists.tabs.getSelectionModel().select(0);
        });
        
        open.setOnAction(e->{
            FileChooser dc = new FileChooser();
                dc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Music files (*.mp3)", "*.mp3"),new FileChooser.ExtensionFilter("Video files (*.mp4)", "*.mp4"));
            List<File> files = dc.showOpenMultipleDialog(stage);
            if(files != null){
                for(File f: files){
                    PlayLists.open(f);
                }
            }
            PlayLists.tabs.getSelectionModel().select(0);
        });
        
        
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("images/icon1.png")));
        icon.setFitHeight(30);icon.setPreserveRatio(true);
        Label name = new Label("The  Red  Skull");
        name.setStyle("-fx-text-fill: #4f4f4f");
        name.setFont(Font.loadFont(getClass().getResource("res/thunder.ttf").toExternalForm(), 18));
                
        close = new Button();
        close.getStyleClass().add("close");
        close.setMaxSize(20, 20);
        close.setMinSize(20, 20);
        close.setEffect(new Glow(10));
        close.setOnAction(e->{
            File assoc = new File("assoc.bat");
            if(assoc.exists())assoc.delete();
            stage.close();
            System.exit(0);
        });
        
        minimise = new Button(null,icon("mini",20));
        minimise.getStyleClass().add("mini");
        minimise.setMaxSize(20, 20);
        minimise.setMinSize(20, 20);
        minimise.setEffect(new Glow(10));
        minimise.setOnAction(e->{
            stage.setIconified(true);
        });
        
        search.textProperty().addListener((obs,o,n)->{
            if(n.length()>0){
                PlayLists.tabs.getSelectionModel().select(0);
                Player.index = 0;
                searchResults.clear();
                for(Item item: searchItems){
                    if(item.getName().toLowerCase().contains(n.toLowerCase())){
                        searchResults.add(item);
                    }
                }
                PlayLists.list.setItems(searchResults);
                PlayLists.list.refresh();
            }else{
                PlayLists.list.setItems(PlayLists.items);
                PlayLists.list.refresh();
            }
        });
       
        
        main.getChildren().addAll(icon,name,space,minimise,close);
        main.setAlignment(Pos.CENTER);
        
        this.getChildren().addAll(main,menubar);
        this.setPadding(new Insets(5));
        this.getStyleClass().add("root");
        this.getStylesheets().add(getClass().getResource("css/main.css").toExternalForm());
        this.setAlignment(Pos.CENTER);
    }
    
    
    public void refresh(){
        loop.setSelected(db.getSetting("loop").equals("true"));
        loop_cur.setSelected(db.getSetting("loop-curr").equals("true"));
        onTop.setSelected(db.getSetting("onTop").equals("true"));
        this.openShots.setSelected(db.getSetting("openShots").equals("true"));
        showVideo.setSelected(db.getSetting("video").equals("true"));
        fullscreen.setSelected(db.getSetting("fullscreen").equals("true"));
    }

    public Player getPlayer() {
        return player;
    }
    
    
    
    
    public void showHelp(){
        mHelp.show();
    }
     
    
     public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setPreserveRatio(true);
        image.setFitHeight(size);
        return image;
    }
    
     
     public void save(){
         if(tabs.getSelectionModel().getSelectedIndex() == 0){
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
     }
     
     
     public static HBox getTop(){
         return main;
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
         list.setCellFactory((ListView<Item> lk)->new PlayLists.Cell3(items));
         list.setMaxSize(200, 200);
         name.setPromptText("Enter Playlist title");
         VBox vb = new VBox(10,l,list,name);
         vb.setPadding(new Insets(20));
         Scene scene = new Scene(vb);
         scene.getStylesheets().add(getClass().getResource("css/popup.css").toExternalForm());
         Stage stage = new Stage();
         stage.setResizable(false);
         stage.setAlwaysOnTop(true);
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
     
     
     public String getShotsDir(){
         String dir = null;
         DirectoryChooser dc = new DirectoryChooser();
         dc.setTitle("Screenshots directory");
         try{
             dir = dc.showDialog(stage).getAbsolutePath();
         }catch(Exception e){}
         return dir;
     }
     
     
     
    
}

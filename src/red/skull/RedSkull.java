/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.omt.lyrics.SearchLyrics;
import com.omt.lyrics.beans.Lyrics;
import com.omt.lyrics.beans.LyricsServiceBean;
import com.omt.lyrics.exception.SearchLyricsException;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.HiddenSidesPane;
import red.skull.FileListener.FileChanger;
import static red.skull.Top.searchItems;

/**
 *
 * @author Monroe
 */
public class RedSkull extends Application implements FileChanger {
    
    public static BorderPane root = new BorderPane();
    Top top;
    PlayLists playlists;
    Player player;
    Stage stage;
    public static Notify notify;
//    Equalizer equalizer;
    NewPlay newPlaylist;
    Balance balance;
//    Network net;
    CyberSpace cyber;
    public static HiddenSidesPane pane,main_pane;
    Preloader preloader;
    MiniPlayer mini;
    Db db = new Db();
    NewItem newItem;
    Settings settings;
    CircleMiniPlayer circle;
    BoxMiniPlayer box;
    MiniListener miniPlayers[] = new MiniListener[3];
    public static int player_index = 2;
//    FileChanger changer;
    public boolean inBrowser = false;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        db.createDb();
        preloader = new Preloader();
        preloader.show();
        if(db.getSetting("version").equals("2.1"))db.update();
        db.saveSetting("version", "2.2");
        player_index = Integer.parseInt(db.getSetting("miniPlayer"));
        notify = new Notify();
        balance = new Balance();
        circle = new CircleMiniPlayer(stage);
        mini = new MiniPlayer(stage);
        box = new BoxMiniPlayer(stage);
        miniPlayers[0] = mini;
        miniPlayers[1] = circle;
        miniPlayers[2] = box;
        player = new Player(balance,miniPlayers);
//        net = new Network(player);
        playlists = new PlayLists(player);
        cyber = new CyberSpace(player,playlists);
        main_pane = new HiddenSidesPane();
        pane = new HiddenSidesPane();
        top = new Top(stage,player);
        newPlaylist = new NewPlay();
        settings = new Settings(player,stage,top);
        
        
        
//        addListeners();  TODO  
        
        List<String> params = this.getParameters().getRaw();
        if(!params.isEmpty()){
            File f = new File(params.get(0));
            if(f.exists()){
                player.playLater(f, 5000);
            }
        }
        
        root.setRight(playlists);
        root.setTop(top);
        root.setCenter(notify);
        root.getStyleClass().add("bp");
        notify.setContent(pane);
        pane.setTriggerDistance(0);
        pane.setContent(player);
//        pane.setRight(equalizer);
        pane.setLeft(settings);
        pane.setTop(newPlaylist);
        pane.setBottom(cyber);
        main_pane.setContent(root);
        main_pane.setTriggerDistance(0);
        Scene scene = new Scene(main_pane, 700, 500);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("css/main.css").toExternalForm());
        stage.setTitle("Red Skull Media Player");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/icon1.png")));
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.showingProperty().addListener((obs,o,n)->{
            if(n)preloader.close();
        });
        
        stage.show();
        
        pane.pinnedSideProperty().addListener((obs,o,n)->{
            if(n == Side.LEFT){
                Equalizer.pmt.setVisible(player.getMediaPlayer() == null);
            }
            inBrowser = n == Side.BOTTOM;
        });
        
        scene.setOnDragOver(e->{
            Dragboard db = e.getDragboard();
            if(db.hasFiles()){
                e.acceptTransferModes(TransferMode.COPY);
            }else{
                e.consume();
            }
        });
        scene.setOnDragDropped(e->{
            Dragboard db = e.getDragboard();
            boolean success = false;
            if(db.hasFiles()){
                success = true;
                for(File f: db.getFiles()){
                    if(f.getName().toLowerCase().endsWith(".mp3") || Player.isVideo(f)){
                        if(PlayLists.tabs.getSelectionModel().getSelectedIndex() == 0){
                            PlayLists.list.getItems().add(new Item(f.getName(),f.length()+"",f.getAbsolutePath()));
                            PlayLists.list.scrollTo(PlayLists.list.getItems().size()-1);
                        }else{
                            if(!PlayLists.playlist.getItems().isEmpty()){
                                if(PlayLists.playlist.getItems().get(0).getPath() == null){
                                    notify.show(true, "Open a playlist to add the songs");
                                }else{
                                    PlayLists.playlist.getItems().add(new Item(f.getName(),f.length()+"",f.getAbsolutePath()));
                                }
                            }else{
                                PlayLists.playlist.getItems().add(new Item(f.getName(),f.length()+"",f.getAbsolutePath()));
                                PlayLists.playlist.scrollTo(PlayLists.playlist.getItems().size()-1);
                            }
                        }
                    }else if(f.isDirectory()){
                        PlayLists.open(f);
                        try{
                            PlayLists.list.scrollTo(PlayLists.list.getItems().size()-1);
                        }catch(Exception se){}
                    }else{
                        notify.show(true, "I can only accept mp3 and video files");
                    }
                }
            }
            e.setDropCompleted(success);
            e.consume();
        });
        
        root.setOnMousePressed(e->{
            initX = e.getScreenX()-stage.getX();
            initY = e.getScreenY() - stage.getY();
        });
        
        root.setOnMouseDragged(e->{
            stage.setX(e.getScreenX()-initX);
            stage.setY(e.getScreenY()-initY);
        });
        initHandlers(stage.getScene());
        
        boolean loaded = preloader.load(); 
        
        noSleep();
        
        stage.iconifiedProperty().addListener((obs,o,n)->{
            if(n){
                miniPlayers[player_index].show();
            }else{
                miniPlayers[player_index].close();
            }
        });
        
           File assoc = new File("assoc.bat");
        if(assoc.exists()){
            try {
                Runtime.getRuntime().exec("cmd /c start assoc.bat");
            } catch (IOException ex) {
                Logger.getLogger(RedSkull.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
        stage.setOnCloseRequest(e->{System.exit(0);});
       
        Timeline tm = new Timeline(new KeyFrame(Duration.millis(5000),e->{
//             playlists.addDownload("http://85.25.73.164/yt/f/3b/luis_fonsi_despacito_ft._daddy_yankee_mp3_35618.mp3");
        }));
        tm.setCycleCount(1);
//        tm.play();
        
    }
    private double initX = 0,initY = 0;
    
    public void noSleep(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(180000);
                        Point mouse = MouseInfo.getPointerInfo().getLocation();
                        Robot robot = new Robot();
                        robot.mouseMove(mouse.x, mouse.y);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RedSkull.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (AWTException ex) {
                        Logger.getLogger(RedSkull.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
    
    public void initHandlers(Scene scene){
        BooleanProperty ctrlPress = new SimpleBooleanProperty();
        BooleanProperty upPress = new SimpleBooleanProperty();
        BooleanProperty downPress = new SimpleBooleanProperty();
        BooleanProperty nextPress = new SimpleBooleanProperty();
        BooleanProperty prevPress = new SimpleBooleanProperty();
        upPress.addListener((obs,o,n)->{
            if(n && ctrlPress.get()){
                player.getVolume().setValue(player.getVolume().getValue()+5);
            }
        });
        downPress.addListener((obs,o,n)->{
            if(n && ctrlPress.get()){
                player.getVolume().setValue(player.getVolume().getValue()-5);
            }
        });
        ctrlPress.addListener((obs,o,n)->{
            player.getVolumes().setVisible(n);
        });
        nextPress.addListener((obs,o,n)->{
            if(n && ctrlPress.get()){
                Duration d = new Duration(player.getMediaPlayer().getCurrentTime().toMillis()+5000);
                player.getMediaPlayer().seek(d);
            }
        });
        prevPress.addListener((obs,o,n)->{
            if(n && ctrlPress.get()){
                Duration d = new Duration(player.getMediaPlayer().getCurrentTime().toMillis()-5000);
                player.getMediaPlayer().seek(d);
            }
        });
        EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event) {
                if(inBrowser)return;
                KeyCode key = event.getCode();
                Side s = pane.getPinnedSide();
                if(key == KeyCode.ENTER){
                    if(Player.playing.get() == true){
                        player.stopAudio();
                    }else{
                        player.startAudio();
                    }
                }else if(key == KeyCode.MINUS){
                    stage.setIconified(true);
                }else if(key == KeyCode.X){
                    stage.close();
                }
                if(player.getMediaPlayer() == null){
                    return;
                }
                if(key == KeyCode.N){
                    player.next();
                }else if(key == KeyCode.P){
                    player.previous();
                }else if(key == KeyCode.M){
                    player.getMediaPlayer().setMute(!player.getMediaPlayer().isMute());
                }else if(key == KeyCode.B){
                    pane.setPinnedSide(s== LEFT?null:Side.LEFT);
                }else if(key == KeyCode.E){
                    pane.setPinnedSide(s== RIGHT?null:Side.RIGHT);
                }else if(key == KeyCode.H){
                    top.showHelp();
                }else if(key == KeyCode.I){
                    pane.setPinnedSide(s == BOTTOM?null:Side.BOTTOM);
                }else if(key == KeyCode.V){
                    if(player.getVplayer() == null){
                        player.showVideo();
                    }else{
                        if(!player.getVplayer().isShowing()){
                            player.getVplayer().show();
                        }else{
                            player.getVplayer().close();
                        }
                    }
                }else if(key == KeyCode.CONTROL){
                    ctrlPress.set(true);
                    player.getVolumes().setVisible(true);
                    db.saveSetting("volume",player.getMediaPlayer().getVolume()+"");
                }else if(key == KeyCode.UP){
                    upPress.set(true);
                }else if(key == KeyCode.DOWN){
                    downPress.set(true);
                }else if(key == KeyCode.RIGHT){
                    nextPress.set(true);
                }else if(key == KeyCode.LEFT){
                    prevPress.set(true);
                }
                event.consume();
            }
        };
        EventHandler<KeyEvent> release = new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event) {
                if(inBrowser)return;
                KeyCode key = event.getCode();
                if(key == KeyCode.CONTROL){
                    try{
                        ctrlPress.set(false);
                        player.getVolumes().setVisible(false);
                        db.saveSetting("volume", player.getMediaPlayer().getVolume()+"");
                    }catch(Exception e){}
                }else if(key == KeyCode.UP){
                    upPress.set(false);
                }else if(key == KeyCode.DOWN){
                    downPress.set(false);
                }else if(key == KeyCode.RIGHT){
                    nextPress.set(false);
                }else if(key == KeyCode.LEFT){
                    prevPress.set(false);
                }
                event.consume();
            }
        };
        scene.setOnKeyPressed(handler);
        scene.setOnKeyReleased(release);
    }
    
    public void addListeners(){
        File[] roots = File.listRoots();
        for(int i = 0; i<roots.length;i++){
            File file = roots[i];
                new Thread(new Runnable() {
                @Override
                public void run() {
                    if(!file.getAbsolutePath().startsWith("C:")){
                        System.out.println("in the ohtr");
                        isDirectory(file);
                    }else{
                        isDirectory(new File(System.getProperty("user.home")));
                    }
                }
            }).start();
        }
    }
    
    public void isDirectory(File f){
        if(!f.getName().startsWith(".") && !f.getName().startsWith("$")){
            try {
                new FileListener(f.toPath(),RedSkull.this);
            } catch (IOException ex) {
                Logger.getLogger(RedSkull.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void deleteItem(final File file){
        for(Item item:PlayLists.items){
            if(item.getName().equals(file.getName())){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        PlayLists.items.remove(item);
                        Top.searchItems.remove(item);
                        RedSkull.notify.show(false, item.getName()+" has been deleted or removed");
                    }
                });
                break;
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        if(lockInstance("C:/red.txt")){
            showError();
        }
    }
    
    public static void showError(){
        Scene scene = new Scene(new Label("Error"));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    private static boolean lockInstance(final String lockFile) {
    try {
        final File file = new File(lockFile);
                        System.err.println("File exists>>"+file.exists());
        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        final FileLock fileLock = randomAccessFile.getChannel().tryLock();
        if (fileLock != null) {
                        System.err.println("File lock");
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        fileLock.release();
                        randomAccessFile.close();
                        file.delete();
                        System.err.println("File lock delete");
                    } catch (Exception e) {
                        System.err.println("Unable to remove lock file: " + lockFile+"LL"+e);
                    }
                }
            });
            return true;
        }
    } catch (Exception e) {
         System.err.println("Unable to create and/or lock file: " + lockFile+" >>> "+ e);
    }
    return false;
}
  
    @Override
    public void fileAdded(File newFile) {
        System.out.println(newFile.getAbsolutePath()+" has been added");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String name = newFile.getName();
                        if(Downloads.services.containsKey(name))return;
                        for(Item item:PlayLists.list.getItems()){
                            if(item.getName().equals(newFile.getName())){
                                return;
                            }
                        }
                        PlayLists.list.getItems().add(new Item(newFile.getName(),newFile.length()+"",newFile.getAbsolutePath()));
                        searchItems.add(new Item(newFile.getName(),newFile.length()+"",newFile.getAbsolutePath()));  
                        RedSkull.notify.show(false, newFile.getName()+" has been added");
                        
                        if(!db.getSetting("scan").equals("true"))return;
                        if(newItem == null){
                            newItem = new NewItem(player,newFile);
                            newItem.show();
                        }else{
                            if(!newItem.isShowing()){
                                newItem.populate(newFile);
                                newItem.show();
                            }
                        }
                    }
                });
    }

    @Override
    public void fileDeleted(File file) {
         deleteItem(file);
    }
    
}

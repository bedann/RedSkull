/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;

/**
 *
 * @author Monroe
 */
public class VPlayer extends Stage {
    
    Button close,minimise;
    Label vol = new Label();
    public static HBox space,main;
    private ProgressBar progress = new ProgressBar();
    Player player;
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    Db db = new Db();
    StackPane root = new StackPane();
    MediaView mv;
    
    public VPlayer(Player player, File file) {
        this.player = player;
        vol.setText(((int)(player.getMediaPlayer().getVolume()*100))+"%");
        mv = new MediaView(player.getMediaPlayer());
        
        mv.setFitHeight(bounds.getHeight()*0.8);
        mv.setFitWidth(bounds.getWidth()*0.75);
        mv.setPreserveRatio(true);
        
        this.heightProperty().addListener((obs,o,n)->{
            mv.setFitHeight(n.doubleValue());
        });
        this.widthProperty().addListener((obs,o,n)->{
            mv.setFitWidth(n.doubleValue());
            progress.setPrefWidth(n.doubleValue());
        });
        
        
        
        player.getMediaPlayer().currentTimeProperty().addListener((obs,o,n)->{
            progress.setProgress(1.0*(player.getMediaPlayer().getCurrentTime().toMillis()/player.getMediaPlayer().getTotalDuration().toMillis()));
        });
        
        
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("images/icon1.png")));
        icon.setFitHeight(30);icon.setPreserveRatio(true);
        Label name = new Label("The  Red  Skull");
        name.setStyle("-fx-text-fill: #4f4f4f");
        name.setFont(Font.loadFont(getClass().getResource("res/thunder.ttf").toExternalForm(), 18));
        vol.getStyleClass().add("vol");
        initControls();
        
        vol.setVisible(false);
        main.getChildren().addAll(icon,name,space,minimise,close);
        main.setPadding(new Insets(10));
        root.getChildren().addAll(mv,main,progress,vol);
        root.getStyleClass().add("window");
        StackPane.setAlignment(main, Pos.TOP_CENTER);
        StackPane.setAlignment(progress, Pos.BOTTOM_CENTER);
        StackPane.setMargin(progress, new Insets(20));
        root.setStyle("-fx-background-color: #0b0b0b");
        root.setOnMouseClicked(e->{
            if(e.getClickCount() > 1){
                this.setFullScreen(!this.isFullScreen());
            }
        });
        
        this.setTitle(file.getName());
        Scene scene = new Scene(root);
        scene.setCursor(Cursor.NONE);
        main.setCursor(Cursor.HAND);
        main.setMaxHeight(100);
        scene.getStylesheets().add(getClass().getResource("css/video.css").toExternalForm());
        scene.setFill(Color.BLACK);
        this.setScene(scene);
        this.setFullScreen(db.getSetting("fullscreen").equals("true"));
        this.setFullScreenExitHint("");
        this.setMinHeight(250);
        this.setMinWidth(300);
        this.getIcons().add(new Image(getClass().getResourceAsStream("images/icon1.png")));
        this.setOnCloseRequest(e->{
            db.saveSetting("volume", player.getMediaPlayer().getVolume()+"");
        });
        
        initHandlers();
    }
    
    public void initHandlers(){
        BooleanProperty ctrlPress = new SimpleBooleanProperty();
        BooleanProperty upPress = new SimpleBooleanProperty();
        BooleanProperty downPress = new SimpleBooleanProperty();
        BooleanProperty nextPress = new SimpleBooleanProperty();
        BooleanProperty prevPress = new SimpleBooleanProperty();
        upPress.addListener((obs,o,n)->{
            if(n && ctrlPress.get()){
                player.getVolume().setValue(player.getVolume().getValue()+5);
                vol.setText(((int)(player.getMediaPlayer().getVolume()*100))+"%");
            }
        });
        downPress.addListener((obs,o,n)->{
            if(n && ctrlPress.get()){
                player.getVolume().setValue(player.getVolume().getValue()-5);
                vol.setText(((int)(player.getMediaPlayer().getVolume()*100))+"%");
            }
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
                KeyCode key = event.getCode();
                if(key == KeyCode.N){
                    player.next();
                }else if(key == KeyCode.P){
                    player.previous();
                }else if(key == KeyCode.M){
                    player.getMediaPlayer().setMute(!player.getMediaPlayer().isMute());
                }else if(key == KeyCode.F){
                    setFullScreen(!isFullScreen());
                }else if(key == KeyCode.V){
                    close();
                }else if(key == KeyCode.CONTROL){
                    ctrlPress.set(true);
                    vol.setVisible(true);
                }else if(key == KeyCode.UP){
                    upPress.set(true);
                }else if(key == KeyCode.DOWN){
                    downPress.set(true);
                }else if(key == KeyCode.RIGHT){
                    nextPress.set(true);
                }else if(key == KeyCode.LEFT){
                    prevPress.set(true);
                }else if(key == KeyCode.ENTER){
                    if(Player.playing.get() == true){
                        player.stopAudio();
                    }else{
                        player.startAudio();
                    }
                }else if(key == KeyCode.S){
                    screenShot();
                }
                event.consume();
            }
        };
        EventHandler<KeyEvent> release = new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event) {
                KeyCode key = event.getCode();
                if(key == KeyCode.CONTROL){
                    ctrlPress.set(false);
                    vol.setVisible(false);
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
        this.getScene().setOnKeyPressed(handler);
        this.getScene().setOnKeyReleased(release);
        this.setAlwaysOnTop(db.getSetting("onTop").equals("true"));
    }
    
    
    public void initControls(){
        
        main = new HBox(10);
        space = new HBox();
        space.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(space, Priority.ALWAYS);
        close = new Button();
        close.getStyleClass().add("close");
        close.setMaxSize(20, 20);
        close.setMinSize(20, 20);
        close.setEffect(new Glow(10));
        close.setOnAction(e->{
            this.close();
        });
        
        minimise = new Button(null,icon("mini",20));
        minimise.getStyleClass().add("mini");
        minimise.setMaxSize(20, 20);
        minimise.setMinSize(20, 20);
        minimise.setEffect(new Glow(10));
        minimise.setOnAction(e->{
            this.setIconified(true);
        });
        minimise.visibleProperty().bind(this.fullScreenProperty());
        close.visibleProperty().bind(this.fullScreenProperty());
        
    }
    
    
    public void screenShot(){
        WritableImage image = mv.snapshot(new SnapshotParameters(), null);
        File redskull = new File(db.getSetting("shotsDir"));
        if(!redskull.exists()){
            redskull.mkdir();
        }
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("hh_mm_ss");
        String name ="Red skull("+sdf.format(date)+")";
        String path = redskull.getAbsolutePath()+"/"+name+".png";
        File file = new File(path);
        try{
           ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        }catch(Exception e){}
        if(db.getSetting("openShots").equals("true")){
            Desktop ds = Desktop.getDesktop();
            try {
                ds.open(file);
            } catch (IOException ex) {
                Logger.getLogger(VPlayer.class.getName()).log(Level.SEVERE, null, ex);
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

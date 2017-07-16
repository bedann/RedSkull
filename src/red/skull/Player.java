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
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import static red.skull.PlayLists.playSelected;
import static red.skull.PlayLists.tabs;
import static red.skull.RedSkull.notify;

public class Player extends VBox{
    public static final ObservableList<EqualizerBand> bands = FXCollections.observableArrayList();
    private XYChart.Data<String, Number>[] series1Data;
    private AudioSpectrumListener audioSpectrumListener;
    private MediaPlayer audioMediaPlayer;
    VPlayer vplayer;
    public static BooleanProperty playing = new SimpleBooleanProperty(false);
    public  BooleanProperty playPressed = new SimpleBooleanProperty(false);
    public  BooleanProperty nextPressed = new SimpleBooleanProperty(false);
    public  BooleanProperty prevPressed = new SimpleBooleanProperty(false);
    public  BooleanProperty equalized = new SimpleBooleanProperty(false);
//    String dancers[] = {"rider2.gif","rock.gif","rocker.gif","guitar2.gif","harley.gif"};
    String dancers[] = {"album.png"};
    Random r = new Random();
    Image dance = new Image(getClass().getResourceAsStream("images/album.png"));
    ImageView dancer = new ImageView(dance);
    ImageView v_bg = new ImageView(new Image(getClass().getResourceAsStream("images/volume4.png")));
    private static final boolean PLAY_AUDIO = Boolean.parseBoolean(
            System.getProperty("demo.play.audio", "true"));
    Timeline timer = new Timeline();

    Button play = new Button("",icon("play2",35));
    Button pause = new Button("",icon("pause1",40));
    Button next = new Button("",icon("next1",40));
    Button shuffle = new Button("",icon("shuffle",20));
    Button previous = new Button("",icon("previous1",40));
    Label curTime = new Label("0.00"), totalTime = new Label("0.00"),vol_perc = new Label("0"); 
    Label year = new Label(),artist = new Label(),album = new Label();
    VBox details = new VBox(5,artist,album,year);
    HBox controls = new HBox(0,curTime,play,pause,previous,next,shuffle,totalTime);
    Slider seeker = new Slider(),volume = new Slider(0,100,0),seeker2 = new Slider(0,100,0);
    public static int index = 0,index2 = 0;
    public static boolean loop = false,random_image = true;
    StackPane volumes = new StackPane();
    Db db;
    ProgressBar progress = new ProgressBar(),volumeBar = new ProgressBar();
    Item recentItem = null;
    ChangeListener<Duration> timeListener;
    MapChangeListener metaListener;
    public static File current = null;
    private BooleanProperty shuffle_on = new SimpleBooleanProperty(false);
   
    public static BarChart<String,Number> visuals;
    public static StackPane window = new StackPane();
    Equalizer equalizer;
    Balance balance;
//    MiniPlayer mini;
    MiniListener miniPlayers[];
    
    Label lyrics = new Label();
    ScrollPane lyricPane = new ScrollPane(lyrics);
    Button lyricPause = new Button(null,icon("pause-button",15));
    Map<String,String> reqs = new HashMap();
    private boolean searching = false,lyricing = false;
    Timeline lyricPlayer = new Timeline();
    StackPane fade = new StackPane();
    StackPane lyricStack = new StackPane(lyricPane,fade,lyricPause);
    
    public Player(Balance balance,MiniListener[] miniPlayers) {
        equalizer = new Equalizer(this);
        this.balance = balance;
        this.miniPlayers = miniPlayers;
        setInitMiniPlayer();
        db = new Db();
        bands.addAll(db.getSavedEQs(db.getSetting("last_eq")));
        loop = db.getSetting("loop").equals("true");
        for(Node n: controls.getChildren()){
            if(n instanceof Button){
                Button b = (Button)n;
                n.getStyleClass().add("controls");
            }
        }
        
        initChart();
        initControlActions();
        initVolume();
        
        audioSpectrumListener = (double timestamp, double duration, float[] magnitudes, float[] phases) -> {
            for (int i = 0; i < series1Data.length; i++) {
                series1Data[i].setYValue(magnitudes[i] + 60);
            }
        };
        
        playing.addListener((obs,o,n)->{
            balance.noMedia(n);
            miniPlayers[RedSkull.player_index].updateUi(n);
            if(n){
                if(!lyrics.getText().isEmpty())lyricPlayer.play();
            }else{
                lyricPlayer.stop();
            }
        });
        
        volume.setOrientation(Orientation.VERTICAL);
        volume.getStyleClass().add("volume");
        
        StackPane proslider = new StackPane(progress,seeker,seeker2);
        seeker.setVisible(false);
        proslider.setOnMouseEntered(e->{seeker.setVisible(true);seeker2.setVisible(true);});
        proslider.setOnMouseExited(e->{seeker.setVisible(false);seeker2.setVisible(false);});
        proslider.setMaxHeight(20);
        proslider.setPadding(new Insets(0,20,0,40));
        
        controls.setAlignment(Pos.TOP_CENTER);
        details.setMaxWidth(200);
        dancer.setFitHeight(200); dancer.setPreserveRatio(true);
        Reflection rf = new Reflection();
        rf.setFraction(0.2);
        rf.setBottomOpacity(0.05);
        rf.setTopOpacity(0.3);
        rf.setTopOffset(0);
        dancer.setEffect(rf);
        dancer.imageProperty().addListener((obs,o,n)->{
            if(n.getHeight()>n.getWidth()){
                dancer.setFitHeight(280);
            }else if(n.getWidth()> n.getHeight()){
                dancer.setFitWidth(300);
            }else{
                dancer.setFitHeight(250);
                dancer.setFitWidth(250);
            }
                dancer.setPreserveRatio(true);
        });
        
        lyrics.setAlignment(Pos.CENTER);
        lyrics.getStyleClass().add("lyrics");
        lyricPane.setFitToWidth(true);
        lyricPane.setPadding(new Insets(30));
        lyricPane.getStyleClass().add("lyricPane");
        fade.getStyleClass().add("fade");
        lyricStack.setVisible(false);
        
        lyricPause.setStyle("-fx-background-color:transparent");
        lyricPause.setOnAction(e->{
            if(lyricing){
                lyricPause.setGraphic(icon("play-arrow",15));
                lyricPlayer.pause();
            }else{
                lyricPause.setGraphic(icon("pause-button",15));
                lyricPlayer.play();
            }
            lyricing = !lyricing;
        });
        StackPane.setAlignment(lyricPause, Pos.TOP_RIGHT);
        StackPane.setMargin(lyricPause, new Insets(20));
        
        lyricPlayer.setCycleCount(Timeline.INDEFINITE);
        
        window.getChildren().addAll(dancer,details,volumes,visuals,volume,lyricStack,proslider);
        StackPane.setMargin(dancer, new Insets(10,10,0,10));
        StackPane.setMargin(details, new Insets(30,10,0,10));
        StackPane.setAlignment(details, Pos.TOP_LEFT);
        StackPane.setAlignment(volumes, Pos.CENTER_RIGHT);
        StackPane.setAlignment(proslider, Pos.BOTTOM_CENTER);
        seeker2.setPrefWidth(Double.MAX_VALUE);
        seeker.setPrefWidth(Double.MAX_VALUE);
        seeker.setTooltip(new Tooltip("Seek"));
        progress.setPrefWidth(Double.MAX_VALUE);
        progress.setMaxHeight(5);
        seeker.getStyleClass().add("seeker");
        seeker.setCursor(Cursor.HAND);
        seeker2.setCursor(Cursor.HAND);
        seeker2.getStyleClass().add("seeker2");
        seeker.setMax(100);
        
        progress.progressProperty().addListener((obs,o,n)->{
            seeker.setValue(n.doubleValue()*100);
        });
        seeker2.valueProperty().addListener((obs,o,n)->{
            if(audioMediaPlayer != null){
                Duration d = Duration.millis((n.doubleValue()*audioMediaPlayer.getTotalDuration().toMillis())/100);
                audioMediaPlayer.seek(d);
                if(!lyrics.getText().isEmpty()){
                    lyricPlayer.playFrom(d);
                }
            }
        });
        timeListener = ((obs,o,n)->{
            curTime.setText((time(n.toMinutes(),n.toSeconds())));
            progress.setProgress(1.0*(audioMediaPlayer.getCurrentTime().toMillis()/audioMediaPlayer.getTotalDuration().toMillis()));
            Duration m = audioMediaPlayer.getTotalDuration();
            totalTime.setText(time(m.toMinutes(),m.toSeconds()));
        });
        
        
        metaListener = new MapChangeListener<String, Object>() {
        @Override
        public void onChanged(Change<? extends String, ? extends Object> ch) {
          if (ch.wasAdded()) {
            handleMetadata(ch.getKey(), ch.getValueAdded());
          }
        }
        };
        
        initMini();
        playPressed.addListener((obs,o,n)->{
            if(n){
                startAudio();
            }else{
                stopAudio();
            }
        });
        prevPressed.addListener((obs,o,n)->{
            previous();
        });
        nextPressed.addListener((obs,o,n)->{
            next();
        });
        this.getChildren().addAll(window,controls);
        this.getStylesheets().add(getClass().getResource("css/main.css").toExternalForm());
    }
    
    private void handleMetadata(String key, Object value) {
        if (key.equals("album")) {
          album.setText(value.toString());
          reqs.put("album", value.toString());
        } else if (key.equals("artist")) {
          artist.setText(value.toString());
          reqs.put("artist", value.toString());
        } if (key.equals("title")) {
          reqs.put("song", value.toString());
          visuals.setTitle(value.toString());
          miniPlayers[RedSkull.player_index].setName(value.toString());
          miniPlayers[RedSkull.player_index].setTitle("Playing "+value.toString());
        } if (key.equals("year")) {
          year.setText(value.toString());
        } if (key.equals("image")) {
          dancer.setImage((Image)value);
          random_image = false;
        } if (key.equals("genre")) {
          System.out.println(value.toString());
        }
        this.getLyrics();
  }
    
    private String s = null;
    
    private void getLyrics(){
        if(reqs.size()>=2 && reqs.containsKey("song")){
            searching = true;
            s= db.getLyrics(reqs.get("song"));
            if(s != null){
                new Timeline(new KeyFrame(Duration.millis(500),e->{
                    this.loadLyrics();
                })).play();
                return;
            }
            
            System.err.println(reqs);
            SearchLyrics searchLyrics = new SearchLyrics();
            new Thread(()->{
                LyricsServiceBean bean = new LyricsServiceBean();
                bean.setSongName(reqs.get("song"));
                bean.setSongArtist(reqs.get("artist"));

                List<Lyrics> lyrics;
                try {
                    lyrics = searchLyrics.searchLyrics(bean);
                    for (Lyrics lyric : lyrics) {
                         s = lyric.getText().replaceAll("\n\n", "\n");
                    }
                    if(s !=null)db.saveLyrics(reqs.get("song"), s);
                 } catch (SearchLyricsException e) {
                 }
                
                Platform.runLater(()->{
                    if(s != null){
                        loadLyrics();
                    }
                });
            }).start();
        }else{
            searching = false;
        }
    }
    
    public void loadLyrics(){
        searching = false;
        this.lyrics.setText(s);
        KeyValue kv = new KeyValue(lyricPane.vvalueProperty(), lyricPane.getVmax());
        KeyFrame kf = new KeyFrame(audioMediaPlayer.getMedia().getDuration(), kv);
        lyricPlayer.getKeyFrames().setAll(kf);
        lyricPlayer.play();
        lyricing = true;
        lyricStack.setVisible(true);
    }
    

    public Equalizer getEqualizer() {
        return equalizer;
    }

    public Balance getBalance() {
        return balance;
    }
    
    
    
   public MediaPlayer getMediaPlayer(){
       return audioMediaPlayer;
   } 
    
    public String time(double mins,double millis){
        Double l = mins;
        return n2(mins)+":"+n((millis)-(l.intValue()*60));
    }
    
    public String n(double n){
        return new DecimalFormat("##").format(n);
    }
    
    public String n2(double n){
        return new DecimalFormat("####").format(n);
    }
    
    public void setInitMiniPlayer(){
        miniPlayers[RedSkull.player_index].setPlayer(this);
    }
    
    public void initVolume(){
        for(Node n:details.getChildren()){
            n.setStyle("-fx-text-fill: #808080");
        }
        vol_perc.setStyle("-fx-font-size: 20px; -fx-text-fill: orangered; -fx-padding: 0 0 0 30");
        v_bg.setFitWidth(50);
        v_bg.setFitHeight(300);
        volumeBar.setRotate(-90);
        volumeBar.setMinSize(300, 40);
        volumes.getChildren().addAll(volumeBar,v_bg,vol_perc);
        volumes.setMaxHeight(300);
        volume.setMinWidth(300);
        volume.setMaxHeight(300);
        volume.getStyleClass().add("volume");
        volumeBar.getStyleClass().add("volumebar");
        StackPane.setAlignment(volumeBar, Pos.CENTER_RIGHT);
        StackPane.setAlignment(v_bg, Pos.CENTER_RIGHT);
        StackPane.setAlignment(volume, Pos.CENTER_RIGHT);
        StackPane.setAlignment(vol_perc, Pos.TOP_RIGHT);
        StackPane.setMargin(volumeBar, new Insets(0,-127,0,0));
        volumes.setPadding(new Insets(0,10,0,0));
        
        volume.valueProperty().addListener((obs,o,n)->{
            volumeBar.setProgress(n.doubleValue()/100);
            vol_perc.setText(n.intValue()+"%");
            if(n.intValue()>89){
                if(n.intValue()>o.intValue()){
                    vol_perc.setScaleX(vol_perc.getScaleX()-0.02);
                    vol_perc.setScaleY(vol_perc.getScaleY()-0.02);
                    vol_perc.setTranslateY(vol_perc.getTranslateY()-2);
                }else{
                    vol_perc.setScaleX(vol_perc.getScaleX()+0.02);
                    vol_perc.setScaleY(vol_perc.getScaleY()+0.02);
                    vol_perc.setTranslateY(vol_perc.getTranslateY()+1.5);
                }
            }else{
                vol_perc.setScaleX(1f);
                vol_perc.setScaleY(1f);
                vol_perc.setTranslateY(1f);
            }
            if(audioMediaPlayer != null){
                audioMediaPlayer.setVolume(n.doubleValue()/100);
            }
        });
        this.setOnScroll(e->{
            double by = e.getDeltaY();
            double vol = volume.getValue();
            volume.setValue((vol+(by>0?+5:(vol>5?-5:0))));
        });
        volume.setOnMouseEntered(e->{volumes.setVisible(true);});
        volume.setOnMouseExited(e->{
            volumes.setVisible(false);
            if(audioMediaPlayer!=null){db.saveSetting("volume", audioMediaPlayer.getVolume()+"");}
        });
        volumes.setVisible(false);
    }

    public void initMini(){
        volume.valueProperty().unbind();
        progress.progressProperty().unbind();
        dancer.imageProperty().unbind();
        curTime.textProperty().unbind();
        totalTime.textProperty().unbind();
        playPressed.unbind();
        nextPressed.unbind();
        prevPressed.unbind();
        miniPlayers[RedSkull.player_index].bindVolume(volume);
        miniPlayers[RedSkull.player_index].bindSeeker(progress.progressProperty());
        miniPlayers[RedSkull.player_index].bindImage(dancer);
        miniPlayers[RedSkull.player_index].bindTimers(curTime, totalTime);
        miniPlayers[RedSkull.player_index].bindPlayButton(playPressed);
        miniPlayers[RedSkull.player_index].bindNextButton(nextPressed);
        miniPlayers[RedSkull.player_index].bindPrevButton(prevPressed);
        playing.set(playing.get());
        if(current != null && current.exists()){
            miniPlayers[RedSkull.player_index].setName(current.getName());
            miniPlayers[RedSkull.player_index].setTitle("Playing "+current.getName());
        }
        miniPlayers[RedSkull.player_index].updateUi(playing.get());
        
        if(RedSkull.player_index==1)miniPlayers[RedSkull.player_index].setImage(dancer.getImage());
    }
    
    public StackPane getVolumes() {
        return volumes;
    }
    
    

    public Slider getVolume() {
        return volume;
    }

    public VPlayer getVplayer() {
        return vplayer;
    }

    public Slider getSeeker() {
        return seeker2;
    }
    
    
    
    
    public void initControlActions(){
        HBox.setMargin(curTime, new Insets(0,40,0,0));
        HBox.setMargin(totalTime, new Insets(0,0,0,40));
        HBox.setMargin(shuffle, new Insets(10,0,0,0));
        curTime.setStyle("-fx-text-fill: red");
        totalTime.setStyle(curTime.getStyle());
        play.setOnMouseEntered(e->{play.setGraphic(icon("play2_pressed",35));});
        play.setOnMouseExited(e->{play.setGraphic(icon("play2",35));});
        pause.setOnMouseEntered(e->{pause.setGraphic(icon("pause1_pressed",40));});
        pause.setOnMouseExited(e->{pause.setGraphic(icon("pause1",40));});
        next.setOnMouseEntered(e->{next.setGraphic(icon("next1_pressed",40));});
        next.setOnMouseExited(e->{next.setGraphic(icon("next1",40));});
        previous.setOnMouseEntered(e->{previous.setGraphic(icon("previous1_pressed",40));});
        previous.setOnMouseExited(e->{previous.setGraphic(icon("previous1",40));});
        play.setOnAction(e->{
            startAudio();
            animate(play);
        });
        play.setTooltip(new Tooltip("Play"));
        pause.setOnAction(e->{
            stopAudio();
            animate(pause);
        });
        pause.setTooltip(new Tooltip("Pause"));
        next.setOnAction(e->{
            next();
            animate(next);
        });
        next.setTooltip(new Tooltip("Next"));
        previous.setOnAction(e->{
            previous();
            animate(previous);
        });
        previous.setTooltip(new Tooltip("Previous"));
        shuffle.setOnAction(e->{
            shuffle_on.set(!shuffle_on.get());
        });
        shuffle_on.addListener((obs,o,n)->{
            shuffle.setGraphic(icon(n?"shuffle_on":"shuffle",20));
        });
        shuffle_on.set(db.getSetting("shuffle").equals("true"));
    }
    
    public void animate(Node n){
        ScaleTransition scale = new ScaleTransition(Duration.millis(2000),n);
        scale.setFromX(0.2f);
        scale.setFromY(0.2f);
        scale.setToX(1f);
        scale.setToY(1f);
        scale.setInterpolator(new Bounce(0.1,10));
        scale.play();
    }
    
    public void hideDancer(){
        dancer.setVisible(!dancer.isVisible());
    }
    
    
    public void initChart(){
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis(0, 50, 10);
        visuals = new <String,Number>BarChart(xAxis,yAxis);
        visuals.setAnimated(false);
        visuals.setBarGap(0);
        visuals.setLegendVisible(false);
        visuals.getYAxis().setOpacity(0);
        visuals.getYAxis().setTickLabelsVisible(false);
        visuals.getXAxis().setOpacity(0);
        visuals.getXAxis().setTickLabelsVisible(false);
        visuals.setHorizontalGridLinesVisible(false);
        visuals.setVerticalGridLinesVisible(false);
        visuals.setTitle("...");
        visuals.getStylesheets().add(getClass().getResource("css/visuals.css").toExternalForm());
        
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();

        //set init data
        series1Data = new XYChart.Data[128];
        String[] categories = new String[128];
        for (int i = 0; i < series1Data.length; i++) {
            categories[i] = Integer.toString(i + 1);
            series1Data[i] = new XYChart.Data<String, Number>(categories[i], 0);
            series1.getData().add(series1Data[i]);
        }
        visuals.getData().add(series1);
        
        
    }
    
    
    public void openSelected(){
            if(audioMediaPlayer != null){
                audioMediaPlayer.stop();
                audioMediaPlayer.getMedia().getMetadata().removeListener(metaListener);
                audioMediaPlayer.currentTimeProperty().removeListener(timeListener);
                audioMediaPlayer = null;
            }
            startAudio();
    }
    
    public void playRecent(Item item){
        PlayLists.tabs.getSelectionModel().select(0);
        if(audioMediaPlayer != null){
            audioMediaPlayer.stop();
            audioMediaPlayer.getMedia().getMetadata().removeListener(metaListener);
            audioMediaPlayer.currentTimeProperty().removeListener(timeListener);
            audioMediaPlayer = null;
        }
        recentItem = item;
//        showError("starting audio");
        startAudio();
        new Thread(new Runnable(){
            @Override
            public void run() {
               for(int i = 0; i<PlayLists.list.getItems().size();i++){
                   Item listItem = PlayLists.list.getItems().get(i);
                   if(item.getName().equals(listItem.getName())){
                       index = i;
                       Platform.runLater(new Runnable() {
                           @Override
                           public void run() {
                               PlayLists.list.getSelectionModel().clearAndSelect(index);
                               PlayLists.list.scrollTo(index);
                           }
                       });
                       return;
                   }
               }
            }
        }).start();
    }
 
    
    public void playLater(File f,double millis){
        if(!f.exists())return;
        recentItem = new Item(f.getName(),f.getParent(),f.getAbsolutePath());
        Timeline tm = new Timeline(new KeyFrame(Duration.millis(millis),e->{
            playRecent(recentItem);
        }));
        tm.setCycleCount(1);
        tm.play();
    }
    
    
    public void next(){
        if(audioMediaPlayer != null){
            audioMediaPlayer.stop();
            audioMediaPlayer.getMedia().getMetadata().removeListener(metaListener);
            audioMediaPlayer.currentTimeProperty().removeListener(timeListener);
            audioMediaPlayer = null;
            if(PlayLists.tabs.getSelectionModel().getSelectedIndex() == 0){
                if(index < PlayLists.list.getItems().size()-1){
                     index++;
                }else{
                    index = 0;
                }
                if(shuffle_on.get())index = r.nextInt(PlayLists.list.getItems().size()-1);
            }else{
                if(index2 < PlayLists.playlist.getItems().size()-1){
                    index2++;
                }else{
                    index2 = 0;
                }
                if(shuffle_on.get())index2 = r.nextInt(PlayLists.playlist.getItems().size()-1);
            }
            startAudio();
        }
    }
    
    
    public void previous(){
        if(audioMediaPlayer != null){
            audioMediaPlayer.stop();
            audioMediaPlayer.getMedia().getMetadata().removeListener(metaListener);
            audioMediaPlayer.currentTimeProperty().removeListener(timeListener);
            audioMediaPlayer = null;
            if(PlayLists.tabs.getSelectionModel().getSelectedIndex() == 0){
                if(index != 0){
                    index--;
                }else{
                    index = PlayLists.list.getItems().size()-1;
                }
            }else{
                if(index2 != 0){
                    index2--;
                }else{
                    index2 = PlayLists.playlist.getItems().size()-1;
                }
            }
            startAudio();
        }
    }
    
    public void startAudio() {
        System.out.println(equalized.get());
        if(hasFiles() || recentItem != null){
            if (PLAY_AUDIO) {
                try{
                    changeSelection();
                }catch(Exception e){}
                if(getAudioMediaPlayer().getAudioSpectrumListener() == null){
                    getAudioMediaPlayer()
                        .setAudioSpectrumListener(audioSpectrumListener);
                }
                getAudioMediaPlayer().play();
                
                if(!equalized.get()){
                    equalized.set(true);
                    equalizer.reCalibrate(audioMediaPlayer,bands);
                }
                playing.set(true);
                playPressed.set(true);
            }
        }
    }
    
    public boolean hasFiles(){
        boolean has = false;
        if(tabs.getSelectionModel().isSelected(0)){
            if(!PlayLists.list.getItems().isEmpty()){
                has = true;
            }
        }else{
            if(!PlayLists.playlist.getItems().isEmpty()){
                has = true;
            }
        }
        return has;
    }
 
    public void changeSelection(){
        if(tabs.getSelectionModel().getSelectedIndex() == 0){
            try{
                PlayLists.list.getSelectionModel().clearAndSelect(index);
                PlayLists.list.scrollTo(index);
            }catch(Exception E){}
        }else{
            try{
                PlayLists.playlist.getSelectionModel().clearAndSelect(index2);
                PlayLists.playlist.scrollTo(index2);
            }catch(Exception E){}
            
        }
    }
    
    public void stopAudio() {
       if(hasFiles()){
           if (getAudioMediaPlayer().getAudioSpectrumListener() == audioSpectrumListener) {
               getAudioMediaPlayer().pause();
               playing.set(false);
               playPressed.set(false);
           }
       }
       
    }
    
    public void showVideo(){
        if(current != null){
            if(isVideo(current)){
                vplayer = new VPlayer(this,current);
                vplayer.show();
            }else{
                notify.show(true, "No video for this media");
            }
        }else{
            notify.show(true, "No file detected");
        }
    }
    
    private MediaPlayer getAudioMediaPlayer() {
        if (audioMediaPlayer == null && (hasFiles() || recentItem != null)) {
            if(!lyrics.getText().isEmpty()){
                lyrics.setText("");
                lyricPlayer.stop();
                lyricing = false;
            }
            lyricStack.setVisible(false);
            equalized.set(false);
            if(index<0){index = 0;}
            year.setText("");
            album.setText("");
            artist.setText("");
            dance = new Image(getClass().getResourceAsStream("images/"+dancers[r.nextInt(dancers.length)]));
            dancer.setImage(dance);
            random_image = true;
            Item item = recentItem;
                if(item == null)item = playSelected.get()?PlayLists.playlist.getItems().get(index2):PlayLists.list.getItems().get(index);
            int last_index = playSelected.get()?PlayLists.playlist.getItems().size()-1:PlayLists.list.getItems().size()-1;
            String path = item.getPath();
            if(path == null){
                ObservableList<Item> items=  db.getPlayList(item.getName());
                path = items.get(0).getPath();
                PlayLists.playlist.setItems(items);
                PlayLists.title2.setText(item.getName()+" ("+items.size()+")");
            }
            File f = new File(path);
            if(!f.exists()){
                return null;
            }else{
                current = f;
            }
            db.updateRecent(path, f.getName());
            RedSkull.notify.show(false, "Playing "+f.getName());
            visuals.setTitle(f.getName());
            miniPlayers[RedSkull.player_index].setName(f.getName());
            miniPlayers[RedSkull.player_index].setTitle("Playing "+f.getName());
            Media audioMedia = new Media(f.toURI().toString());
            audioMediaPlayer = new MediaPlayer(audioMedia);
            audioMediaPlayer.currentTimeProperty().addListener(timeListener);
            audioMediaPlayer.setVolume(Double.valueOf(db.getSetting("volume")));
            volume.setValue(audioMediaPlayer.getVolume()*100);
            reqs.clear();
            audioMedia.getMetadata().addListener(metaListener);
            
            Balance.balance.setValue(audioMediaPlayer.getBalance());
            Balance.balance.valueProperty().unbind();
            Balance.balance.valueProperty().bindBidirectional(audioMediaPlayer.balanceProperty());
            
            if(vplayer != null){
                vplayer.close();
            }
            vplayer = null;
            if(isVideo(f) && db.getSetting("video").equals("true")){
                vplayer = new VPlayer(this,f);
                vplayer.show();
            }
            
            audioMediaPlayer.setOnEndOfMedia(new Runnable(){
                @Override
                public void run() {
                    equalized.set(false);
                    if(vplayer != null){
                        vplayer.close();
                    }
                    if(db.getSetting("loop-curr").equals("true")){
                        openSelected();
                    }else if(!db.getSetting("loop").equals("true")){//when value is false
                        if(index != last_index){
                            next();
                        }
                    }else{
                        next();
                    }
                }
            });
            audioMediaPlayer.setOnError(new Runnable(){
                @Override
                public void run() {
                    notify.show(true,"Damn! Sumn wrong with that file");
                            next();
                }
            });
            recentItem = null;
        }
        return audioMediaPlayer;
    }
    
    
    public static boolean isVideo(File f){
        String name = f.getName().toLowerCase();
        return name.endsWith(".mp4") || name.endsWith(".wmv") || name.endsWith(".flv") || name.endsWith(".m4a") || name.endsWith(".wav");
    }
    
    public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setFitHeight(size);
        image.setPreserveRatio(true);
        return image;
    }
    
    
     public  void showError(String text){
        Scene scene = new Scene(new Label(text));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import static javafx.concurrent.Worker.State.FAILED;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author Monroe
 */
public class Browser extends StackPane{
    
     public static final String EVENT_TYPE_CLICK = "click";
    public static final String EVENT_TYPE_MOUSEOVER = "mouseover";
    public static final String EVENT_TYPE_MOUSEOUT = "mouseclick";
    
    JFXButton download = new JFXButton();
    JFXButton cancel = new JFXButton(null,icon("refresh",30));
    JFXButton back = new JFXButton(null,icon("broback",30));
    JFXButton foward = new JFXButton(null,icon("forward",30));
    JFXButton refresh = new JFXButton("Refresh",icon("refresh",100));
    StackPane refreshH = new StackPane(refresh);
    VBox vb = new VBox();
    StringProperty url = new SimpleStringProperty(null);
    PlayLists playlists;
    static WebEngine engine;
    ProgressIndicator pi = new ProgressIndicator();
    Label dlPrompt = new Label("Download media",icon("bottom-left-arrow",100));
    Label bored = new Label("Nothing to show",icon("bored",200));
    
    WebHistory history;

    public Browser(PlayLists playlists) {
        this.playlists = playlists;
        download.setGraphic(icon("download2",40));
        download.setVisible(false);
        bored.setVisible(false);
        refreshH.setVisible(false);
        download.setOnAction(e->{
            if(url.get() != null){
                Item name = getName();
                Item item = new Item(name.getName(),name.getDesc(),url.get());
                playlists.addDownload(item);
            }
        });
        url.addListener((obs,o,n)->{
            download.setVisible(n!=null);
        });
        
        WebView web = new WebView();
//        web.setStyle("");
        engine = web.getEngine();
        history=engine.getHistory();
        engine.setUserStyleSheetLocation(getClass().getResource("css/windows.css").toString());
        engine.load(new Db().getSetting("downloadLink"));
        
        
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue ov, State oldState, State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                pi.setVisible(false);
                url.set(null);
                    EventListener listener = (Event ev) -> {
                        String domEventType = ev.getType();
                        //System.err.println("EventType: " + domEventType);
                        if (domEventType.equals(EVENT_TYPE_CLICK)) {
                            pi.setVisible(true);
                            String href = ((Element)ev.getTarget()).getAttribute("href");
                            try{
                                Media m = new Media(href);
                                System.err.println(m.getMetadata().get("title"));
                                System.out.println("MEDIA FOUND");
                                download.setVisible(true);
                                url.set(href);
                            }catch(Exception e){
                                download.setVisible(false);
                                System.out.println("Not media");
                            }
                            
                            System.err.println(href);
                            //////////////////////
                            // here do what you want with that clicked event
                            // and the content of href
                            //////////////////////
                        }
                    };
 
                    Document doc = engine.getDocument();
                    NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, false);
                        //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                        //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                    }
                }else if(newState == FAILED){
                    pi.setVisible(false);
                    refreshH.setVisible(true);
                }
            }
        });
        
        back.disabledProperty().addListener((obs,o,n)->{
            back.setGraphic(icon("broback"+(n?"_dis":""),30));
        });
        foward.disabledProperty().addListener((obs,o,n)->{
            foward.setGraphic(icon("forward"+(n?"_dis":""),30));
        });
        back.setDisable(true);
        foward.setDisable(true);
        history.currentIndexProperty().addListener((obs,o,n)->{
            back.setDisable(n.intValue() == 0);
            foward.setDisable(n.intValue() == history.getEntries().size()-1);
        });
        
        back.setOnAction(e->{
            goBack();
        });
        foward.setOnAction(e->{
            goForward();
        });
        cancel.setOnAction(e->{
            refreshH.setVisible(false);
            pi.setVisible(true);
            load(history.getEntries().isEmpty()?new Db().getSetting("downloadLink"):history.getEntries().get(history.getCurrentIndex()).getUrl());
        });
        
        pi.setPrefSize(30, 30);
        
        dlPrompt.setContentDisplay(ContentDisplay.BOTTOM);
        dlPrompt.setAlignment(Pos.CENTER);
        dlPrompt.setStyle("-fx-font-weight:bold;-fx-font-size: 20px");
        download.visibleProperty().bindBidirectional(dlPrompt.visibleProperty());
        download.visibleProperty().addListener((obs,o,n)->{
            if(n)engine.load(null);
            download.setDisable(!n);
        });
        
        refresh.setContentDisplay(ContentDisplay.BOTTOM);
        refreshH.setStyle("-fx-background-color: #fff");
        refresh.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        refresh.setOnAction(e->{
            refreshH.setVisible(false);
            pi.setVisible(true);
            load(history.getEntries().isEmpty()?new Db().getSetting("downloadLink"):history.getEntries().get(history.getCurrentIndex()).getUrl());
        });
        
        pi.visibleProperty().addListener((obs,o,n)->{
            web.setDisable(n);
        });
       
        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox hb = new HBox(5,download,space,pi,back,foward,cancel);
        hb.setAlignment(Pos.CENTER);
        hb.setPadding(new Insets(5));
        StackPane webH = new StackPane(web,dlPrompt,bored,refreshH);
        vb.getChildren().addAll(webH,hb);
        VBox.setMargin(webH, new Insets(10));
        webH.setStyle("-fx-border-width: 5px;-fx-border-color: #808080; -fx-border-radius: 5px;");
        this.getChildren().addAll(vb);
        
        download.setVisible(false);
    }
    
    public static void load(String url){
        engine.load(url);
    }
    
    public String goBack(){    
      pi.setVisible(true);
      bored.setVisible(false);
      download.setVisible(false);
    ObservableList<WebHistory.Entry> entryList=history.getEntries();
    int currentIndex=history.getCurrentIndex();
//    Out("currentIndex = "+currentIndex);
//    Out(entryList.toString().replace("],","]\n"));

    Platform.runLater(new Runnable() { public void run() { history.go(-1); } });
    return entryList.get(currentIndex>0?currentIndex-1:currentIndex).getUrl();
  }

  public String goForward(){    
      pi.setVisible(true);
      bored.setVisible(false);
      download.setVisible(false);
    ObservableList<WebHistory.Entry> entryList=history.getEntries();
    int currentIndex=history.getCurrentIndex();
//    Out("currentIndex = "+currentIndex);
//    Out(entryList.toString().replace("],","]\n"));

    Platform.runLater(new Runnable() { public void run() { history.go(1); } });
    return entryList.get(currentIndex<entryList.size()-1?currentIndex+1:currentIndex).getUrl();
  }
    
    
    public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setPreserveRatio(true);
        image.setFitHeight(size);
        return image;
    }
    
    
    Item n = null;
     public Item getName(){
         n = null;
         RadioButton mp3 = new RadioButton("Mp3");
         RadioButton mp4 = new RadioButton("Mp4");
         ToggleGroup rg = new ToggleGroup();
         rg.selectToggle(mp3);
         rg.getToggles().addAll(mp3,mp4);
         Label l = new Label();
                 l.setText("Please name this file");
         TextField name = new TextField("");
         name.setPromptText("Enter title");
         VBox vb = new VBox(10,l,name,new HBox(5,mp3,mp4));
         vb.setPadding(new Insets(20));
         Scene scene = new Scene(vb);
         scene.getStylesheets().add(getClass().getResource("css/popup.css").toExternalForm());
         Stage stage = new Stage();
         stage.setAlwaysOnTop(true);
         stage.setResizable(false);
         stage.setTitle("Set title");
         name.textProperty().addListener((obs,o,n)->{
             if(n != null){
                 stage.setTitle(n);
             }
         });
         name.setOnAction(e->{
             Item item = new Item(name.getText().trim(),mp3.isSelected()?"mp3":"mp4",null);
             n = item;
             stage.close();
         });
         stage.setScene(scene);
         stage.setOnCloseRequest(e->{
             e.consume();
             Item item = new Item(name.getText().trim(),mp3.isSelected()?"mp3":"mp4",null);
             n = item;
                 stage.close();
         });
         stage.showAndWait();
         return n;
     }
    
    
}

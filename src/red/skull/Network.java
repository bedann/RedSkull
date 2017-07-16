/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.EqualizerBand;
import org.controlsfx.control.MaskerPane;
import static red.skull.Db.fire;

/**
 *
 * @author Monroe
 */
public class Network extends StackPane{
    
    ObservableList<Item> search_tings = FXCollections.observableArrayList();
    ObservableList<Item> settings = FXCollections.observableArrayList();
    Button back = new Button(null,icon("back3",30));
    Label place= new Label("When you are connected to the internet you will be able to see and download settings shared by other people here");
    ListView <Item>eqs = new ListView(settings);
    boolean selected = false;
    CheckBox openAfter = new CheckBox("Open equalizer after");
    MaskerPane masker = new MaskerPane();
    Wtext search = new Wtext("Search for an EQ");
    Button save = new Button(null,icon("dl",30));
    Button tryOut = new Button(null,icon("try1",30));
    HBox controls = new HBox(20,save,tryOut,openAfter);
    Player player;
    Db db = new Db();

    public Network(Player player) {
        this.player = player;
        masker.setVisible(false);
        search.setMaxWidth(200);
        place.setWrapText(true);
        place.setStyle("-fx-text-fill: #cecece");
        place.setMaxWidth(250);
        back.setOnMouseEntered(e->{back.setGraphic(icon("back3_pressed",30));});
        back.setOnMouseExited(e->{back.setGraphic(icon("back3",30));});
        back.setOnAction(e->{
            RedSkull.pane.setPinnedSide(null);
        });
        
        eqs.setCellFactory((ListView<Item> l)->new Cell());
        eqs.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{
            selected = (n != null);
        });
        eqs.getItems().addListener((ListChangeListener.Change<? extends Item> c) ->{
            controls.setDisable(false);
        });
        
        tryOut.setOnAction(e->{
            tryOut();
        });
        save.setOnAction(e->{
            save();
        });
        
        search.textProperty().addListener((obs,o,n)->{
            if(n != null && !n.equals("")){
                System.out.println("set");
                search_tings.clear();
                eqs.setItems(search_tings);
                for(Item item: settings){
                    if((item.getName()).toLowerCase().contains(n)){
                        search_tings.add(item);
                    }
                }
            }else{
                System.out.println("reset");
                eqs.setItems(settings);
            }
        });
        
        fire.child("red_skull").child("settings").child("items").addChildEventListener(new ChildEventListener(){

            @Override
            public void onChildAdded(DataSnapshot ds, String string) {
                System.out.println(ds.getValue().getClass());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        settings.add(ds.getValue(Item.class));
                    }
                  });
            }

            @Override
            public void onChildChanged(DataSnapshot ds, String string) {
            }

            @Override
            public void onChildRemoved(DataSnapshot ds) {
                settings.remove(ds.getValue());
            }

            @Override
            public void onChildMoved(DataSnapshot ds, String string) {
            }

            @Override
            public void onCancelled(FirebaseError fe) {
            }
        });
        
        controls.setAlignment(Pos.CENTER);
        controls.setMaxHeight(30);
        controls.setDisable(true);
        save.setTooltip(new Tooltip("Save for offline"));
        tryOut.setTooltip(new Tooltip("Try it out"));
        eqs.setPlaceholder(place);
        eqs.getStyleClass().add("lists2");
        back.getStyleClass().add("back");
        this.getChildren().addAll(eqs,search,controls,masker);
//        this.setPrefSize(450, 430);
        this.getStyleClass().add("root");
        this.getStylesheets().add(getClass().getResource("css/windows.css").toExternalForm());
        back.getStyleClass().add("back");
        StackPane.setMargin(eqs, new Insets(40,30,60,30));
        StackPane.setAlignment(back, Pos.TOP_RIGHT);
        StackPane.setMargin(search, new Insets(10,0,0,30));
        StackPane.setAlignment(search, Pos.TOP_LEFT);
        StackPane.setMargin(controls, new Insets(10));
        StackPane.setAlignment(controls, Pos.BOTTOM_CENTER);
        
    }
    
    
    public void tryOut(){
        if(player.getMediaPlayer() == null){
            return;
        }
        masker.setVisible(true);
        Item item = eqs.getSelectionModel().getSelectedItem();
        if(item != null){
            fire.child("red_skull").child("settings").child("share").child(item.getName()).addListenerForSingleValueEvent(new ValueEventListener(){

                @Override
                public void onDataChange(DataSnapshot ds) {
                    System.out.println(ds.getValue().getClass());
                    ArrayList<String> list = ds.getValue(ArrayList.class);
                    masker.setVisible(list != null);
                    if(list != null){
                        ObservableList<EqualizerBand> bands = arrayToList(list);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                player.getEqualizer().reCalibrate(player.getMediaPlayer(), bands);
                                if(openAfter.isSelected()){RedSkull.pane.setPinnedSide(Side.RIGHT);}
                                masker.setVisible(false);
                            }
                          });
                    }
                }

                @Override
                public void onCancelled(FirebaseError fe) {
                    masker.setVisible(false);
                }
            });
        }else{
            RedSkull.notify.show(true, "Select an EQ first");
        }
    }
    
    public void save(){
        final Item item = eqs.getSelectionModel().getSelectedItem();
        if(item != null){
            masker.setVisible(true);
            fire.child("red_skull").child("settings").child("share").child(item.getName()).addListenerForSingleValueEvent(new ValueEventListener(){

                @Override
                public void onDataChange(DataSnapshot ds) {
                    ArrayList<String> list = ds.getValue(ArrayList.class);
                    masker.setVisible(list != null);
                    if(list != null){
                        ObservableList<EqualizerBand> bands = arrayToList(list);
                        for(int i = 0; i<7; i++){
                            EqualizerBand band = bands.get(i);
                            db.saveEQ(item.getName(), i, band.getCenterFrequency()+","+band.getGain());
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Equalizer.presetItems.setAll(db.getEQs());
                                masker.setVisible(false);
                            }
                          });
                    }
                }

                @Override
                public void onCancelled(FirebaseError fe) {
                    masker.setVisible(false);
                }
            });
        }else{
            RedSkull.notify.show(true, "Select an EQ first");
        }
    }
    
    public ObservableList<EqualizerBand> arrayToList(ArrayList<String> list){
        ObservableList<EqualizerBand> bands = FXCollections.observableArrayList();
        for(int i = 0; i<7;i++){
            String val = list.get(i);
            String[] vals = val.split(",");
            double freq = Double.valueOf(vals[0].replaceAll(" ", "").replaceAll("kHz", "").replaceAll("Hz", "").replaceAll("k", ""));
            double gain = Double.valueOf(vals[1].replaceAll(" ", ""));
            System.out.println(freq+","+gain);
            bands.add(new EqualizerBand(freq,freq/2,gain));
        }
        return bands;
    }
    
    
    public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setPreserveRatio(true);
        image.setFitHeight(size);
        return image;
    }
    
    class Cell extends ListCell<Item>{
        @Override
        public void updateItem(Item item, boolean empty){
            super.updateItem(item, empty);
            if(item != null && !empty){
                setMaxWidth(200);
                setText(item.getName()+" - "+item.getDesc());
                if(getText()==null){setText(item.getName());}
            }else{
                setText("");
            }
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.firebase.client.Firebase;
import com.jfoenix.controls.JFXToggleButton;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;

/**
 *
 * @author Monroe
 */
public class Equalizer extends StackPane{

    public static ObservableList<String> presetItems = FXCollections.observableArrayList();
    Button back = new Button(null,icon("back3",30));
    ComboBox <String> presets = new ComboBox(presetItems);
//    CheckBox swich = new CheckBox("Enable");
    JFXToggleButton swich = new JFXToggleButton();
    GridPane grid = new GridPane();
    Label equa = new Label("EQUALIZER");
    SpectrumBar specs[];
    final ObservableList<EqualizerBand> bands = FXCollections.observableArrayList();
    TextField name = new TextField();
    Button save = new Button(null,icon("save1",30));
    Button share = new Button(null,icon("share",30));
    HBox sp = new HBox();
    HBox controls = new HBox(10,share,sp,name,save);
    public static Label pmt = new Label();
    private static final int BAND_COUNT = 7;
    private static final int FREQ = 250;
    Db db = new Db();
    SpectrumListener specListener;
    
    
    
    public Equalizer(Player player) {
        swich.setText("Enable");
        swich.setToggleColor(Color.RED);
        back.setOnMouseEntered(e->{back.setGraphic(icon("back3_pressed",30));});
        back.setOnMouseExited(e->{back.setGraphic(icon("back3",30));});
        back.setOnAction(e->{
            RedSkull.pane.setPinnedSide(null);
        });
        
        pmt.setText("Something must be playing to be able to play with these babies\n"
                + "Changes in frequency can be seen by the visualization of bars shown behind (The 'jumping' lines)");
        pmt.setTextAlignment(TextAlignment.CENTER);
        pmt.setStyle("-fx-font-weight: bold");
        pmt.setMaxWidth(250);
        pmt.setWrapText(true);
        grid.setAlignment(Pos.CENTER);
        pmt.getStyleClass().add("label2");
        equa.setStyle("-fx-font-weight: bold; -fx-font-size: 18px");
        controls.setAlignment(Pos.CENTER_RIGHT);
        controls.setMaxHeight(50);
        
        
        presetItems.addAll(db.getEQs());
        
        presets.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{
            if(n != null){
                if(n.equals("Default")){
                    calibrate(player.getMediaPlayer());
                    return;
                }
                Player.bands.clear();
                Player.bands.addAll(db.getSavedEQs(n));
                reCalibrate(player.getMediaPlayer(), Player.bands);
                db.saveSetting("last_eq", n);
            }
        });
        
        save.setOnAction(e->{
            if(name.getText().isEmpty()){
                RedSkull.notify.show(true,"Bruh, add a name");
            }else{
                save();
                name.clear();
                presetItems.clear(); presetItems.addAll(db.getEQs());
            }
        });
        
        share.setOnAction(e->{
            Item item = getItem();
            if(item.getName() != null){
                share(item);
            }
        });
        
        pmt.visibleProperty().addListener((obs,o,n)->{
            presets.setDisable(n);
            controls.setDisable(n);
            swich.setDisable(n);
        });
        presets.setDisable(true);
        controls.setDisable(true);
        swich.setDisable(true);
       
        swich.selectedProperty().addListener((obs,o,n)->{
            db.saveSetting("eq", n.toString());
            presets.setDisable(!n);
            for(Node node: grid.getChildren()){
                node.setDisable(!n);
            }
        });
        swich.setSelected(db.getSetting("eq").equals("true"));
       
        Label nb = new Label("* Before modifying an EQ please switch to the 'Default' preset to start from the proper range");
        nb.setWrapText(true);
        share.setTooltip(new Tooltip("Share your settings with other music lovers"));
        share.getStyleClass().add("back");
        presets.setPromptText("Presets");
        back.getStyleClass().add("back");
        HBox.setHgrow(sp, Priority.ALWAYS);
        this.getChildren().addAll(pmt,nb,equa,grid,controls,presets,swich);
        this.getStyleClass().add("equa");
        this.getStylesheets().add(getClass().getResource("css/equalizer.css").toExternalForm());
        this.setPrefWidth(450);
        StackPane.setMargin(grid, new Insets(0,0,0,30));
        StackPane.setMargin(presets, new Insets(10,0,0,20));
        StackPane.setMargin(swich, new Insets(-5,20,0,0));
        StackPane.setMargin(controls, new Insets(0,0,10,20));
        StackPane.setMargin(nb, new Insets(50,20,20,20));
        StackPane.setAlignment(nb, Pos.TOP_CENTER);
        StackPane.setAlignment(swich, Pos.TOP_RIGHT);
        StackPane.setAlignment(back, Pos.TOP_RIGHT);
        StackPane.setAlignment(controls, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(equa, Pos.TOP_CENTER);
        StackPane.setAlignment(presets, Pos.TOP_LEFT);
    }
    
    public void reCalibrate(MediaPlayer mp, ObservableList<EqualizerBand> bands){
        mp.getAudioEqualizer().setEnabled(db.getSetting("eq").equals("true"));
        swich.selectedProperty().unbind();
        swich.selectedProperty().bindBidirectional(mp.getAudioEqualizer().enabledProperty());
        presets.getSelectionModel().clearSelection();
        grid.getChildren().clear();
        pmt.setVisible(false);
        final ObservableList<EqualizerBand> mp_bands = mp.getAudioEqualizer().getBands();
        mp_bands.clear();
        mp_bands.addAll(bands);
        
        double min = EqualizerBand.MIN_GAIN;
        double max = EqualizerBand.MAX_GAIN;
        
        for(int i = 0; i<mp_bands.size(); i++){
            EqualizerBand eb = mp_bands.get(i);
            Slider slider = slider(eb,min,max);
            GridPane.setHgrow(slider, Priority.ALWAYS);
            final Label label = new Label(format(eb.getCenterFrequency()));
            label.getStyleClass().add("label");
            grid.add(slider, i, 1);
            grid.add(label, i, 2);
            
        }
        if(!swich.isSelected()){
            for(Node node: grid.getChildren()){
                node.setDisable(true);
            }
        }
    }
    
    
    public void calibrate(MediaPlayer mp){
        mp.getAudioEqualizer().setEnabled(db.getSetting("eq").equals("true"));
        swich.selectedProperty().unbind();
        swich.selectedProperty().bindBidirectional(mp.getAudioEqualizer().enabledProperty());
        grid.getChildren().clear();
        pmt.setVisible(false);
        final ObservableList<EqualizerBand> bands = mp.getAudioEqualizer().getBands();
        bands.clear();
        
        double min = EqualizerBand.MIN_GAIN;
        double max = EqualizerBand.MAX_GAIN;
        double mid = (max-min)/2;
        double freq = FREQ;
        
        for(int i = 0; i<BAND_COUNT; i++){
            double theta = (double)i/(double)(BAND_COUNT-1)*(2*Math.PI);
            
            double scale = 0.4 * (1+Math.cos(theta));
            
            double gain = min + mid + (mid*scale);
            bands.add(new EqualizerBand(freq,freq/2,gain));
            freq*=2;
        }
        
        
        for(int i = 0; i<bands.size(); i++){
            EqualizerBand eb = bands.get(i);
            Slider slider = slider(eb,min,max);
            GridPane.setHgrow(slider, Priority.ALWAYS);
            final Label label = new Label(format(eb.getCenterFrequency()));
            label.getStyleClass().add("label");
            grid.add(slider, i, 1);
            grid.add(label, i, 2);
            
        }
        
        if(!swich.isSelected()){
            for(Node node: grid.getChildren()){
                node.setDisable(true);
            }
        }
    }
    
    
    public void secondStep(MediaPlayer mp){
        specs = new SpectrumBar[BAND_COUNT];
        
        for(int i = 0; i<specs.length; i++){
            specs[i] = new SpectrumBar(100,20);
            specs[i].setMaxWidth(40);
            grid.add(specs[i], i, 0);
        }
        specListener = new SpectrumListener(FREQ,mp,specs);
    }
    
    public void save(){
        ObservableList<Slider> sliders = FXCollections.observableArrayList();
        ObservableList<Label> labels = FXCollections.observableArrayList();
        for(Node n: grid.getChildren()){
            if (n instanceof Slider ){
                sliders.add((Slider)n);
            }
            if(n instanceof Label){
                labels.add((Label)n);
            }
        }
        for(int i = 0; i<7; i++){
            Slider s = sliders.get(i);
            Label l = labels.get(i);
            db.saveEQ(name.getText(), i, l.getText()+","+s.getValue());
        }
    }
    
    public void share(Item item){
        if(item.getName().equals("")){
            return;
        }
        Map<String,Object> post = new HashMap();
        Map<String,Object> updates = new HashMap();
        ObservableList<Slider> sliders = FXCollections.observableArrayList();
        ObservableList<Label> labels = FXCollections.observableArrayList();
        for(Node n: grid.getChildren()){
            if (n instanceof Slider ){
                sliders.add((Slider)n);
            }
            if(n instanceof Label){
                labels.add((Label)n);
            }
        }
        for(int i = 0; i<7; i++){
            Slider s = sliders.get(i);
            Label l = labels.get(i);
            post.put(i+"", l.getText().replaceAll("Hz", "").replaceAll("kHz", "")+","+s.getValue());
        }
        updates.put("/red_skull/settings/share/"+item.getName(), post);
        updates.put("/red_skull/settings/items/"+item.getName(), item.toMap());
        Db.fire.updateChildren(updates);
        RedSkull.pane.setPinnedSide(Side.BOTTOM);
    }
    
    
    private String format(double f){
        if(f <1000){
            return String.format("%.0f Hz", f);
        }else{
            return String.format("%.1f kHz", f/1000);
        }
    }
    
    
    public Slider slider(EqualizerBand band,double min,double max){
        Slider slider = new Slider(min,max,band.getGain());
        slider.setOrientation(Orientation.VERTICAL);
        slider.setMaxHeight(150);
        slider.valueProperty().bindBidirectional(band.gainProperty());
        slider.getStyleClass().add("eSeeker");
        return slider;
    }
    
    
    public HBox hbox(){
        HBox hb = new HBox();
        hb.setAlignment(Pos.CENTER);
        return hb;
    }
    
    public ImageView icon(String name,int size){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+name+".png")));
        image.setPreserveRatio(true);
        image.setFitHeight(size);
        return image;
    }
    
    Item n = null;
     public Item getItem(){
         n = null;
         Label l = new Label();
                 l.setText("Add a name for your upload");
         Wtext name = new Wtext("Enter name");
         Wtext desc = new Wtext("Description");
         VBox vb = new VBox(10,l,name,desc);
         vb.setPadding(new Insets(20));
         Scene scene = new Scene(vb);
         Stage stage = new Stage();
         stage.setScene(scene);
         stage.setTitle("Share Equalizer");
         stage.setOnCloseRequest(e->{
             e.consume();
             n = new Item(name.getText(),desc.getText(),"zzff");
                 stage.close();
         });
         stage.showAndWait();
         return n;
     }
    
}

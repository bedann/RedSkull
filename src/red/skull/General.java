/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 *
 * @author Monroe
 */
public class General extends StackPane{
    
    GridPane grid = new GridPane();
    Stage stage;
    Db db;
    Top top;

    public General(Stage stage,Top top) {
        db = new Db();
        this.top = top;
        this.stage = stage;
        
        ToggleButton normal = toggle("normal",0),circle = toggle("circle",1),square = toggle("square",2);
        ToggleGroup gp = new ToggleGroup();
        gp.getToggles().addAll(circle,square,normal);
        
        VBox grid = new VBox(5);
        grid.getChildren().addAll(
                label("Start Up","title"),
                swich("Include videos in search","includeVideo"),
                swich("Scan media directory (get notification if media is added)","scan"),
                swich("Always open screenshots","openShots"),
                label("Media PlayBack","title"),
                swich("Loop songs","loop"),
                swich("Video always on top","onTop"),
                swich("Always show video","video"),
                swich("Video always fullscreen","fullscreen"),
                label("Storage","title"),
                text("Sreenshots folder","shotsDir"),
                text("Downloads folder","downloadDir"),
                label("Internet","title"),
                text("Music link","downloadLink"),
                label("Customization","title"),
                label("Choose mini player type","desc"),
                new FlowPane(circle,square,normal)
        );
        
        grid.setPadding(new Insets(0,0,20,0));
        this.getChildren().addAll(new ScrollPane(grid));
        this.setPadding(new Insets(20));
        this.getStylesheets().add(getClass().getResource("css/gen.css").toExternalForm());
        
    }
    
    
    public CheckBox swich(String text,String value){
        CheckBox swich = new CheckBox();
        swich.setText(text);
//        swich.setPrefSize(20, 20);
        swich.setStyle("-fx-text-fill:#cecece");
        swich.setSelected(db.getSetting(value).equals("true"));
        swich.selectedProperty().addListener((obs,o,n)->{
            db.saveSetting(value, n.toString());
            top.refresh();
        });
//        swich.setToggleColor(Color.RED);
        return swich;
    }
    
    public Label label(String text,String clas){
        Label l = new Label(text);
        if(clas.equals("title"))l.setPadding(new Insets(5,0,0,0));
        l.getStyleClass().add(clas);
        return l;
    }
    
    public ToggleButton toggle(String image,int rep){
        ToggleButton tg = new ToggleButton();
        tg.getStyleClass().add("tg");
        tg.setGraphic(icon(image,50));
        if(Integer.toString(rep).equals(db.getSetting("miniPlayer")))tg.setSelected(true);
        tg.selectedProperty().addListener((obs,o,n)->{
            if(n){
                RedSkull.player_index = rep;
                db.saveSetting("miniPlayer", rep+"");
                top.getPlayer().setInitMiniPlayer();
                top.getPlayer().initMini();
            }
        });
        return tg;
    }
    
    public VBox text(String prompt,String value){
        Label l = new Label(prompt);
        l.getStyleClass().add("desc");
        TextField tx = new TextField(db.getSetting(value));
        JFXButton browse = new JFXButton("Browse");
        browse.setOnAction(e->{
            DirectoryChooser dc = new DirectoryChooser();
            try{
                File f = dc.showDialog(stage);
                if(f != null)tx.setText(f.getAbsolutePath());
            }catch(Exception fe){}
        });
        JFXButton save = new JFXButton("SAVE");
        save.setStyle("-fx-text-fill: greenyellow");
        save.setOnAction(e->{
            File f = new File(tx.getText().trim());
            if(!f.exists() && !f.mkdirs() && !value.toLowerCase().contains("link")){
                RedSkull.notify.show(true, "That directory does not exist");
            }
            if(f.exists() || value.toLowerCase().contains("link")){
                db.saveSetting(value,tx.getText());
                if(value.toLowerCase().contains("link"))Browser.load(tx.getText());
            }
        });
        HBox hb = new HBox(20,tx,browse,save);
        if(value.toLowerCase().contains("link"))hb.getChildren().remove(1);
        VBox vb = new VBox(l,hb);
        return vb;
    }
    
    
    public ImageView icon(String ic,int d){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+ic+".PNG")));
        image.setFitHeight(d);
        image.setPreserveRatio(true);
        return image;
    }
    
    
}

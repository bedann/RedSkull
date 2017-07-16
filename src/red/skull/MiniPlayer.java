/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.effects.JFXDepthManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.PopOver;
import static red.skull.RedSkull.root;

/**
 *
 * @author Monroe
 */
public class MiniPlayer extends Stage implements MiniListener{
    
    BooleanProperty playPressed = new SimpleBooleanProperty(false);
    BooleanProperty nextPressed = new SimpleBooleanProperty(false);
    BooleanProperty prevPressed = new SimpleBooleanProperty(false);
    ImageView image = new ImageView();
    Label time1 = new Label(""),time2 = new Label("");
    Button play = new Button(null,(Node)icon("play-arrow","#cecece"));
    Button next = new Button(null,icon("play-next-button","#cecece"));
    Button previous = new Button(null,icon("previous-track","#cecece"));
    Button expand = new Button(null,icon("move-window","#cecece"));
    Button volume = new Button(null,icon("volume-up","#cecece"));
    Label name;
    Label ellapsed = new Label("_"),total = new Label("_");
    Db db = new Db();
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    JFXProgressBar progress = new JFXProgressBar();
    PopOver vol = popOver();
    Stage mainStage;
    Player player;

    public MiniPlayer(Stage mainStage) {
        this.mainStage = mainStage;
        this.player = player;
//        progress.setPrefWidth(240);
        HBox.setHgrow(progress, Priority.ALWAYS);
        image.setFitWidth(50);
        image.setPreserveRatio(true);
        progress.getStyleClass().add("progress-bar");
        ObservableList<Button> buttons = FXCollections.observableArrayList(play,next,previous,expand,volume);
        for(Button b: buttons){
            b.getStyleClass().add("controls");
        }
        name = new Label("Loading...");
        name.setStyle("-fx-text-fill: orangered");
        ellapsed.setStyle("-fx-text-fill: #cecece");
        total.setStyle(ellapsed.getStyle());
        
        volume.setOnAction(e->{
            vol.show(volume);
        });
        
        vol.showingProperty().addListener((obs,o,n)->{
            if(!n){
                if(player.getMediaPlayer() != null)db.saveSetting("volume", player.getMediaPlayer().getVolume()+"");
            }
        });
        
        expand.setOnAction(e->{
            hide();
        });
        play.setOnAction(e->{
            playPressed.set(!Player.playing.get());
        });
        
        next.setOnAction(e->{
            nextPressed.set(!nextPressed.get());
        });
        previous.setOnAction(e->{
            prevPressed.set(!prevPressed.get());
        });
       
        HBox space = new HBox(name);
        space.setAlignment(Pos.CENTER);
        space.setPadding(new Insets(0,10,0,10));
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox hbox = new HBox(5,previous,play,next,space,volume,expand);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(0,10,0,10));
        HBox timers = new HBox(5,ellapsed,progress,total);
        timers.setAlignment(Pos.CENTER);
        VBox main = new VBox(hbox,timers);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(10,0,5,0));
        HBox mainNode = new HBox(5,image,main);
        mainNode.setPadding(new Insets(10));
        mainNode.setAlignment(Pos.CENTER_LEFT);
        Scene scene = new Scene(mainNode,350,60);
        scene.getStylesheets().add(getClass().getResource("css/mini.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        mainNode.setStyle("-fx-background-color:#222222");
        this.getIcons().add(new Image(getClass().getResourceAsStream("images/icon1.png")));
        this.setScene(scene);
        this.initStyle(StageStyle.TRANSPARENT);
        this.setAlwaysOnTop(true);
        this.setX(bounds.getWidth()-350);
        this.setY(bounds.getHeight()-60);
        scene.setOnMousePressed(e->{
            initX = e.getScreenX()-this.getX();
            initY = e.getScreenY() - this.getY();
        });
        
        scene.setOnMouseDragged(e->{
            this.setX(e.getScreenX()-initX);
            this.setY(e.getScreenY()-initY);
        });
        
        this.showingProperty().addListener((obs,o,n)->{
            if(n){
                mainStage.hide();
            }else{
                mainStage.show();
                mainStage.setIconified(false);
            }
        });
       
        
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void setImage(Image image) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
    @Override
    public void bindTimers(Label ellapsed,Label total){
        this.ellapsed.textProperty().bind(ellapsed.textProperty());
        this.total.textProperty().bind(total.textProperty());
    }
    
    @Override
    public void bindVolume(Slider slider){
        this.slider.valueProperty().bindBidirectional(slider.valueProperty());
    }
    
    @Override
    public void bindImage(ImageView image){
        this.image.imageProperty().bind(image.imageProperty());
    }
    
    @Override
    public void bindSeeker(DoubleProperty progress){
        this.progress.progressProperty().bind(progress);
    }
    
    @Override
    public void bindPlayButton(BooleanProperty binder){
        playPressed.bindBidirectional(binder);
    }
    
    @Override
    public void bindNextButton(BooleanProperty binder){
        nextPressed.bindBidirectional(binder);
    }
    
    @Override
    public void bindPrevButton(BooleanProperty binder){
        prevPressed.bindBidirectional(binder);
    }
    
    @Override
    public void setName(String name){
        this.name.setText(name);
    }
    
    @Override
    public void updateUi(boolean playing){
        play.setGraphic(icon(playing?"pause-button":"play-arrow","#cecece"));
    }
    
    Slider slider;
    @Override
    public PopOver popOver(){
        PopOver pop = new PopOver();
        pop.setAutoHide(true);
        pop.setArrowSize(0);
        slider = new Slider();
        slider.setPadding(new Insets(5,0,5,0));
        slider.getStyleClass().add("seeker");
        slider.setOrientation(Orientation.VERTICAL);
        pop.setContentNode(slider);
        pop.setDetachable(false);
        pop.setPrefSize(20, 150);
        pop.setStyle("-fx-background-color: #414141");
        return pop;
    }
    
    private double initX,initY;
    public ImageView icon(String ic,String d){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+ic+".png")));
        image.setFitHeight(12);
        image.setPreserveRatio(true);
        return image;
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.PopOver;

/**
 *
 * @author Monroe
 */
public class BaseMiniPlayer extends Stage implements MiniListener{
    
    BooleanProperty playPressed = new SimpleBooleanProperty(false);
    BooleanProperty nextPressed = new SimpleBooleanProperty(false);
    BooleanProperty prevPressed = new SimpleBooleanProperty(false);
    DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
    public ImageView image = new ImageView();
    public Label time1 = new Label(""),time2 = new Label("");
    public Button play = new Button(null,(Node)icon("play-arrow","#cecece"));
    public Button next = new Button(null,icon("play-next-button","#cecece"));
    public Button previous = new Button(null,icon("previous-track","#cecece"));
    public Button expand = new Button(null,icon("move-window","#cecece"));
    public Button volume = new Button(null,icon("volume-up","#cecece"));
    public Label name = new Label();
    public Label ellapsed = new Label("_"),total = new Label("_");
    Db db = new Db();
    public Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    public JFXProgressBar progress = new JFXProgressBar();
    PopOver vol = popOver();
    public Stage mainStage;
    Player player;
    public StackPane main = new StackPane();
    Scene scene;
    public static final double scale_factor = -0.6;

    public BaseMiniPlayer() {
        
        
        
        volume.setOnAction(e->{
            vol.show(volume);
        });
        
        vol.showingProperty().addListener((obs,o,n)->{
            if(!n){
                try{
                    if(player.getMediaPlayer() != null)db.saveSetting("volume", player.getMediaPlayer().getVolume()+"");
                }catch(Exception e){e.printStackTrace();}
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
        
        ObservableList<Button> buttons = FXCollections.observableArrayList(play,next,previous,expand,volume);
        for(Button b: buttons){
            b.getStyleClass().add("controls");
        }
        
        scene = new Scene(main);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("css/mini.css").toExternalForm());
        this.getIcons().add(new Image(getClass().getResourceAsStream("images/icon1.png")));
        this.setScene(scene);
        this.initStyle(StageStyle.TRANSPARENT);
        this.setAlwaysOnTop(true);
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
        
        progressProperty.addListener((obs,o,n)->{
            updatePorgress(n.doubleValue());
        });
    }
    
    
    
    public void updatePorgress(Double p){
        
    }

    public void modifyStyleSheet(String sheet){
        scene.getStylesheets().add(getClass().getResource("css/"+sheet+".css").toExternalForm());
    }
    
    Slider slider;
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
    
    
    public ImageView image(String ic,int d){
        ImageView image = new ImageView(new Image(getClass().getResourceAsStream("images/"+ic+".png")));
        image.setFitHeight(d);
        image.setPreserveRatio(true);
        return image;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
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
        this.progressProperty.bind(progress);
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

    @Override
    public void setImage(Image image) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}

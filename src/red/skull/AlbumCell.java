/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.controlsfx.control.GridCell;

/**
 *
 * @author Monroe
 */
public class AlbumCell extends GridCell<String>{
    
    ImageView image = new ImageView();
    Label title = new Label();
    VBox vb = new VBox(image,title);

    public AlbumCell() {
        image.setImage(new Image(getClass().getResourceAsStream("images/album.png")));
        image.setFitHeight(100);
        image.setPreserveRatio(true);
        title.setMaxWidth(100);
        title.setStyle("-fx-text-fill:white");
        vb.setAlignment(Pos.CENTER);
        vb.setOnMouseEntered(e->{vb.setEffect(new Glow(2));});
        vb.setOnMouseExited(e->{vb.setEffect(null);});
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if(item != null && !empty){
            title.setText(item);
            setGraphic(vb);
        }else{
            setGraphic(null);
        }
    }
    
    
    
}

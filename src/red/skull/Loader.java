/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

/**
 *
 * @author Monroe
 */
public class Loader extends Service{
    Db db = new Db();
    FilenameFilter filter = null;
    public static Map<String,ObservableList<Item>> albums = new HashMap<>();
    public static Map<String,ObservableList<Image>> albumCovers = new HashMap<>();

    public Loader() {
        
    }
    
    

    @Override
    protected Task createTask() {
        return new Task<Void>(){
                    @Override
                    protected Void call() throws Exception{
                        updateMessage("Loading music files...");
                        filter = (File f,String name)->{return name.toLowerCase().endsWith(".mp3");};
                        if(db.getSetting("includeVideo").equals("true")){
                            filter = (File f,String name)->{return name.toLowerCase().endsWith(".mp3") || name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".flv");};
                        }
                         
                        
                            File[] roots = File.listRoots();
                            for(int i = 0; i<roots.length;i++){
                                File file = roots[i];
                                this.updateMessage(null);
                                try{
                                    if(!file.getAbsolutePath().startsWith("C:")){
                                        isDirectory(file);
                                    }else{
                                        isDirectory(new File("C:/Users"));
                                    }
                                }catch(Exception e){}
                            }
                            updatePlayList();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                     PlayLists.title.setText("All songs ("+PlayLists.items.size()+")");
                                }
                            });
                            
                       
                        return null;
                    }
                    
                    public void noLoad(){
                        
                    }
                    
                    public void updatePlayList(){
                        for(Item item: db.getPlayListNames()){
                            try{
                                for(Item song: db.getPlayList(item.getName())){
                                    updateMessage("Updating:: "+song.getName());
                                    try{
                                        File f = new File(song.getPath());
                                        if(!f.exists()){
                                            notfound(song);
                                        }
                                    }catch(Exception e){}
                                }
                            }catch(Exception e){}
                        }
                    }
    
                    
                    
                    public  void isDirectory(File f){
//                        System.out.println(f.getName());
                        File[] fs = f.listFiles(filter);
                        if(fs != null){
                            for(int i = 0; i<fs.length;i++){
                                File m = fs[i];
                                add(new Item(m.getName(),"",m.getAbsolutePath()));
                            }
                        }
                        File list[] = f.listFiles();
                        if(list != null){
                            for(int i = 0; i<list.length;i++){
                                File folder = list[i];
                                if(folder.isDirectory() && !folder.getName().startsWith("$")){
                                    isDirectory(folder);
                                }
                            }
                        }
                    }
                    
                    public void add(Item i){
                        categorise(i);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Top.searchItems.add(i);
                                PlayLists.items.add(i);
                            }
                        });
                    }
                    
                    
                    public void categorise(final Item i){
                        
                        try{
                            Media media = new Media(new File(i.getPath()).toURI().toString());
                            media.getMetadata().addListener(new MapChangeListener<String, Object>() {
                            @Override
                            public void onChanged(MapChangeListener.Change<? extends String, ? extends Object> ch) {
                              if (ch.wasAdded()) {
                                String key = ch.getKey();
                                if(key.equals("album")){
                                    String s = (String) ch.getValueAdded();
                                    if(albums.get(s)==null){
                                        ObservableList<Item> songs = FXCollections.observableArrayList(i);
                                        albums.put(s, songs);
                                        Platform.runLater(()->{
                                            PlayLists.albums.getItems().add(s);
                                        });
                                    }else{
                                        albums.get(s).add(i);
                                    }
                                }else if(key.equals("image")){
                                    
                                }
                              }
                            }
                            });
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    
                    
                };
    }
    
    
    
    
    public void notfound(Item item){
        for(Item res: Top.searchItems){
            if(res.getName().equals(item.getName())){
                db.updateSong(item.getDesc(), res.getPath(), item.getName());
            }
        }
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Monroe
 */
public class FileListener {
    
    WatchService watcher;
    Map<WatchKey,Path> keys;
    FileChanger changer;

    public FileListener(Path directory,FileChanger changer) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.changer = changer;
        
        registerAll(directory);
        
        for(;;){
            WatchKey key;
            try{
                key = watcher.take();
            }catch(InterruptedException x){return;}
            Path dir = keys.get(key);
            if(dir == null){
                continue;
            }
            
            for(WatchEvent<?> event:key.pollEvents()){
                WatchEvent.Kind kind = event.kind();
                if(kind == OVERFLOW){
                    continue;
                }
                
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                
                if(kind == ENTRY_CREATE){
                    System.out.println("file created: "+child);
                    if(Files.isDirectory(child, NOFOLLOW_LINKS)){
                        registerAll(child);
                    }else{
                        System.out.println("file created: "+child);
                        File f = child.toFile();
                         if(f.getName().toLowerCase().endsWith(".mp3") || Player.isVideo(f)){
                             changer.fileAdded(f);
                         }
                    }
                }else if(kind == ENTRY_DELETE){
                    System.out.println("file deleted: "+child);
                    File f = child.toFile();
                    if(f.getName().toLowerCase().endsWith(".mp3") || Player.isVideo(f)){
                        changer.fileDeleted(f);
                    }
                }
            }
            boolean valid = key.reset();
            if(!valid){
                keys.remove(key);
                if(keys.isEmpty()){
                    break;
                }
            }
        }
        
    }
    
    
    
    public void register(Path dir) throws IOException{
        WatchKey key = dir.register(watcher, ENTRY_CREATE,ENTRY_DELETE);
        keys.put(key, dir);
    }
    
    
    public void registerAll(final Path start) throws IOException,AccessDeniedException{
//        try{
            Files.walkFileTree(start, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException ,AccessDeniedException{
                File f = dir.toFile();
                if(!f.getName().startsWith(".") && !f.getName().startsWith("$") && !f.getName().contains("Android") && !f.getName().contains("AppData")){
                    register(dir);
                return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.SKIP_SUBTREE; //To change body of generated methods, choose Tools | Templates.
                }
            
        });
//    }catch(Exception e){}
        
    }
    
    public interface FileChanger{
        void fileAdded(File newFile);
        void fileDeleted(File file);
    }
    
    @SuppressWarnings("unchecked")
    static<T> WatchEvent<T> cast(WatchEvent<?> event){
        return(WatchEvent<T>)event;
    }
    
}

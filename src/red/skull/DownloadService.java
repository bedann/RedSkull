/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 *
 * @author Monroe
 */
public class DownloadService extends Service{
    
    ProgressBar progress;
    Label perc;
    Item download;
    String path,ext;

    public DownloadService(Item url,String path,String ext) {
        this.download = url;
        this.path = path;
        this.ext = ext;
    }

    
    
    @Override
    protected Task createTask() {
        return new Task<Void>(){
                    @Override
                    protected Void call() throws Exception{
                       
                        
                        try {
                            int count;
                                    URL url = new URL(download.getPath());
                                    HttpURLConnection  conection = (HttpURLConnection )url.openConnection();
                                    conection.addRequestProperty("User-Agent", "Mozilla/4.76"); 
                                    System.setProperty("http.agent", "Chrome");
                                    conection.connect();

                                    // this will be useful so that you can show a tipical 0-100%
                                    // progress bar
                                    int lenghtOfFile = conection.getContentLength();

                                    // download the file
                                    InputStream input = new BufferedInputStream(url.openStream(),
                                            8192);

                                    // Output stream
//                                    String path = new Db().getSetting("downloadDir");
                                    path = path+"/"+download.getName()+"."+ext;
                                    System.err.println(path);
                                    File f = new File(path);
                                    if (!f.getParentFile().exists()){
                                        f.getParentFile().mkdirs();
                                    }
                                    OutputStream output = new FileOutputStream(path);

                                    byte data[] = new byte[1024];

                                    long total = 0;

                                    while ((count = input.read(data)) != -1) {
                                        total += count;
                                        if(this.isCancelled()){
                                            f.delete();
                                            break;
                                        }
                                        // publishing the progress....
                                        // After this onProgressUpdate will be called
                                        System.err.println((int) ((total * 100) / lenghtOfFile));
                                        this.updateProgress(total, lenghtOfFile);
                                        this.updateMessage(((int) ((total * 100) / lenghtOfFile))+"%");
                                        // writing data to file
                                        output.write(data, 0, count);
                                       
                                    }

                                    // flushing output
                                    output.flush();

                                    // closing streams
                                    output.close();
                                    input.close();

                                } catch (Exception e) {
                                    System.err.println("Error: "+ e.getMessage());
                                    RedSkull.notify.show(true, e.getMessage());
                                }
                        
                        
                        return null;
                    }
                };
    }
    
    
}

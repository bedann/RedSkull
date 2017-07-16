/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import com.firebase.client.Firebase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.EqualizerBand;

/**
 *
 * @author Monroe
 */
public class Db {
    
    public static final String URL = "https://accounts-e49d2.firebaseio.com/";
    public static Firebase fire = new Firebase(URL);
    Statement state;
    ResultSet res;
    Connection con;
    public static final String playlists = "playlists";
    public static final String eqs = "equalizers";
    public static final String lyrics = "lyrics";
    
    
    private static String path = "Database.db";
    public Db(){
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("Error class not found " + e);
        }
        
    }
    
    public static Connection connection(){
        try {
            return DriverManager.getConnection("jdbc:sqlite:Database.db");
        } catch (SQLException ex) {
            System.out.println("Error");
            return null;
        }
    }
    
    
    public void saveSetting(String key,String value){
        try{
            con = Db.connection();
            state = con.createStatement();
            state.executeUpdate("INSERT OR REPLACE INTO settings VALUES('"+key+"','"+value+"')");
        }catch(SQLException e){
            
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String getSetting(String key){
        String val = "";
        try{
            con = Db.connection();
            state = con.createStatement();
            res = state.executeQuery("SELECT value FROM settings WHERE key = '"+key+"'");
            if(res.next()){
                val = res.getString(1);
            }
        }catch(SQLException e){
            
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return val;
    }
    
    public void saveLyrics(String song,String lyrics){
        File f = new File("Lyrics/"+song+".txt");
        if(!f.getParentFile().exists())f.getParentFile().mkdir();
        if(!f.exists()){
            try {
                Formatter formater = new Formatter(f);
                formater.format("%s", lyrics);
                formater.flush();
                formater.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else if(f.length() == 0){
        }
    }
    
    public String getLyrics(String song){
        String val = null;
        try{
            File f = new File("Lyrics/"+song+".txt");
            BufferedReader br = new BufferedReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while(line != null){
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            val = (sb.toString());
            br.close();
        }catch(Exception e){System.out.println(e);}
        return val;
    }
    
    public void deleteSong(String playlist,String path){
        try{
            con = Db.connection();
            state = con.createStatement();
            state.executeUpdate("DELETE FROM  "+playlist+" WHERE path = '"+path+"'");
            
        }catch(SQLException e){
            System.out.println("@delete"+e);
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void updateSong(String playlist,String path,String name){
        try{
            con = Db.connection();
            state = con.createStatement();
            state.executeUpdate("UPDATE "+playlist+" SET path = '"+path+"' WHERE name = '"+name.replaceAll("'", "''")+"'");
            
        }catch(SQLException e){
            System.out.println("@delete"+e);
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void deletePlayList(String name){
        try{
            con = Db.connection();
            state = con.createStatement();
            state.executeUpdate("DROP TABLE "+name);
            state.executeUpdate("DELETE FROM  "+Db.playlists+" WHERE name = '"+name.replaceAll("'", "''")+"'");
            
        }catch(SQLException e){
            System.out.println("@delete"+e);
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void addPlayList(String name,String desc,ObservableList<Item> items){
        if(name == null || name.equals("")){
            System.out.println("name is null");
            return;
        }
        try{
            con = Db.connection();
            state = con.createStatement();
            String sql = "CREATE TABLE "+name.replaceAll("'", "''").replaceAll(" ", "_")+" ("
                    + "name TEXT,desc TEXT,path TEXT PRIMARY KEY"
                    + ");"
                    + "INSERT INTO "+Db.playlists+" VALUES('"+name.replaceAll("'", "''").replaceAll(" ", "_")+"','"+""+"','"+""+"')";
            System.out.println(sql);
            try{state.executeUpdate(sql);}catch(Exception e){}
            for(Item item: items){
                if(item != null && item.getName() != null){
                    sql = "INSERT OR REPLACE INTO "+name.replaceAll("'", "''").replaceAll(" ", "_")+" VALUES('"+item.getName().replaceAll("'", "''")+"','"+name.replaceAll("'", "''").replaceAll(" ", "_")+"','"+item.getPath()+"')";
                    state.executeUpdate(sql);
                }
            }
            
        }catch(SQLException e){
            System.out.println("@addPlaylist"+e);
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public void updatePlayList(String name,ObservableList<Item> items){
        if(name == null || name.equals("")){
            return;
        }
        try{
            con = Db.connection();
            state = con.createStatement();
            String sql = null;
            for(Item item: items){
                sql = "INSERT OR REPLACE INTO "+name.replaceAll("'", "''").replaceAll(" ", "_")+" VALUES('"+item.getName().replaceAll("'", "''")+"','"+name+"','"+item.getPath()+"')";
                state.executeUpdate(sql);
                System.out.println(sql);
            }
            
        }catch(SQLException e){
            System.out.println("@addPlaylist"+e);
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    public ObservableList<Item> getPlayList(String name){
       ObservableList<Item> files = FXCollections.observableArrayList();
        try{
            con = Db.connection();
            state = con.createStatement();
            res = state.executeQuery("SELECT * FROM "+name+"");
            while(res.next()){
                files.add(new Item(res.getString(1),res.getString(2),res.getString(3)));
            }
        }catch(SQLException e){
            System.out.println("@getPlaylists "+e.getMessage());
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return files;
    }
    
    public ObservableList<Item> getPlayListNames(){
       ObservableList<Item> files = FXCollections.observableArrayList();
        try{
            con = Db.connection();
            state = con.createStatement();
            res = state.executeQuery("SELECT * FROM "+Db.playlists+"");
            while(res.next()){
                System.out.println("Found");
                files.add(new Item(res.getString(1),res.getString(2),null));
            }
        }catch(SQLException e){
            System.out.println("@getNames"+e);
        }finally{
            try {
                con.close();
//                res.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return files;
    }
    
    public ObservableList<String> getEQs(){
       ObservableList<String> files = FXCollections.observableArrayList();
        try{
            con = Db.connection();
            state = con.createStatement();
            res = state.executeQuery("SELECT * FROM "+Db.eqs+"");
            while(res.next()){
                files.add((res.getString(1)));
            }
        }catch(SQLException e){
            System.out.println("@getEQS"+e);
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return files;
    }
    
    public ObservableList<EqualizerBand> getSavedEQs(String name){
       ObservableList<EqualizerBand> bands = FXCollections.observableArrayList();
        try{
            con = Db.connection();
            state = con.createStatement();
            res = state.executeQuery("SELECT * FROM "+Db.eqs+" WHERE name = '"+name.replaceAll("'", "''")+"'");
            while(res.next()){
                System.out.println(res.getString(1));
                for(int i = 0; i<7;i++){
                    String val = res.getString("val"+i);
                    String[] vals = val.split(",");
                    double freq = Double.valueOf(vals[0].replaceAll(" ", "").replaceAll("kHz", "").replaceAll("Hz", ""));
                    double gain = Double.valueOf(vals[1].replaceAll(" ", ""));
                    bands.add(new EqualizerBand(freq,freq/2,gain));
                }
            }
        }catch(SQLException e){
            System.out.println("@getQBYNAME "+e);
        }finally{
            try {
                con.close();
//                res.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return bands;
    }
    
    
    
    public void saveEQ(String name,int index,String val){
        if(name == null  || name.equals("")){
            return;
        }
        try{
            con = Db.connection();
            state = con.createStatement();
            res = state.executeQuery("SELECT*FROM "+eqs+" WHERE name = '"+name.replaceAll("'", "''")+"'");
            if(res.next()){
                state.executeUpdate("UPDATE "+eqs+" SET val"+index+" = '"+val+"' WHERE name = '"+name.replaceAll("'", "''")+"'");
            }else{
                state.executeUpdate("INSERT OR REPLACE INTO "+eqs+" (name,val"+index+") VALUES('"+name+"','"+val+"')");
            }
        }catch(SQLException e){
            
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public void updateRecent(String path,String name){
        try{
            con = Db.connection();
            state = con.createStatement();
            res = state.executeQuery("SELECT*FROM recents");
            int count = 0;
            while(res.next()){
                count++;
            }
            if(count>10){
                state.executeUpdate("DELETE FROM recents WHERE name IN (SELECT name FROM recents ORDER BY time ASC LIMIT "+(count-10)+")");
            }
            state.executeUpdate("INSERT OR REPLACE INTO recents (name,path,time) VALUES('"+name.replaceAll("'", "''")+"','"+path+"','"+String.valueOf(System.currentTimeMillis())+"')");
        }catch(SQLException e){
            System.out.println("@delete "+e);
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public ObservableList<Item> getRecents(){
       ObservableList<Item> files = FXCollections.observableArrayList();
        try{
            con = Db.connection();
            state = con.createStatement();
            res = state.executeQuery("SELECT * FROM "+"recents"+" ORDER BY time DESC");
            while(res.next()){
                files.add(new Item(res.getString(1),res.getString(3),res.getString(2)));
            }
        }catch(SQLException e){
            System.out.println("@getRecents "+e.getMessage());
        }finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return files;
    }
    
    
    public void createDb(){
        File f = new File(path);
        if(!f.exists()){
//            f.getParentFile().mkdir();
            try {
                Formatter formater = new Formatter(path);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            }
            create();
        }else if(f.length() == 0){
            create();
        }
    }
    
    public void create(){
        Connection con = connection();
        try{
            
            state = con.createStatement();
            state.executeUpdate("CREATE TABLE settings ("
                    +"key TEXT PRIMARY KEY, value TEXT DEFAULT '0'"
                    + ");"
                    + "INSERT INTO settings VALUES('volume','0.45');"
                    + "INSERT INTO settings VALUES('loop','true');"
                    + "INSERT INTO settings VALUES('loop-curr','0');"
                    + "INSERT INTO settings VALUES('video','false');"
                    + "INSERT INTO settings VALUES('last_eq','Flat');"
                    + "INSERT INTO settings VALUES('eq','true');"
                    + "INSERT INTO settings VALUES('shuffle','false');"
                    + "INSERT INTO settings VALUES('load','true');"
                    + "INSERT INTO settings VALUES('fullscreen','false');"
                    + "INSERT INTO settings VALUES('scan','true');"
                    + "INSERT INTO settings VALUES('openShots','false');"
                    + "INSERT INTO settings VALUES('miniPlayer','0');"
                    + "INSERT INTO settings VALUES('downloadLink','http://tubidy.mobi/');"
                    + "INSERT INTO settings VALUES('shotsDir','"+System.getProperty("user.home")+"/Desktop/Red Skull');"
                    + "INSERT INTO settings VALUES('downloadDir','"+System.getProperty("user.home")+"/Desktop');"
                    + "INSERT INTO settings VALUES('onTop','false');"
                    + "INSERT INTO settings VALUES('includeVideo','false');"
                    + "CREATE TABLE "+Db.playlists+" ("
                    + "name TEXT PRIMARY KEY, desc TEXT, path TEXT"
                    + ");"
                     + "CREATE TABLE "+Db.lyrics+" ("
                    + "song TEXT PRIMARY KEY, lyrics TEXT"
                    + ");"
                    + "CREATE TABLE "+eqs+" (name TEXT PRIMARY KEY, val0 TEXT,val1 TEXT,val2 TEXT,val3 TEXT,val4 TEXT,val5 TEXT,val6 TEXT);"
                    + "CREATE TABLE recents (name TEXT PRIMARY KEY,path TEXT,time TEXT)");
            this.saveEQ("Default", 0, "_");
            defBands();
        }catch(Exception e){RedSkull.notify.show(true, "Error creating the database");System.out.println("@creation"+e);}
    }
    
    
    public void update(){
        Connection con = connection();
        try{
            state = con.createStatement();
            state.executeUpdate("CREATE TABLE "+Db.lyrics+" ("
                    + "song TEXT PRIMARY KEY, lyrics TEXT"
                    + ");");
//            state.executeUpdate("INSERT INTO settings VALUES('fullscreen','false');");
        }catch(Exception e){}
    }
    
    
    public void defBands(){
            double min = EqualizerBand.MIN_GAIN;
            double max = EqualizerBand.MAX_GAIN;
            double mid = (max-min)/2;
            double freq = 250;
            String[] bestEQ = {"250.0,8.4","500.0,4.800000000000001","1.0,-2.399999999999998","2.0,-6.0","4.0,-2.400000000000003","8.0,4.800000000000001","16.0,8.4"};
            String[] flatEQ = {"250 Hz,-5.4688524590163965","500 Hz,-5.2327868852459005","1.0 kHz,-6.236065573770489","2.0 kHz,-6.0","4.0 kHz,-5.940983606557378","8.0 kHz,-5.82295081967213","16.0 kHz,-5.4688524590163965"};
        for(int i = 0; i<7; i++){
            double theta = (double)i/(double)(7-1)*(2*Math.PI);
            
            double scale = 0.4 * (1+Math.cos(theta));
            
            double gain = min + mid + (mid*scale);
            
//            this.saveEQ("Flat", i, 2000+","+"-6.0");
            this.saveEQ("Flat", i, flatEQ[i]);
//            this.saveEQ("Perfect EQ", i, String.valueOf(freq)+","+String.valueOf(gain));
            this.saveEQ("Optimum EQ", i, bestEQ[i]);
            freq*=2;
        }
        
    }
    
    
}

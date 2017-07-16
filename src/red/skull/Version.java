/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Monroe
 */
public class Version {
    
    private String version,release,features,url;

    public Version() {
    }

    public Version(String version, String release, String features, String url) {
        this.version = version;
        this.release = release;
        this.features = features;
        this.url = url;
    }
    
    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("version", version);
        map.put("release", release);
        map.put("features", features);
        map.put("url", url);
        return map;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    
}

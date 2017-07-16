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
public class Item {
    private String name,desc,path;

    public Item() {
    }

    public Item(String name,String desc,String path) {
        this.name = name;
        this.path = path;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap();
        map.put("name", name);
        map.put("desc", desc);
        map.put("path", path);
        return map;
    }
    
    
}

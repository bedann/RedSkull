/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;

/**
 *
 * @author Monroe
 */
public class Wtext extends TextField{
    
        public Wtext(String prompt){
            this.setPromptText(prompt);
        }
        
        public Wtext(String prompt, Pos pos){
            this.setPromptText(prompt);
            this.setAlignment(pos);
        }
    
        @Override
        public void replaceText(int start, int end, String text){
            if(validate(text)){
                super.replaceText(start, end, text);
            }else{
                RedSkull.notify.show(true, "Only letters fam, only letters");
            }
        }
        
        private boolean validate(String text){
            return text.toLowerCase().matches("[a-z_ ]*");
        }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author onlyred
 */
public final class JavaScriptUtil {
    
    public ArrayList<String> findLinkJs(String site,String html)
    {
         String regeJsNoAbs="http\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/\\S*)?";
         Pattern pattern = Pattern.compile(regeJsNoAbs);
         Matcher matcher = pattern.matcher(html);

         return null;
    }
 
          
    
}

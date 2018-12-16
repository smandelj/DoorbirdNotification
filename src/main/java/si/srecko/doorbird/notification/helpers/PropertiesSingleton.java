package si.srecko.doorbird.notification.helpers;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Srecko Mandelj
 * Date: 28.4.2016
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
public class PropertiesSingleton {
    HashMap<String, String> cache;
    public static PropertiesSingleton mySelf=new PropertiesSingleton();

    protected PropertiesSingleton() {
        cache=new HashMap<String, String>();
        ResourceBundle rb=ResourceBundle.getBundle("doorbird");
        Enumeration<String> enumer=rb.getKeys();
        while(enumer.hasMoreElements()) {
            String oneKey=enumer.nextElement();
            cache.put(oneKey, rb.getString(oneKey));
        }
    }

    public String getString(String key) {
        return cache.get(key);
    }

    public static PropertiesSingleton getInstance() {
        return mySelf;
    }
}

package si.srecko.doorbird.notification.controls;

import si.srecko.doorbird.notification.helpers.PropertiesSingleton;

public class VPNControl {
    public static String VPN_GET="vpn";

    public void getVPNCode() {
        PropertiesSingleton pSing=PropertiesSingleton.getInstance();
        URLReader urlReader=new URLReader();
        urlReader.urlPoster(pSing.getString("vpn.url"),
                pSing.getString("vpn.post"));
    }
}

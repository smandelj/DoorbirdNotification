package si.srecko.doorbird.notification.controls;

import org.junit.Test;

public class WakeOnLanTester {
    @Test
    public void testWakeOnLan() {
        MainController mainController=new MainController();
        System.out.println(mainController.doWork(WakeOnLan.WAKE_ON_LAN+"_arcadyan"));
    }
}

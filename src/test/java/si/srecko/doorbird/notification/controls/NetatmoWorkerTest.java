package si.srecko.doorbird.notification.controls;

import org.junit.Assert;
import org.junit.Test;
import si.srecko.doorbird.notification.data.NetatmoTemperatures;

/**
 * Created by Srecko Mandelj on 16. 07. 2017.
 */
public class NetatmoWorkerTest {
    @Test
    public void testNetatmo() {
        NetatmoWorker netatmoWorker=new NetatmoWorker();
        NetatmoTemperatures netatmoTemperatures=netatmoWorker.checkTemp();
        if(netatmoTemperatures!=null)
        System.out.println("Living room temperature:"+netatmoTemperatures.getLivingRoomTemperature()+"," +
                "bedroom temperature:"+netatmoTemperatures.getBedroomTemperature()+", " +
                "outdoor temperature:"+netatmoTemperatures.getOutdoorTemperature());
        Assert.assertNotNull(netatmoTemperatures);
    }
}

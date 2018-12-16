package si.srecko.doorbird.notification.controls;

import org.apache.log4j.Logger;
import si.srecko.doorbird.notification.helpers.Log4JHelper;

/**
 * Created with IntelliJ IDEA.
 * User: Srecko Mandelj
 * Date: 28.4.2016
 * Time: 12:12
 * To change this template use File | Settings | File Templates.
 */
public class MainController {
    Logger log= Log4JHelper.getLogger(this.getClass());

    public String doWork(String event) {
        log.debug("Received event:" + event);
        if(event==null) return "Empty event received.";
        if(ZipatoWorker.RADIO_ON.equals(event)||ZipatoWorker.RADIO_OFF.equals(event)||
                ZipatoWorker.OPEN_DOOR.equals(event)||ZipatoWorker.LOCK_DOOR.equals(event)) {
            ZipatoWorker zipatoWorker=new ZipatoWorker();
            return zipatoWorker.operateDoor(event);
        } else {
            DoorbirdAction doorbirdAction = new DoorbirdAction();
            doorbirdAction.doDoorbotAction(event);
            return null;
        }
    }
}

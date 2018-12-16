package si.srecko.doorbird.notification.controls;

import org.apache.log4j.Logger;
import si.srecko.doorbird.notification.data.NetatmoTemperatures;
import si.srecko.doorbird.notification.helpers.DateSingleton;
import si.srecko.doorbird.notification.helpers.Log4JHelper;
import sun.rmi.runtime.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

/**
 * Created by Srecko Mandelj on 14. 08. 2017.
 */
public class MainTimerTask extends TimerTask {
    DateSingleton dateSingleton = DateSingleton.getInstance();
    Logger log = Log4JHelper.getLogger(this.getClass());

    public void run() {
        log.debug("Timer triggered.");
        NetatmoWorker netatmoWorker = new NetatmoWorker();
        NetatmoTemperatures netatmoTemperatures = netatmoWorker.checkTemp();
        if ((netatmoTemperatures == null) || (netatmoTemperatures.getOutdoorTemperature() == null) ||
                (netatmoTemperatures.getBedroomTemperature() == null) ||
                (netatmoTemperatures.getLivingRoomTemperature() == null)) return;
        String sentMessage = null;
        log.debug("Date for bedroom:" + dateSingleton.getDateOfBedroomMessage() + ", date for living room:" +
                dateSingleton.getDateOfLivingRoomMessage());
        log.debug("Outdor temp:" + netatmoTemperatures.getOutdoorTemperature() + ", living room temperature:" +
                netatmoTemperatures.getLivingRoomTemperature() + ", bedroom temperature:" +
                netatmoTemperatures.getBedroomTemperature());
        if (netatmoTemperatures.getOutdoorTemperature().compareTo(
                netatmoTemperatures.getBedroomTemperature()) < 0) {
            Date compareDate = dateSingleton.getDateOfBedroomMessage();
            if (doSending(compareDate)) {
                sentMessage = "Temperature in bedroom is " + netatmoTemperatures.getBedroomTemperature() +
                        " which is higher than temperature outdoors, where it is " +
                        netatmoTemperatures.getOutdoorTemperature() +
                        ". It is now time to open windows in bedroom.";
                dateSingleton.setDateOfBedroomMessage(new Date());
            }
        }
        if (netatmoTemperatures.getOutdoorTemperature().compareTo(
                netatmoTemperatures.getBedroomTemperature()) > 0) {
            Date compareDate = dateSingleton.getDateOfBedroomMessage();
            if (compareDate!=null) {
                dateSingleton.setDateOfBedroomMessage(null);
                log.debug("We will set the date of bedroom to null, since the temperature outdooor is higher.");
            }
        }

        if (netatmoTemperatures.getOutdoorTemperature().compareTo(
                netatmoTemperatures.getLivingRoomTemperature()) < 0) {
            Date compareDate = dateSingleton.getDateOfLivingRoomMessage();
            if (doSending(compareDate)) {
                if (sentMessage == null) sentMessage = "";
                else sentMessage += " ";

                sentMessage += "Temperature in living room is " + netatmoTemperatures.getLivingRoomTemperature() +
                        " which is higher than temperature outdoors, where it is " +
                        netatmoTemperatures.getOutdoorTemperature() +
                        ". It is now time to open windows in living room.";
                dateSingleton.setDateOfLivingRoomMessage(new Date());
            }
        }

        if (netatmoTemperatures.getOutdoorTemperature().compareTo(
                netatmoTemperatures.getLivingRoomTemperature()) > 0) {
            Date compareDate = dateSingleton.getDateOfLivingRoomMessage();
            if (compareDate!=null) {
                dateSingleton.setDateOfLivingRoomMessage(null);
                log.debug("We will set the date of living room to null, since the temperature outdooor is higher.");
            }
        }

        if (sentMessage != null) {
            InstantMessageSender imSender = new InstantMessageSender();
            imSender.sendInstantMessage(sentMessage);
        }
    }

    private Calendar truncateToDate(Calendar input) {
        if (input == null) return null;
        input.set(Calendar.HOUR_OF_DAY, 0);
        input.set(Calendar.MINUTE, 0);
        input.set(Calendar.SECOND, 0);
        input.set(Calendar.MILLISECOND, 0);
        return input;
    }

    private boolean doSending(Date compareDate) {
        Date lastSent = compareDate;
        /*Calendar currentCal = truncateToDate(Calendar.getInstance());
        if (lastSent != null) {
            Calendar lastSentCal = Calendar.getInstance();
            lastSentCal.setTime(lastSent);
            lastSentCal = truncateToDate(lastSentCal);
            if (lastSentCal.compareTo(currentCal) < 0) lastSent = null;
        }*/
        return lastSent == null;
    }
}

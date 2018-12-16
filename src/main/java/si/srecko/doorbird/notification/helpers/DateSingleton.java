package si.srecko.doorbird.notification.helpers;

import java.util.Date;

/**
 * Created by Srecko Mandelj on 14. 08. 2017.
 */
public class DateSingleton {
    private Date dateOfBedroomMessage=null;
    private Date dateOfLivingRoomMessage=null;

    public static DateSingleton mySelf=new DateSingleton();

    public static DateSingleton getInstance() {
        return  mySelf;
    }

    public Date getDateOfBedroomMessage() {
        return dateOfBedroomMessage;
    }

    public void setDateOfBedroomMessage(Date dateOfBedroomMessage) {
        this.dateOfBedroomMessage = dateOfBedroomMessage;
    }

    public Date getDateOfLivingRoomMessage() {
        return dateOfLivingRoomMessage;
    }

    public void setDateOfLivingRoomMessage(Date dateOfLivingRoomMessage) {
        this.dateOfLivingRoomMessage = dateOfLivingRoomMessage;
    }
}

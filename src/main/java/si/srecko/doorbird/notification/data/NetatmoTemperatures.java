package si.srecko.doorbird.notification.data;

import java.math.BigDecimal;

/**
 * Created by Srecko Mandelj on 14. 08. 2017.
 */
public class NetatmoTemperatures {
    private BigDecimal livingRoomTemperature;
    private BigDecimal bedroomTemperature;
    private BigDecimal outdoorTemperature;

    public BigDecimal getLivingRoomTemperature() {
        return livingRoomTemperature;
    }

    public void setLivingRoomTemperature(BigDecimal livingRoomTemperature) {
        this.livingRoomTemperature = livingRoomTemperature;
    }

    public BigDecimal getBedroomTemperature() {
        return bedroomTemperature;
    }

    public void setBedroomTemperature(BigDecimal bedroomTemperature) {
        this.bedroomTemperature = bedroomTemperature;
    }

    public BigDecimal getOutdoorTemperature() {
        return outdoorTemperature;
    }

    public void setOutdoorTemperature(BigDecimal outdoorTemperature) {
        this.outdoorTemperature = outdoorTemperature;
    }
}

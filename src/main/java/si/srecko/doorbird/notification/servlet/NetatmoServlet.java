package si.srecko.doorbird.notification.servlet;

import com.google.gson.JsonObject;
import si.srecko.doorbird.notification.controls.NetatmoWorker;
import si.srecko.doorbird.notification.data.NetatmoTemperatures;
import si.srecko.doorbird.notification.helpers.PropertiesSingleton;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NetatmoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        PropertiesSingleton pSing=PropertiesSingleton.getInstance();
        NetatmoWorker netatmoWorker=new NetatmoWorker();
        NetatmoTemperatures temperatures=netatmoWorker.checkTemp();
        String response="Temperatures are not available at the moment. Please check later.";
        if(temperatures!=null) {
            response="Temperature in bedroom is "+temperatures.getBedroomTemperature()+
                    " degrees and temperature in living room is "+temperatures.getLivingRoomTemperature()+
                    " degrees. Outdoor temperature is "+temperatures.getOutdoorTemperature()+
                    " degrees.";
        }
        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("speech",response);
        jsonObject.addProperty("displayText",response);
        httpServletResponse.setContentType("application/json");
        ServletOutputStream output=httpServletResponse.getOutputStream();
        output.print(jsonObject.toString());
        output.close();
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doGet(httpServletRequest, httpServletResponse);
    }
}

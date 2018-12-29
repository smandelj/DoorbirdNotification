package si.srecko.doorbird.notification.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import si.srecko.doorbird.notification.controls.*;
import si.srecko.doorbird.notification.data.NetatmoTemperatures;
import si.srecko.doorbird.notification.helpers.Log4JHelper;
import si.srecko.doorbird.notification.helpers.PropertiesSingleton;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Srecko Mandelj
 * Date: 28.4.2016
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
public class ZipatoServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        Logger log= Log4JHelper.getLogger(this.getClass());
        PropertiesSingleton pSing=PropertiesSingleton.getInstance();
        String event=httpServletRequest.getParameter("event");
        String response;
        if(event==null) event=(String)httpServletRequest.getAttribute("event");
        if(event!=null) response="Invalid parameter received: "+event;
        else response="Missing parameters received. No operation performed";
        log.debug("Received event:"+event);
        if(event==null) return;
        String token=httpServletRequest.getParameter("myspecialone");
        log.debug("Received token:"+token);
        if(!pSing.getString("validity.token").equals(token)) return;


        if(ZipatoWorker.RADIO_ON.equals(event)||ZipatoWorker.RADIO_OFF.equals(event)||
                ZipatoWorker.LOCK_DOOR.equals(event)||ZipatoWorker.OPEN_DOOR.equals(event)) {
            MainController mainController = new MainController();
            response=mainController.doWork(event);
        }

        if(NetatmoWorker.EVENT_NAME.endsWith(event)) {
            NetatmoWorker netatmoWorker=new NetatmoWorker();
            NetatmoTemperatures temperatures=netatmoWorker.checkTemp();
            response="Temperatures are not available at the moment. Please check later.";
            if(temperatures!=null) {
                response="Temperature in bedroom is "+temperatures.getBedroomTemperature()+
                        " degrees and temperature in living room is "+temperatures.getLivingRoomTemperature()+
                        " degrees. Outdoor temperature is "+temperatures.getOutdoorTemperature()+
                        " degrees.";
            }
        }

        if(VPNControl.VPN_GET.equalsIgnoreCase(event)) {
            VPNControl vpnControl=new VPNControl();
            vpnControl.getVPNCode();
            response="Done.";
        }

        if(Arrays.asList(BoxControl.POSSIBLE_COMMANDS).contains(event)||event.matches("\\d+")) {
            BoxControl boxControl=new BoxControl();
            boxControl.sendBoxControl(event);
            response="Done";
        }

        if(event.startsWith(WakeOnLan.WAKE_ON_LAN)) {
            String deviceName=event.substring(event.lastIndexOf('_')+1);
            String deviceMac=pSing.getString("wake.on.lan."+deviceName);
            if(deviceMac==null) response="Unknown device received:"+deviceName;
            else {
                WakeOnLan wakeOnLan = new WakeOnLan();
                response=wakeOnLan.wakeOnLan(pSing.getString("wake.on.lan.mask"), deviceMac.toUpperCase());
            }
        }

        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("speech",response);
        jsonObject.addProperty("displayText",response);
        httpServletResponse.setContentType("application/json");
        ServletOutputStream output=httpServletResponse.getOutputStream();
        log.debug("Response to be sent:"+jsonObject.toString());
        output.print(jsonObject.toString());
        output.close();
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ServletInputStream inStream=httpServletRequest.getInputStream();
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inStream));
        StringBuffer inRead=new StringBuffer();
        String oneLine;
        while((oneLine=bufferedReader.readLine())!=null) inRead.append(oneLine).append("\n");
        inStream.close();

        Logger log= Log4JHelper.getLogger(this.getClass());
        log.debug("Received input:"+inRead.toString());
        JsonElement received=new JsonParser().parse(inRead.toString());
        JsonObject recvdObj=received.getAsJsonObject();
        JsonElement result=recvdObj.get("result");

        if(result!=null) {
            JsonElement parametersElement=result.getAsJsonObject().get("parameters");
            if(parametersElement!=null) {
                JsonElement eventEl=parametersElement.getAsJsonObject().get("event");
                if(eventEl!=null) httpServletRequest.setAttribute("event", eventEl.getAsString());
            }
        }

        doGet(httpServletRequest, httpServletResponse);
    }
}

package si.srecko.doorbird.notification.controls;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import si.srecko.doorbird.notification.helpers.Log4JHelper;
import si.srecko.doorbird.notification.helpers.PropertiesSingleton;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Srecko Mandelj on 16. 07. 2017.
 */
public class ZipatoWorker {
    Logger log= Log4JHelper.getLogger(this.getClass());
    public static final String OPEN_DOOR="openDoor";
    public static final String LOCK_DOOR="lockDoor";
    public static final String RADIO_ON="radioOn";
    public static final String RADIO_OFF="radioOff";

    private final String ZIPATO_URL="https://my.zipato.com:443/zipato-web/v2";
    PropertiesSingleton pSing=PropertiesSingleton.getInstance();

    public String operateDoor(String operation) {
        String newValue = null;
        if (LOCK_DOOR.equals(operation)) newValue = "true";
        if (OPEN_DOOR.equals(operation)) newValue = "false";
        if (RADIO_OFF.equals(operation)) newValue = "false";
        if (RADIO_ON.equals(operation)) newValue = "true";

        if (newValue == null) {
            log.info("Wrong operation received:" + operation + ", no action taken.");
            return "Wrong operation received. Please check the logic.";
        }

        String deviceState = pSing.getString("danalock.state.attribute");
        String deviceName = pSing.getString("danalock.zipato.name");

        if (RADIO_ON.equals(operation) || RADIO_OFF.equals(operation)) {
            deviceName = pSing.getString("radio.zipato.name");
        }

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(ZIPATO_URL + "/user/init");
        String sessionId = null;

        try {
            JsonParser jsonParser = new JsonParser();

            CloseableHttpResponse response = httpclient.execute(httpGet);
            String respString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            log.debug("Received after user init:" + respString);
            response.close();

            JsonElement nonceElement = jsonParser.parse(respString);
            JsonObject initObj = nonceElement.getAsJsonObject();
            String nonce = initObj.get("nonce").getAsString();
            sessionId = initObj.get("jsessionid").getAsString();

            String passSha = DigestUtils.sha1Hex(pSing.getString("zipato.password").getBytes());
            String noncePlusPass = nonce + passSha;

            String token = DigestUtils.sha1Hex(noncePlusPass);


            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair("username", pSing.getString("zipato.username")));
            params.add(new BasicNameValuePair("token", token));
            String parmUrl = URLEncodedUtils.format(params, "UTF-8");

            httpGet = new HttpGet(ZIPATO_URL + "/user/login?" + parmUrl);
            httpGet.addHeader("Cookie", "JSESSIONID=" + sessionId);
            response = httpclient.execute(httpGet);
            respString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            log.debug("Received after login:" + respString);
            response.close();

            httpGet = new HttpGet(ZIPATO_URL + "/endpoints");
            httpGet.addHeader("Cookie", "JSESSIONID=" + sessionId);
            response = httpclient.execute(httpGet);
            respString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            log.debug("Received after endpoints:" + respString);
            response.close();

            JsonElement endpoinsElement = jsonParser.parse(respString);
            JsonArray endPArr = endpoinsElement.getAsJsonArray();

            String doorURL = null;
            for (int indy = 0; indy < endPArr.size(); indy++) {
                JsonObject oneEndpoint = endPArr.get(indy).getAsJsonObject();
                JsonElement nameElement = oneEndpoint.get("name");
                if (nameElement == null) continue;
                if (deviceName.equals(nameElement.getAsString())) {
                    JsonElement linkEl = oneEndpoint.get("link");
                    if (linkEl != null) {
                        doorURL = linkEl.getAsString();
                        break;
                    }
                }
            }

            System.out.println("Found lock:" + doorURL);
            if (doorURL == null) {
                log.info("No doorlock found.");
                return "No device found for this operation.";
            }

            httpGet = new HttpGet(doorURL + "?attributes=true");
            httpGet.addHeader("Cookie", "JSESSIONID=" + sessionId);
            response = httpclient.execute(httpGet);
            respString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            log.debug("Received after endpoint attribute:" + respString);
            response.close();

            JsonElement endpointDataEl = jsonParser.parse(respString);
            JsonObject endpointObj = endpointDataEl.getAsJsonObject();
            JsonArray attribsEl = endpointObj.getAsJsonArray("attributes");
            if (attribsEl == null) {
                log.info("No attributes on doorlock.");
                return "No attributes found for device in control. Check zipato console.";
            }

            String stateAttrib = null;
            for (int indy = 0; indy < attribsEl.size(); indy++) {
                JsonObject oneObj = attribsEl.get(indy).getAsJsonObject();
                JsonElement nameAttr = oneObj.get("name");
                if (nameAttr == null) continue;
                if (deviceState.equals(nameAttr.getAsString())) {
                    JsonElement attribUID = oneObj.get("uuid");
                    if (attribUID != null) stateAttrib = attribUID.getAsString();
                    break;
                }
            }

            if (stateAttrib == null) {
                log.info("No state attribute found.");
                return "No state attribute found on device. This device can't be controlled.";
            }

            httpGet = new HttpGet(ZIPATO_URL + "/attributes/" + stateAttrib + "/value");
            httpGet.addHeader("Cookie", "JSESSIONID=" + sessionId);
            response = httpclient.execute(httpGet);
            respString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            log.debug("Received after state attrib value:" + respString);
            response.close();

            JsonObject attribVal = jsonParser.parse(respString).getAsJsonObject();
            JsonElement valEl = attribVal.get("value");
            if (valEl == null) {
                log.info("No value in doorlock attribute found.");
                return "State attribute has no value. Is this the right device we are trying to control?";
            }

            String value = valEl.getAsString();
            if ((OPEN_DOOR.equals(operation) || RADIO_OFF.equals(operation)) && "false".equals(value)) {
                log.info("Door already open, no need to lock it.");
                if (OPEN_DOOR.equals(operation)) return "Door is already opened, no need to open it again.";
                if (RADIO_OFF.equals(operation)) return "Radio is already off. No need to turn it off.";
            }

            if ((LOCK_DOOR.equals(operation) || RADIO_ON.equals(operation)) && "true".equals(value)) {
                log.info("Door already closed, no need to open it.");
                if (LOCK_DOOR.equals(operation)) return "Door is already locked. No need to lock it again.";
                if (RADIO_ON.equals(operation)) return "Radio is already on. No need to turn it on again.";
            }


            HttpPut httpPut = new HttpPut(ZIPATO_URL + "/attributes/" + stateAttrib + "/value");
            StringEntity stringEntity = new StringEntity(newValue,
                    ContentType.create("text/plain", "UTF-8"));
            httpPut.setEntity(stringEntity);
            response = httpclient.execute(httpPut);
            respString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            System.out.println("Received after set attribute:" + respString);
            response.close();
            if (OPEN_DOOR.equals(operation)) return "The door is now open.";
            if (LOCK_DOOR.equals(operation)) return "The door is now locked";
            if (RADIO_ON.equals(operation)) return "Radio is now on";
            if (RADIO_OFF.equals(operation)) return "Radio is now off";
            return "Some weird stuff happened. We did something but this was nothing.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Input output error occured. We failed to contact zipato cloud.";
        } finally {
            performLogout(httpclient, sessionId);
        }
    }

    private void performLogout(CloseableHttpClient httpClient, String sessionId) {
        if((httpClient==null)||(sessionId==null)) return;
        HttpGet httpGet=new HttpGet(ZIPATO_URL+"/user/logout");
        httpGet.addHeader("Cookie","JSESSIONID="+sessionId);
        try {
            httpClient.execute(httpGet);
        } catch (IOException e) {
            log.error("Error performing logout:", e);
        }
    }
}

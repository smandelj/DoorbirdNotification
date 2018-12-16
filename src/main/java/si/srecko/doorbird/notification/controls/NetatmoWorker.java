package si.srecko.doorbird.notification.controls;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import si.srecko.doorbird.notification.data.NetatmoTemperatures;
import si.srecko.doorbird.notification.helpers.Log4JHelper;
import si.srecko.doorbird.notification.helpers.PropertiesSingleton;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by Srecko Mandelj on 14. 08. 2017.
 */
public class NetatmoWorker {
    public static String EVENT_NAME="temperature";

    Logger log= Log4JHelper.getLogger(this.getClass());
    private final String NETATMO_URL="https://api.netatmo.com";
    PropertiesSingleton pSing=PropertiesSingleton.getInstance();

    public NetatmoTemperatures checkTemp() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(NETATMO_URL + "/oauth2/token");
        HttpEntity authEntity=MultipartEntityBuilder.create().
                addTextBody("grant_type","password").
                addTextBody("client_id",pSing.getString("netatmo.client.id")).
                addTextBody("client_secret", pSing.getString("netatmo.client.secret")).
                addTextBody("username",pSing.getString("netatmo.username")).
                addTextBody("password",pSing.getString("netatmo.password")).
                addTextBody("scope","read_station").build();
        httpPost.setEntity(authEntity);
        CloseableHttpResponse response = null;
        JsonParser jsonParser=new JsonParser();
        String deviceId=pSing.getString("netatmo.device.id");
        try {
            response = httpClient.execute(httpPost);
            String respString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            log.debug("Received after user init:" + respString);
            response.close();

            JsonElement authObj=jsonParser.parse(respString);
            String accessToken=authObj.getAsJsonObject().get("access_token").getAsString();

            HttpGet httpGet=new HttpGet(NETATMO_URL+"/api/getstationsdata?access_token="+
                    URLEncoder.encode(accessToken,"UTF-8")+
                    "&device_id="+ URLEncoder.encode(deviceId,"UTF-8"));
            response=httpClient.execute(httpGet);
            respString=EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            log.debug("Received after get station data:"+respString);
            response.close();
            JsonElement devObj=jsonParser.parse(respString);
            JsonElement bodyObj=devObj.getAsJsonObject().get("body");
            if(bodyObj==null) {
                log.debug("No body on device received.");
                return null;
            }
            JsonArray devicesObj=bodyObj.getAsJsonObject().getAsJsonArray("devices");
            if((devicesObj==null)||(devicesObj.size()==0)) {
                log.debug("No devices returned.");
                return null;
            }
            NetatmoTemperatures retVal=null;
            for(int indy=0; indy<devicesObj.size(); indy++) {
                JsonElement oneDev=devicesObj.get(indy);
                JsonElement devId=oneDev.getAsJsonObject().get("_id");
                if((devId==null)||!deviceId.equals(devId.getAsString())) continue;
                JsonElement dashboardData=oneDev.getAsJsonObject().get("dashboard_data");
                if(dashboardData==null) continue;
                JsonElement tempEl=dashboardData.getAsJsonObject().get("Temperature");
                if(tempEl==null) continue;
                retVal=new NetatmoTemperatures();
                retVal.setLivingRoomTemperature(tempEl.getAsBigDecimal());
                JsonArray arrModules=oneDev.getAsJsonObject().getAsJsonArray("modules");
                if((arrModules==null)||(arrModules.size()==0)) break;
                for(int indx=0; indx<arrModules.size(); indx++) {
                    JsonElement oneModule=arrModules.get(indx);
                    JsonElement moduleName=oneModule.getAsJsonObject().get("module_name");
                    if(moduleName==null) continue;
                    dashboardData=oneModule.getAsJsonObject().get("dashboard_data");
                    if(dashboardData==null) continue;
                    tempEl=dashboardData.getAsJsonObject().get("Temperature");
                    if(tempEl==null) continue;
                    if(pSing.getString("netatmo.bedroom.name").equals(moduleName.getAsString())) retVal.setBedroomTemperature(tempEl.getAsBigDecimal());
                    if(pSing.getString("netatmo.outdoor.name").equals(moduleName.getAsString())) retVal.setOutdoorTemperature(tempEl.getAsBigDecimal());
                }
            }
            return retVal;
        } catch (IOException e) {
            log.error("Error working with netatmo API:", e);
            return null;
        } catch (Throwable thr) {
            log.error("Error working with Netatmo;", thr);
            return null;
        }

    }

}

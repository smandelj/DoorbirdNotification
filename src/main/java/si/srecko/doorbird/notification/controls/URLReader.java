package si.srecko.doorbird.notification.controls;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import si.srecko.doorbird.notification.helpers.Log4JHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: Srecko Mandelj
 * Date: 28.4.2016
 * Time: 12:25
 * To change this template use File | Settings | File Templates.
 */
public class URLReader {
    Logger log = Log4JHelper.getLogger(this.getClass());

    public byte[] getBytesFromURL(String url, String username, String password) {
        try {
            URL contentUrl = new URL(url);
            URLConnection urlConn = contentUrl.openConnection();
            if (username != null) {
                String encoded = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
                urlConn.setRequestProperty("Authorization", "Basic " + encoded);
            }

            return readContent(urlConn);
        } catch (MalformedURLException e) {
            log.error("Error - wrong URL received.", e);
        } catch (IOException e) {
            log.error("Error - IO Exception.", e);
        }
        return null;
    }


    private byte[] readContent(URLConnection urlConn) throws IOException {
        byte[] byteBuff = new byte[4096];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream inStream = urlConn.getInputStream();
        int numBytes;
        do {
            numBytes = inStream.read(byteBuff);
            if (numBytes > 0)
                bos.write(byteBuff, 0, numBytes);
        } while (numBytes > 0);
        inStream.close();
        bos.flush();
        return bos.toByteArray();
    }

    public void urlPoster(String url, String content) {
        try {
            URL postURL = new URL(url);
            HttpURLConnection urlConn = (HttpURLConnection) postURL.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setDoOutput(true);
            OutputStream os = urlConn.getOutputStream();
            os.write(content.getBytes());
            os.flush();
            os.close();
            int respCode = urlConn.getResponseCode();
            log.debug("Sent content to " + url + ", response code:" + respCode);
            byte[] output = readContent(urlConn);
            log.debug("Received from url " + url + ":" + new String(output));
        } catch (MalformedURLException e) {
            log.error("Wrong URL received.");
        } catch (IOException e) {
            log.error("Error working with url " + url, e);
        }
    }
}
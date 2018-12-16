package si.srecko.doorbird.notification.controls;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import si.srecko.doorbird.notification.helpers.Log4JHelper;
import si.srecko.doorbird.notification.helpers.PropertiesSingleton;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Srecko Mandelj
 * Date: 28.4.2016
 * Time: 12:27
 * To change this template use File | Settings | File Templates.
 */
public class DoorbirdAction {
    PropertiesSingleton pSing = PropertiesSingleton.getInstance();
    Logger log = Log4JHelper.getLogger(this.getClass());

    public void doDoorbotAction(String event) {
        if (event == null) return;
        final String username = pSing.getString("mail.username");
        final String password =  pSing.getString("mail.password");

        Properties props = new Properties();
        props.put("mail.smtp.host", pSing.getString("mail.server"));
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        String subjectMessage = "Doorbird calling with event " + event;
        InstantMessageSender imSender = new InstantMessageSender();
        imSender.sendInstantMessage(subjectMessage);

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(pSing.getString("from.address")));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(pSing.getString("to.address")));
            message.setSubject(subjectMessage);
            Multipart multiPart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Doorbot came alive and sent email.");
            multiPart.addBodyPart(textPart);
            URLReader snapshotReader = new URLReader();
            byte[] arrImage = snapshotReader.getBytesFromURL(pSing.getString("webcam.snapshot.url"),
                    pSing.getString("webcam.username"), pSing.getString("webcam.password"));
            if ((arrImage != null) && (arrImage.length > 0)) {
                byte[] base64Encoded = Base64.encodeBase64(arrImage);

                InternetHeaders headers = new InternetHeaders();
                headers.addHeader("Content-Type", "image/jpeg");
                headers.addHeader("Content-Transfer-Encoding", "base64");


                MimeBodyPart imagePart = new MimeBodyPart(headers,
                        base64Encoded);
                imagePart.setDisposition(MimeBodyPart.INLINE);
                imagePart.setContentID("&lt;image&gt;");
                imagePart.setFileName("webcam.jpg");

                multiPart.addBodyPart(imagePart);
            }
            if (pSing.getString("doorbird.image.url") != null) {
                arrImage = snapshotReader.getBytesFromURL(
                        String.format(pSing.getString("doorbird.image.url"),
                                pSing.getString("doorbird.ip")),
                        pSing.getString("doorbird.username"), pSing.getString("doorbird.password"));
                if ((arrImage != null) && (arrImage.length > 0)) {
                    byte[] base64Encoded = Base64.encodeBase64(arrImage);

                    InternetHeaders headers = new InternetHeaders();
                    headers.addHeader("Content-Type", "image/jpeg");
                    headers.addHeader("Content-Transfer-Encoding", "base64");


                    MimeBodyPart imagePart = new MimeBodyPart(headers,
                            base64Encoded);
                    imagePart.setDisposition(MimeBodyPart.INLINE);
                    imagePart.setContentID("&lt;image&gt;");
                    imagePart.setFileName("doorbird.jpg");

                    multiPart.addBodyPart(imagePart);
                }
            }
            message.setContent(multiPart);
            Transport.send(message);

        } catch (MessagingException e) {
            log.error("Error sending mail:", e);
        }
    }

}

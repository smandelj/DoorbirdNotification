package si.srecko.doorbird.notification.controls;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import si.srecko.doorbird.notification.helpers.Log4JHelper;
import si.srecko.doorbird.notification.helpers.PropertiesSingleton;

/**
 * Created with IntelliJ IDEA.
 * User: Srecko Mandelj
 * Date: 28.4.2016
 * Time: 12:17
 * To change this template use File | Settings | File Templates.
 */
public class InstantMessageSender {
    Logger log= Log4JHelper.getLogger(this.getClass());
    PropertiesSingleton pSing=PropertiesSingleton.getInstance();

    public void sendInstantMessage(String instantMessage) {
        try {
            ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
            connConfig.setSASLAuthenticationEnabled(true);
            connConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
            XMPPConnection connection = new XMPPConnection(connConfig);
            connection.connect();

// login with username and password
            connection.login(pSing.getString("google.sendchat.user"), pSing.getString("google.sendchat.password"));



// set presence status info
            Presence presence = new Presence(Presence.Type.available);
            connection.sendPacket(presence);

            ChatManager chatManager=connection.getChatManager();
            Chat chat=chatManager.createChat(pSing.getString("google.sendchat.recipient"),null);
// send a message to somebody
            Message msg = new Message(instantMessage, Message.Type.chat);
            msg.setBody(instantMessage);
            chat.sendMessage(msg);

            presence = new Presence(Presence.Type.unavailable);
            connection.sendPacket(presence);
            connection.disconnect();
        } catch (XMPPException e) {
            log.error("Error sending instant message to google talk....",e);
        }
    }
}


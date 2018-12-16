package si.srecko.doorbird.notification.helpers;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Created with IntelliJ IDEA.
 * User: Srecko Mandelj
 * Date: 28.4.2016
 * Time: 11:27
 * To change this template use File | Settings | File Templates.
 */
public class Log4JHelper {
    public static Log4JHelper mySelf=new Log4JHelper();

    static {
        DOMConfigurator.configure(Thread.currentThread().getContextClassLoader().getResource("log4.xml"));
        Logger log=Logger.getLogger(Log4JHelper.class);
        log.debug("Log4J for DoorbirdNotifaction initialized.");
    }

    public static Logger getLogger(Class classInstance) {
        return Logger.getLogger(classInstance);
    }

    public static Logger getLogger(String logger) {
        return Logger.getLogger(logger);
    }
}

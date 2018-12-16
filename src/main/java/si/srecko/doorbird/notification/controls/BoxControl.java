package si.srecko.doorbird.notification.controls;

import si.srecko.doorbird.notification.helpers.PropertiesSingleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;

public class BoxControl {
    public static String CHANNEL_UP = "channel_up";
    public static String CHANNEL_DOWN = "channel_down";
    public static String VOLUME_UP = "volume_up";
    public static String VOLUME_DOWN = "volume_down";


    public static String[] POSSIBLE_COMMANDS =
            {
                    CHANNEL_UP,
                    CHANNEL_DOWN,
                    VOLUME_UP,
                    VOLUME_DOWN
            };

    PropertiesSingleton pSing = PropertiesSingleton.getInstance();

    public void sendBoxControl(String boxCommand) {
        URLReader urlReader = new URLReader();

        if (boxCommand.matches("\\d+")) {
            Stack<Integer> cmdStack=new Stack<Integer>();
            Integer numCommand = Integer.valueOf(boxCommand);
            while (numCommand > 0) {
                int nextCommand = numCommand % 10;
                cmdStack.push(nextCommand);
                numCommand = numCommand / 10;
            }
            while(!cmdStack.empty()) {
                Integer command=cmdStack.pop();
                urlReader.getBytesFromURL(pSing.getString("spalnica.box.url") + command,
                        null, null);
            }
        } else {
            urlReader.getBytesFromURL(pSing.getString("spalnica.box.url") + boxCommand,
                    null, null);
        }
    }
}

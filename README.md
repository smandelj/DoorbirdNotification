# DoorbirdNotification

This is a WAR project that has classes to control my smart home and send notifications. It contains REST services that are called by IFTTT or iOS shortcuts - by this I can control my devices via Alexa, Google Assistant or Siri)
- a REST service that controls Zipato zipabox z-wave controller
- a REST service that tells me the temperature of my Netatmo weather station
- a REST service that is called by DoorBird (https://www.doorbird.com/) when someone is at my door - it sends email with a picture that it takes from my camera and sends a notification to Google Talk
- a REST service to control Netgem set top box

It also needs doorbird.properties file that I didn't include into a project. This properties file contains usernames, passwords and API tokens to access cloud services (Zipato, Google Talk, Netatmo).

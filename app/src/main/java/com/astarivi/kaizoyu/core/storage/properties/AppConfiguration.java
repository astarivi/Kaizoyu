package com.astarivi.kaizoyu.core.storage.properties;

import android.content.Context;

import java.util.Random;


public class AppConfiguration extends BaseConfiguration {

    // Initializes the Hashmap and loads the properties into it.
    public AppConfiguration(Context context) {
        super(context, "Kaizoyu.properties");
        generateUsername();
    }

    public boolean getBooleanProperty(String property) {
        return Boolean.parseBoolean(properties.getProperty(property));
    }

    public void setBooleanProperty(String property, boolean value) {
        properties.setProperty(property, String.valueOf(value));
    }

    private void generateUsername(){
        String ircName = properties.getProperty("ircName");

        if (!ircName.equals("null")){
            return;
        }

        String randomChars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
        StringBuilder randomString = new StringBuilder();
        Random random = new Random();
        while (randomString.length() < 7) {
            int index = (int) (random.nextFloat() * randomChars.length());
            randomString.append(randomChars.charAt(index));
        }
        properties.setProperty("ircName", "KaiZ" + randomString);
        this.save();
    }
}

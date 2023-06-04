package com.astarivi.kaizoyu.core.storage.properties;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class BaseConfiguration {
    protected final Context context;
    protected final Properties properties;
    protected final String filename;
    protected String subFolder = "config/";

    public BaseConfiguration(Context context, String filename, String subFolder) {
        this.subFolder = subFolder;
        this.context = context;
        this.filename = filename;
        this.properties = load();
    }

    public BaseConfiguration(Context context, String filename) {
        this.context = context;
        this.filename = filename;
        this.properties = load();
    }

    public void save(){
        File propertiesFile = new File(context.getFilesDir(), subFolder + filename);
        try {
            File parentFile = propertiesFile.getParentFile();

            if (parentFile == null) return;

            // If the directory cannot be created, it will fail later.
            //noinspection ResultOfMethodCallIgnored
            parentFile.mkdirs();

            FileOutputStream fileOutput = new FileOutputStream(propertiesFile);
            this.properties.store(fileOutput, filename);
            fileOutput.close();
        } catch (IOException e) {
            Logger.debug(e);
        }
    }

    protected @NotNull Properties load() {
        File propertiesFile = new File(context.getFilesDir(), subFolder + filename);
        Properties locProperties = new Properties();
        Properties defProperties = new Properties();

        try {
            File parentFile = propertiesFile.getParentFile();

            if (parentFile == null) return defProperties;

            // If we fail, we do later, not here.
            //noinspection ResultOfMethodCallIgnored
            parentFile.mkdirs();

            if (propertiesFile.exists()) {
                FileInputStream fileis = new FileInputStream(propertiesFile);
                locProperties.load(fileis);
                fileis.close();
                if (locProperties.size() != 0) {
                    return locProperties;
                }
            }

            defProperties.load(context.getAssets().open(filename));

            FileOutputStream fileOutput = new FileOutputStream(propertiesFile);
            defProperties.store(fileOutput, filename);
            fileOutput.close();

        } catch (IOException ignored) {

        }
        return defProperties;
    }

    public Properties getConfiguration() {
        return properties;
    }
}

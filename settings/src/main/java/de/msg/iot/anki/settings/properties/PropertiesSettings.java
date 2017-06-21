package de.msg.iot.anki.settings.properties;


import de.msg.iot.anki.settings.Settings;
import de.msg.iot.anki.settings.SettingsException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesSettings implements Settings {

    private final Properties properties;
    private final String resource;

    private PropertiesSettings(Properties properties, String resource) {
        this.properties = properties;
        this.resource = resource;
    }

    public PropertiesSettings(String resource) {
        this(new Properties(), resource);

        try (InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream(resource)) {

            this.properties.load(in);

        } catch (Exception e) {
            throw new SettingsException("Cannot load properties [" + resource + "].");
        }
    }

    @Override
    public String get(String key) {
        return this.properties.getProperty(key);
    }

    @Override
    public void set(String key, String value) {
        try (OutputStream out = new FileOutputStream(
                new File(
                        getClass()
                                .getClassLoader()
                                .getResource(resource)
                                .getFile()
                )
        )) {

            this.properties.setProperty(key, value);
            this.properties.store(out, "");

        } catch (Exception e) {
            throw new SettingsException("Cannot save property [" + key + "=" + value + "].");
        }
    }
}
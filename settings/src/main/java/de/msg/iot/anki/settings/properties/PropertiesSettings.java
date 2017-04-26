package de.msg.iot.anki.settings.properties;


import de.msg.iot.anki.settings.Settings;
import de.msg.iot.anki.settings.SettingsException;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesSettings implements Settings {

    private final Properties properties;

    private PropertiesSettings(Properties properties) {
        this.properties = properties;
    }

    public PropertiesSettings(String resource) {
        this(new Properties());

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
}
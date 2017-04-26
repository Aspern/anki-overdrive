package de.msg.iot.anki.settings.properties;

import de.msg.iot.anki.settings.SettingsException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class PropertiesSettingsTest {

    public static class PropertiesSettingsImpl extends PropertiesSettings {

        PropertiesSettingsImpl(String resource) {
            super(resource);
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void constructor() {
        exception.expect(SettingsException.class);
        new PropertiesSettingsImpl("invalid");
    }

    @Test
    public void get() throws Exception {
        PropertiesSettingsImpl settings = new PropertiesSettingsImpl("sample-properties-settings.properties");
        assertEquals(settings.get("key"), "value");
        assertEquals(settings.getAsInt("int", 0), new Integer(42));
    }

}
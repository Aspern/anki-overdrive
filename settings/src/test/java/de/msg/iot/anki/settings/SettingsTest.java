package de.msg.iot.anki.settings;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class SettingsTest {

    private static SettingsImpl settings;

    public static class SettingsImpl implements Settings {

        private final Map<String, String> settings;

        SettingsImpl() {
            this.settings = new HashMap<String, String>() {{
                put("key", "value");
                put("int", "4711");
                put("long", "82");
                put("float", "17.5");
                put("double", "42.4711");
                put("boolean", "true");
                put("invalid", "47asd");
            }};
        }

        @Override
        public String get(String key) {
            return this.settings.get(key);
        }

        @Override
        public void set(String key, String value) {
            this.settings.put(key, value);
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() {
        settings = new SettingsImpl();
    }

    @Test
    public void get() throws Exception {
        assertEquals(settings.get("key"), "value");
        assertEquals(settings.get("no-key"), null);
    }

    @Test
    public void set() throws Exception {
        settings.set("hello", "world");
        assertEquals(settings.get("hello"), "world");
    }

    @Test
    public void getAsString() throws Exception {
        assertEquals(settings.get("key", "default"), "value");
        assertEquals(settings.get("no-key", "default"), "default");
    }

    @Test
    public void getAsInt() throws Exception {
        assertEquals(settings.getAsInt("int", 42), new Integer(4711));
        assertEquals(settings.getAsInt("no-int", 42), new Integer(42));
        exception.expect(SettingsException.class);
        settings.getAsInt("invalid", 42);
    }

    @Test
    public void getAsLong() throws Exception {
        assertEquals(settings.getAsLong("long", 42L), new Long(82));
        assertEquals(settings.getAsLong("no-long", 42L), new Long(42));
        exception.expect(SettingsException.class);
        settings.getAsLong("invalid", 42L);
    }

    @Test
    public void getAsFloat() throws Exception {
        assertEquals(settings.getAsFloat("float", 42f), new Float(17.5));
        assertEquals(settings.getAsFloat("no-float", 42f), new Float(42));
        exception.expect(SettingsException.class);
        settings.getAsFloat("invalid", 42f);
    }

    @Test
    public void getAsDouble() throws Exception {
        assertEquals(settings.getAsDouble("double", 42d), new Double(42.4711));
        assertEquals(settings.getAsDouble("no-double", 42d), new Double(42));
        exception.expect(SettingsException.class);
        settings.getAsDouble("invalid", 42d);
    }

    @Test
    public void getAsBoolean() throws Exception {
        assertEquals(settings.getAsBoolean("boolean", false), Boolean.TRUE);
        assertEquals(settings.getAsBoolean("no-boolean", false), Boolean.FALSE);
    }

}

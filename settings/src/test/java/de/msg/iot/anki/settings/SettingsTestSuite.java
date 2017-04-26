package de.msg.iot.anki.settings;


import de.msg.iot.anki.settings.properties.PropertiesSettingsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SettingsTest.class,
        PropertiesSettingsTest.class
})
public class SettingsTestSuite {
}
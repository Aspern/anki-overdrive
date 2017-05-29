package de.msg.iot.anki.anticollision;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class AntiCollisionApplicationTest {


    @Test
    public void start() throws Exception {

    }

    @Test
    public void stop() throws Exception {

    }

    @Test
    public void addListener() throws Exception {

    }

    @Test
    public void name() throws Exception {
        AntiCollisionApplication application = new AntiCollisionApplication();
        assertEquals(application.name(), application.getClass().getSimpleName());
    }

}
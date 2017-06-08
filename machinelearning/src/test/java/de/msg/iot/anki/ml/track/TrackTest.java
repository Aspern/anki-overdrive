package de.msg.iot.anki.ml.track;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TrackTest {

    private static Track track;

    @BeforeClass
    public static void beforeClass() {
        track = Track.builder()
                .addCurve(1)
                .addCurve(2)
                .addStraight(3)
                .addCurve(4)
                .addCurve(5)
                .build();
    }


    @Test
    public void getStart() throws Exception {
        assertNotNull(track.getStart());
        assertTrue(track.getStart() instanceof Start);
    }

    @Test
    public void getFinish() throws Exception {
        assertNotNull(track.getFinish());
        assertTrue(track.getFinish() instanceof Finish);
    }

    @Test
    public void eachPieceWithLocations() throws Exception {

        for (int i = 0; i < 16; i++) {
            track.eachPieceWithLocations(i, (piece, location) -> {
                assertNotNull(piece);
                assertNotNull(location);
            });
        }
    }

    @Test
    public void builder() throws Exception {
        Track track = Track.builder()
                .addCurve(1)
                .addCurve(2)
                .addStraight(3)
                .addCurve(4)
                .addCurve(5)
                .build();

        Piece current = track.getStart()
                .getNext();
        int i = 1;

        while (current != track.getFinish()) {
            assertEquals(current.getId(), i++);
            current = current.getNext();
        }
    }

}
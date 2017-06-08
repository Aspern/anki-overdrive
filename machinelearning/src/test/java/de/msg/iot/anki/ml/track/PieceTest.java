package de.msg.iot.anki.ml.track;

import org.junit.Test;

import static org.junit.Assert.*;


public class PieceTest {

    public static Piece piece = new Straight(1);

    @Test
    public void eachLane() throws Exception {
        piece.eachLane(lanes -> assertEquals(lanes.length, 3));
    }

    @Test
    public void eachLocation() throws Exception {
        for (int i = 0; i < 16; i++) {
            piece.eachLocation(i, location -> assertNotNull(location));
        }
    }

    @Test
    public void eachLocation1() throws Exception {
        piece.eachLocation(location -> assertNotNull(location));
    }

    @Test
    public void getId() throws Exception {
        assertEquals(piece.getId(), 1);
    }

    @Test
    public void getLanes() throws Exception {
        assertNotNull(piece.getLanes());
        assertEquals(piece.getLanes().length, 16);
    }

    @Test
    public void getNext() throws Exception {
        Curve curve = new Curve(2);
        piece.setNext(curve);
        assertEquals(curve, piece.getNext());
    }

    @Test
    public void setNext() throws Exception {
        Curve curve = new Curve(2);
        piece.setNext(curve);
        assertEquals(curve, piece.getNext());
    }

    @Test
    public void getPrevious() throws Exception {
        Curve curve = new Curve(2);
        piece.setPrevious(curve);
        assertEquals(curve, piece.getPrevious());
    }

    @Test
    public void setPrevious() throws Exception {
        Curve curve = new Curve(2);
        piece.setPrevious(curve);
        assertEquals(curve, piece.getPrevious());
    }

}
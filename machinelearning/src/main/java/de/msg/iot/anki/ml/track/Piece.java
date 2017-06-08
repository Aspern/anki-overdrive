package de.msg.iot.anki.ml.track;

import java.util.function.Consumer;

public abstract class Piece {

    protected final int id;
    protected final int[][] lanes;
    protected Piece next;
    protected Piece previous;

    public Piece(int id, int[][] lanes) {
        this.id = id;
        this.lanes = lanes;
    }

    public void eachLane(Consumer<int[]> consumer) {
        for (int[] lane : lanes) {
            consumer.accept(lane);
        }
    }

    public void eachLocation(int lane, Consumer<Integer> consumer) {
        for (int location : lanes[lane]) {
            consumer.accept(location);
        }
    }

    public void eachLocation(Consumer<Integer> consumer) {
        for (int i = 0; i < lanes.length; i++)
            eachLocation(i, consumer);
    }

    public int getId() {
        return id;
    }

    public int[][] getLanes() {
        return lanes;
    }

    public Piece getNext() {
        return next;
    }

    public void setNext(Piece next) {
        this.next = next;
    }

    public Piece getPrevious() {
        return previous;
    }

    public void setPrevious(Piece previous) {
        this.previous = previous;
    }
}
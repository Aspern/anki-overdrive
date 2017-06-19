package de.msg.iot.anki.core.track;

import java.util.function.BiConsumer;

public class Track {

    private final Start start;
    private final Finish finish;

    public Track() {
        this.start = new Start();
        this.finish = new Finish();

        start.setPrevious(finish);
        finish.setNext(start);
    }

    public Start getStart() {
        return start;
    }

    public Finish getFinish() {
        return finish;
    }

    public void eachPieceWithLocations(int lane, BiConsumer<Integer, Integer> consumer) {
        Piece current = this.getStart();

        do {
            final int piece = current.getId();
            current.eachLocation(lane, location -> {
                consumer.accept(piece, location);
            });
            current = current.getNext();
        } while (current != this.getStart());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Track track;
        private Piece current;

        public Builder() {
            this.track = new Track();
            this.current = track.getStart();
        }

        public Builder addCurve(int id) {
            Curve curve = new Curve(id);
            current.setNext(curve);
            current = curve;
            return this;
        }

        public Builder addStraight(int id) {
            Straight straight = new Straight(id);
            current.setNext(straight);
            current = straight;
            return this;
        }

        public Track build() {
            current.setNext(track.getFinish());
            track.getFinish().setPrevious(current);
            return track;
        }

    }

}
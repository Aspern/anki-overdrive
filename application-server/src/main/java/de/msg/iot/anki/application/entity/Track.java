package de.msg.iot.anki.application.entity;


import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@Entity
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;

    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Piece> pieces;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }
}
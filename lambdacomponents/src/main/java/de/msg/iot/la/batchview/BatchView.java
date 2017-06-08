package de.msg.iot.la.batchview;


import java.util.Collection;
import java.util.function.Consumer;

public interface BatchView<D> extends Runnable{

    Collection<D> fetch();

    void forEach(Consumer<D> consumer);

    void drop();

}

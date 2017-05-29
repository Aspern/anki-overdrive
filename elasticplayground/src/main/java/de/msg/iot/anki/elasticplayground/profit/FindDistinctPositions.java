package de.msg.iot.anki.elasticplayground.profit;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;


public class FindDistinctPositions implements Callable<List<String>> {

    private final List<String> distinctPositions;
    private final EntityManager mysql;

    public FindDistinctPositions() {
        this.distinctPositions = new ArrayList<>();
        this.mysql = Persistence.createEntityManagerFactory("anki")
                .createEntityManager();
    }

    @Override
    public List<String> call() throws Exception {
        mysql.getTransaction().begin();
        List<Object> result = mysql.createNativeQuery("SELECT DISTINCT position FROM QUALITYENTRY")
                .getResultList();
        distinctPositions.addAll(
                result.stream()
                        .map(o -> o.toString())
                        .collect(Collectors.toList())
        );
        mysql.getTransaction().commit();

        return distinctPositions;
    }

}

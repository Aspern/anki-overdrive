package de.msg.iot.anki.application.rest;

import de.msg.iot.anki.application.entity.Setup;
import de.msg.iot.anki.application.entity.Vehicle;
import de.msg.iot.anki.application.kafka.ScenarioKafkaProducer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/setup")
public class SetupRestHandler {


    private static final ScenarioKafkaProducer scenarioProducer = new ScenarioKafkaProducer();
    private static final List<String> scenarios = new ArrayList<String>() {{
        add("collision");
        add("anti-collision");
    }};


    private final EntityManagerFactory factory = Persistence.createEntityManagerFactory("anki");
    private final EntityManager manager = factory.createEntityManager();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        @SuppressWarnings("unchecked") List<Setup> list = manager.createQuery("select d from Setup d")
                .getResultList();

        return Response.ok(list).build();
    }

    @GET
    @Path("/{setupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("setupId") String setupId) {
        Setup setup = manager.find(Setup.class, setupId);

        if (setup == null)
            return Response.status(404).build();

        return Response.ok(setup).build();
    }

    @GET
    @Path("/{setupId}/vehicle")
    @Produces(MediaType.APPLICATION_JSON)
    public Response vehicle(@PathParam("setupId") String setupId) {
        Setup setup = manager.find(Setup.class, setupId);

        if (setup == null)
            return Response.status(404).build();

        return Response.ok(setup.getVehicles()).build();
    }

    @GET
    @Path("/{setupId}/vehicle/{vehicleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response vehicle(@PathParam("setupId") String setupId, @PathParam("vehicleId") String vehicleId) {
        Setup setup = manager.find(Setup.class, setupId);

        if (setup == null)
            return Response.status(404).build();

        for (Vehicle vehicle : setup.getVehicles()) {
            if (vehicleId.equals(vehicle.getUuid()))
                return Response.ok(vehicle).build();
        }

        return Response.status(404).build();
    }


    @GET
    @Path("/{setupId}/track")
    @Produces(MediaType.APPLICATION_JSON)
    public Response track(@PathParam("setupId") String setupId) {
        Setup setup = manager.find(Setup.class, setupId);

        if (setup == null || setup.getTrack() == null)
            return Response.status(404).build();

        return Response.ok(setup.getTrack()).build();
    }


    @GET
    @Path("/{setupId}/scenario")
    @Produces(MediaType.APPLICATION_JSON)
    public Response scenario(@PathParam("setupId") String setupId) {
        Setup setup = manager.find(Setup.class, setupId);

        if (setup == null)
            return Response.status(404).build();

        return Response.ok(scenarios).build();
    }

    @POST
    @Path("/{setupId}/scenario/{name}/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startScenario(@PathParam("setupId") String setupId, @PathParam("name") String name) {
        Setup setup = manager.find(Setup.class, setupId);

        if (setup == null || !scenarios.contains(name))
            return Response.status(404).build();

        switch (name) {
            case "collision":
                scenarioProducer.startCollision();
                return Response.ok().build();
            case "anti-collision":
                scenarioProducer.startAntiCollision();
                return Response.ok().build();
            default:
                return Response.status(404).build();
        }
    }

    @POST
    @Path("/{setupId}/scenario/{name}/interrupt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response interruptScenario(@PathParam("setupId") String setupId, @PathParam("name") String name) {
        Setup setup = manager.find(Setup.class, setupId);

        if (setup == null || !scenarios.contains(name))
            return Response.status(404).build();

        switch (name) {
            case "collision":
                scenarioProducer.stopCollision();
                return Response.ok().build();
            case "anti-collision":
                scenarioProducer.stopAntiCollision();
                return Response.ok().build();
            default:
                return Response.status(404).build();
        }
    }

}
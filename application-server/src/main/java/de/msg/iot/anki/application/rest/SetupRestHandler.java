package de.msg.iot.anki.application.rest;

import de.msg.iot.anki.application.entity.Setup;
import de.msg.iot.anki.application.entity.Vehicle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/setup")
public class SetupRestHandler {


    //    private static KafkaScenarioController scenarioController = new KafkaScenarioController();
//    private static Map<String, KafkaVehicleController> controller = new HashMap<>();
    private static final List<String> scenarios = new ArrayList<String>() {{
        add("collision");
        add("anti-collision");
        add("max-speed");
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

//    @POST
//    @Path("/{setupId}/vehicle/{vehicleId}/set-speed")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response setSpeed(@PathParam("setupId") String setupId, @PathParam("vehicleId") String vehicleId, @QueryParam("speed") int speed, @QueryParam("acceleration") int acceleration) {
//        Response response = vehicle(setupId, vehicleId);
//
//        if (response.getStatus() != 200)
//            return Response.status(response.getStatus()).build();
//
//        try {
//            Vehicle vehicle = response.readEntity(Vehicle.class);
//            controller(vehicle.getUuid()).setSpeed(speed, acceleration);
//            return Response.ok().build();
//        } catch (Exception e) {
//            return Response.status(500).entity(e).build();
//        }
//    }

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
//
//    @POST
//    @Path("/{setupId}/scenario/{name}/start")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response startScenario(@PathParam("setupId") String setupId, @PathParam("name") String name) {
//        Setup setup = manager.find(Setup.class, setupId);
//
//        if (setup == null || !scenarios.contains(name))
//            return Response.status(404).build();
//
//        switch (name) {
//            case "collision":
//                scenarioController.collisionScenario(false);
//                return Response.ok().build();
//            case "anti-collision":
//                scenarioController.antiCollisionScenario(false);
//                return Response.ok().build();
//            case "max-speed":
//                scenarioController.maxSpeedScenario(false);
//                return Response.ok().build();
//            default:
//                return Response.status(404).build();
//        }
//    }

//    @POST
//    @Path("/{setupId}/scenario/{name}/interrupt")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response interruptScenario(@PathParam("setupId") String setupId, @PathParam("name") String name) {
//        Setup setup = manager.find(Setup.class, setupId);
//
//        if (setup == null || !scenarios.contains(name))
//            return Response.status(404).build();
//
//        switch (name) {
//            case "collision":
//                scenarioController.collisionScenario(true);
//                return Response.ok().build();
//            case "anti-collision":
//                scenarioController.antiCollisionScenario(true);
//                return Response.ok().build();
//            case "max-speed":
//                scenarioController.maxSpeedScenario(true);
//                return Response.ok().build();
//            default:
//                return Response.status(404).build();
//        }
//    }


//    private KafkaVehicleController controller(String uuid) {
//        if (!controller.containsKey(uuid))
//            controller.put(uuid, new KafkaVehicleController(uuid));
//
//        return controller.get(uuid);
//    }
}
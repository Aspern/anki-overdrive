package de.msg.iot.anki.application.rest;

import de.msg.iot.anki.application.entity.Setup;
import de.msg.iot.anki.application.entity.Vehicle;
import de.msg.iot.anki.application.entity.VehicleCommand;
import de.msg.iot.anki.application.kafka.ScenarioKafkaProducer;
import de.msg.iot.anki.application.kafka.VehicleCommandKafkaProducer;
import de.msg.iot.anki.spark.anticollision.AntiCollision;
import org.apache.hadoop.util.hash.Hash;
import org.apache.log4j.Logger;
import scala.Int;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/setup")
public class SetupRestHandler {


    private static final ScenarioKafkaProducer scenarioProducer = new ScenarioKafkaProducer();
    private static AntiCollision antiCollision = new AntiCollision();
    private static final List<String> scenarios = new ArrayList<String>() {{
        add("collision");
        add("anti-collision");
        add("product-improvement");
    }};
    private final EntityManagerFactory factory = Persistence.createEntityManagerFactory("anki");
    private final EntityManager manager = factory.createEntityManager();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        @SuppressWarnings("unchecked")
        List<Setup> list = manager.createQuery("select d from Setup d")
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

    @POST
    @Path("/{setupId}/vehicle/{vehicleId}/connect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response connect(@PathParam("setupId") String setupId, @PathParam("vehicleId") String vehicleId) {
        Setup setup = manager.find(Setup.class, setupId);

        System.out.println("Connect " + vehicleId + " @" + setupId);

        if (setup == null)
            return Response.status(404).build();

        for (Vehicle vehicle : setup.getVehicles()) {
            if (vehicleId.equals(vehicle.getUuid())) {
                final VehicleCommandKafkaProducer producer = new VehicleCommandKafkaProducer(vehicleId);
                producer.sendMessage(
                        new VehicleCommand("connect")
                );
                manager.getTransaction().begin();
                vehicle.setConnected(true);
                manager.getTransaction().commit();
                return Response.ok().build();
            }
        }

        return Response.status(404).build();
    }

    @POST
    @Path("/{setupId}/vehicle/{vehicleId}/disconnect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disconnect(@PathParam("setupId") String setupId, @PathParam("vehicleId") String vehicleId) {
        Setup setup = manager.find(Setup.class, setupId);

        System.out.println("Disconnect " + vehicleId + " @" + setupId);

        if (setup == null)
            return Response.status(404).build();

        for (Vehicle vehicle : setup.getVehicles()) {
            if (vehicleId.equals(vehicle.getUuid())) {
                final VehicleCommandKafkaProducer producer = new VehicleCommandKafkaProducer(vehicleId);
                producer.sendMessage(
                        new VehicleCommand("disconnect")
                );
                manager.getTransaction().begin();
                vehicle.setConnected(false);
                manager.getTransaction().commit();
                return Response.ok().build();
            }
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
    public Response startScenario(@PathParam("setupId") String setupId, @PathParam("name") String name,
                                  @QueryParam("speed_GS") int speedGroundShock, @QueryParam("speed_SK") int speedSkull,
                                  @QueryParam("lane") int lane, @QueryParam("break") int accelerationBrake,
                                  @QueryParam("accel") int accelerationSpeedUp, @QueryParam("distance") int distance,
                                  @QueryParam("quality") int quality, @QueryParam("improve") boolean improve) {

        Setup setup = manager.find(Setup.class, setupId);


        if (setup == null || !scenarios.contains(name))
            return Response.status(404).build();

        switch (name) {
            case "collision":
                scenarioProducer.startCollision();
                return Response.ok().build();
            case "anti-collision":
                return startAntiCollisionScenario(
                        setup,
                        speedGroundShock,
                        speedSkull,
                        distance,
                        accelerationSpeedUp,
                        accelerationBrake
                );
            case "product-improvement":
                scenarioProducer.startProductImprovement();
                return Response.ok().build();
            default:
                return Response.status(404).build();
        }
    }

    private Response startAntiCollisionScenario(Setup setup, int speedGroundShock, int speedSkull,
                                                int distance, int accelerationSpeedUp, int accelerationBrake) {
        if (setup.getVehicles().size() < 2)
            return Response.status(500)
                    .entity("Cannot start anti-collision scenario with less then 2 vehicles.")
                    .build();

        try {
            Vehicle vehicle1 = setup.getVehicles().get(0);
            Vehicle vehicle2 = setup.getVehicles().get(1);
            VehicleCommandKafkaProducer producer1 = new VehicleCommandKafkaProducer(vehicle1.getUuid());
            VehicleCommandKafkaProducer producer2 = new VehicleCommandKafkaProducer(vehicle2.getUuid());

            producer1.sendMessage(new VehicleCommand("set-speed", 400));
            Thread.sleep(1000);
            producer2.sendMessage(new VehicleCommand("set-speed", 400));
            Thread.sleep(2000);
            producer1.sendMessage(new VehicleCommand("change-lane", 68.0));
            producer2.sendMessage(new VehicleCommand("change-lane", 68.0));
            Thread.sleep(1500);

            String idGroundShock = null;
            String idSkull = null;

            if (vehicle1.getName().equals("Ground Shock")) {
                idGroundShock = vehicle1.getUuid();
                idSkull = vehicle2.getUuid();
            } else {
                idGroundShock = vehicle2.getUuid();
                idSkull = vehicle1.getUuid();
            }

            final Map<String, Integer> store = new HashMap<>();
            store.put(idGroundShock, speedGroundShock);
            store.put(idSkull, speedSkull);

            antiCollision.setParameters(
                    distance,
                    store,
                    accelerationBrake,
                    accelerationSpeedUp
            );

            antiCollision.start();
            return Response.ok().build();

        } catch (Exception exception) {
            return Response.status(500)
                    .entity(exception)
                    .build();
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
                System.out.println("calling stop");
                antiCollision.stop();
                return Response.ok().build();
            case "product-improvement":
                scenarioProducer.stopProductImprovment();
                return Response.ok().build();
            default:
                return Response.status(404).build();
        }
    }

}
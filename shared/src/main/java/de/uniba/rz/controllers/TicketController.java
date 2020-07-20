package de.uniba.rz.controllers;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;
import de.uniba.rz.services.TicketService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.logging.Logger;


@Path("tickets")
@Singleton
public class TicketController {
    private final static Logger logger = Logger.getLogger(TicketController.class.getName());



    public TicketController() {

    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewTicket(Ticket newTicket)  {
        if (newTicket == null) {
            logger.info("Ticket not found   "+Response.Status.NOT_FOUND);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Ticket mapTicket= newTicket.MapToTicket();
        //TicketService ts= new TicketService();
        Ticket ticket= TicketService.getInstance().storeNewTicket(mapTicket);

        return Response
                .status(Response.Status.OK)
                .entity(new Ticket(ticket))
                .type(MediaType.APPLICATION_JSON)
                .build();

    }

    @PUT
    @Path("/update/{ticketId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTicket(Ticket ticket, @PathParam("ticketId") final int id){
        if (ticket == null) {
            logger.severe("Invalid request body   "+Response.Status.BAD_REQUEST);
            throw new WebApplicationException("Invalid request body",Response.Status.BAD_REQUEST); //400 status code
        }
        Ticket mapTicket= ticket.MapToTicket();
        //TicketService ts= new TicketService();
        Ticket gotTicket= TicketService.getInstance().getTicketbyId(id);
        if(gotTicket == null){
            logger.warning("Ticket not found with ID: " + id+ "    "+ Response.Status.NOT_FOUND);
            throw new WebApplicationException("Ticket not found with ID: " + id, 404);
        }
        Ticket updatedTicket = TicketService.getInstance().updateTicket(mapTicket,gotTicket);
        System.out.println("Updated:  " +updatedTicket);
        return Response.status(Response.Status.OK)
                .entity(new Ticket(updatedTicket))
                .type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTickets(){
        //TicketService ts= new TicketService();
        if(TicketService.getInstance().getAllTickets().isEmpty()){
            logger.info("Currently no tickets exist, please create a ticket  "+Response.Status.NOT_FOUND);
        }
        String allTicks=TicketService.getInstance().getAllTickets().toString();
        System.out.println("All tickets:  "+allTicks);
        return Response
                .status(Response.Status.OK)
                .entity(allTicks)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("/searchname")
    public String getTicketsByName(@DefaultValue("Harry") @QueryParam("name") String name){
        //TicketService ts= new TicketService();
        if(TicketService.getInstance().getTicketsByName(name).isEmpty()){
            logger.warning("Searched ticket not found  "+Response.Status.NOT_FOUND);
            throw new WebApplicationException("Searched ticket not found ", 404);
        }
        return TicketService.getInstance().getTicketsByName(name).toString();

    }

    @GET
    @Path("/searchtype")
    public String getTicketsByType(@DefaultValue("TASK") @QueryParam("type") Type type){
        //TicketService ts= new TicketService();
        if(TicketService.getInstance().getTicketsByType(type).isEmpty()){
            logger.warning("Searched ticket not found  "+Response.Status.NOT_FOUND);
            throw new WebApplicationException("Searched ticket not found ", 404);
        }
        return TicketService.getInstance().getTicketsByType(type).toString();


    }
}

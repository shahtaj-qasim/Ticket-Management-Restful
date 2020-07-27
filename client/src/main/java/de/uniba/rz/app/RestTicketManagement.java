package de.uniba.rz.app;


import de.uniba.rz.entities.*;
import de.uniba.rz.services.TicketService;

import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class RestTicketManagement implements TicketManagementBackend, TicketSearchBackend{

    HashMap<Integer, Ticket> tickets = TicketService.getTicketInstance();

    private AtomicInteger ticketId;

    public RestTicketManagement() {
        ticketId = new AtomicInteger(1);
    }
    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) throws TicketException {
        Ticket newTicket = new Ticket(ticketId.getAndIncrement(), reporter, topic, description, type, priority);
        tickets.put(newTicket.getId(), newTicket);

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://localhost:9999/tickets").path("create");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response
                = invocationBuilder
                .post(Entity.entity(newTicket, MediaType.APPLICATION_JSON));
        System.out.println("Web Service - Created Status   "+response.getStatus());
       System.out.println("Created ticket:  "+response.readEntity(Ticket.class));

       return (Ticket) newTicket.clone();

    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://localhost:9999/tickets").path("all");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        String response = invocationBuilder.get(String.class);

        System.out.println("All Tickets: "+response);

        return tickets.entrySet().stream().map(entry -> (Ticket) entry.getValue().clone())
                .collect(Collectors.toList());

        //return list;
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {

        Ticket ticket=TicketService.getInstance().getTicketbyId(id);
        return ticket;

    }

    private Ticket getTicketByIdInteral(int id) throws TicketException {
        Ticket ticket= getTicketById(id);
        return ticket;
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        Ticket ticketToModify = getTicketByIdInteral(id);
        if (ticketToModify.getStatus() != Status.NEW) {
            throw new TicketException(
                    "Can not accept Ticket as it is currently in status " + ticketToModify.getStatus());
        }

        ticketToModify.setStatus(Status.ACCEPTED);
        updateTicketClient(id,ticketToModify);
        return (Ticket) ticketToModify.clone();
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        Ticket ticketToModify = getTicketByIdInteral(id);
        if (ticketToModify.getStatus() != Status.NEW) {
            throw new TicketException(
                    "Can not reject Ticket as it is currently in status " + ticketToModify.getStatus());
        }

        ticketToModify.setStatus(Status.REJECTED);
        updateTicketClient(id,ticketToModify);
        return (Ticket) ticketToModify.clone();
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        Ticket ticketToModify = getTicketByIdInteral(id);
        if (ticketToModify.getStatus() != Status.ACCEPTED) {
            throw new TicketException(
                    "Can not close Ticket as it is currently in status " + ticketToModify.getStatus());
        }

        ticketToModify.setStatus(Status.CLOSED);
        updateTicketClient(id,ticketToModify);
        return (Ticket) ticketToModify.clone();
    }

    public void updateTicketClient(int id, Ticket ticketToModify){
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://localhost:9999/tickets").path("update/"+id);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response
                = invocationBuilder
                .put(Entity.entity(ticketToModify, MediaType.APPLICATION_JSON));
        System.out.println("Web Service - Updated Status   "+response.getStatus());
        System.out.println("Updated ticket:  "+response.readEntity(Ticket.class));
    }
    @Override
    public void triggerShutdown() {

    }

    //search implementation
    @Override
    public List<Ticket> getTicketsByName(String name, int offset, int limit) throws TicketException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://localhost:9999/tickets").path("searchname").queryParam("name",name)
                .queryParam("offset",offset).queryParam("limit",limit);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        String response = invocationBuilder.get(String.class);

        System.out.println("Searched tickets (with topic/reporter/description: "+name+"): "+response);

        return tickets.entrySet().stream().map(entry -> (Ticket) entry.getValue().clone())
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> getTicketsByNameAndType(String name, Type type, int offset, int limit) throws TicketException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://localhost:9999/tickets").path("searchtype")
                .queryParam("type",type).queryParam("offset",offset).queryParam("limit",limit);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        String response = invocationBuilder.get(String.class);

        System.out.println("Searched tickets (with type: "+type+"): "+response);

        return tickets.entrySet().stream().map(entry -> (Ticket) entry.getValue().clone())
                .collect(Collectors.toList());
    }
}

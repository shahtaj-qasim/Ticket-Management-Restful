package de.uniba.rz.backend.services;

import de.uniba.rz.backend.TicketStore;
import de.uniba.rz.backend.UnknownTicketException;
import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Status;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketService implements TicketStore {

    HashMap<Integer, Ticket> tickets = new HashMap<>();
    private AtomicInteger ticketId;

    public TicketService() {
        ticketId = new AtomicInteger(1);
    }

    @Override
    public Ticket storeNewTicket(String reporter, String topic, String description, Type type, Priority priority) {
        Ticket newTicket = new Ticket(ticketId.getAndIncrement(), reporter, topic, description, type, priority);
        tickets.put(newTicket.getId(), newTicket);
        //System.out.println("Created new Ticket from Reporter: " + reporter + " with the topic \"" + topic + "\"");
        System.out.println(newTicket.toString());
        return newTicket;

    }

    @Override
    public void updateTicketStatus(int ticketId, Status newStatus) throws UnknownTicketException, IllegalStateException {

    }

    @Override
    public List<Ticket> getAllTickets() {
        return null;
    }
}

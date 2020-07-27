package de.uniba.rz.backend;

import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Status;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;
import de.uniba.rz.services.TicketService;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RestTicketStore implements TicketStore {
    HashMap<Integer, Ticket> tickets = new HashMap<>();
    private AtomicInteger ticketId;

    public RestTicketStore() {
        ticketId = new AtomicInteger(1);
    }

    @Override
    public Ticket storeNewTicket(String reporter, String topic, String description, Type type, Priority priority) {
        Ticket newTicket = new Ticket(ticketId.getAndIncrement(),reporter, topic, description, type, priority);

        TicketService t= new TicketService();
        t.storeNewTicket(newTicket);
        return null;
    }

    @Override
    public void updateTicketStatus(int ticketId, Status newStatus) throws UnknownTicketException, IllegalStateException {

    }

    @Override
    public List<Ticket> getAllTickets() {
        return null;
    }
}

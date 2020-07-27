package de.uniba.rz.backend;

import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Status;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;

import java.util.ArrayList;
import java.util.List;

public class grpcTicketStore implements TicketStore {

    private int nextTicketId = 1;
    private List<Ticket> ticketList = new ArrayList<>();

    @Override
    public Ticket storeNewTicket(String reporter, String topic, String description, Type type, Priority priority) {
        //System.out.println("Creating new Ticket from Reporter: " + reporter + " with the topic \"" + topic + "\"");
        Ticket newTicket = new Ticket(nextTicketId++, reporter, topic, description, type, priority);
        ticketList.add(newTicket);
        System.out.println(newTicket.toString());
        return newTicket;
    }

    @Override
    public void updateTicketStatus(int ticketId, Status newStatus) throws UnknownTicketException, IllegalStateException {
        for (Ticket ticket : ticketList) {
            if (ticket.getId() == ticketId) {
                ticket.setStatus(newStatus);
            }
        }
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketList;
    }
}

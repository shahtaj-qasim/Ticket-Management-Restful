package de.uniba.rz.services;


import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class TicketService {

    private static final HashMap<Integer, Ticket> tickets = new HashMap<>();
    private static AtomicInteger ticketId;
    private static TicketService instance;

    public TicketService() {
        ticketId = new AtomicInteger(1);
    }

    public static synchronized HashMap<Integer, Ticket> getTicketInstance(){
        return tickets;
    }
    public static synchronized TicketService getInstance(){
        if (instance == null) {
            instance = new TicketService();
        }
        return instance;
    }



    public static Ticket storeNewTicket(Ticket newTicket) {
        //Ticket newTicket = new Ticket(ticketId.getAndIncrement(), reporter, topic, description, type, priority);
        //tickets.put(newTicket.getId(), newTicket);
        newTicket.setId(ticketId.getAndIncrement());
        tickets.put(newTicket.getId(), newTicket);

        //System.out.println("Created new Ticket from Reporter: " + reporter + " with the topic \"" + topic + "\"");
        System.out.println(newTicket.toString());

        return newTicket;

    }

    public static Ticket updateTicket(Ticket ticket, Ticket gotTicket) {

        if (ticket == null || gotTicket == null) {
            return null;
        }

        gotTicket.setStatus(ticket.getStatus());
        return gotTicket;

    }

    public static Ticket getTicketbyId(int id) {
        Ticket ticket = tickets.get(id);
        //System.out.println("Found ticket: " +ticket);
        return ticket;
    }


    public static HashMap<Integer, Ticket> getAllTickets() {
        //System.out.println("tickets  in  "+tickets);
        return tickets;
    }

    public static List<Map.Entry<Integer,Ticket>> getTicketsByName(String name) {

        List<Map.Entry<Integer,Ticket>> matchedEntry =
                tickets.entrySet().stream().filter(element -> element.getValue().getReporter().contains(name)
                        ||  element.getValue().getTopic().contains(name) || element.getValue().getDescription().contains(name) ).collect(Collectors.toList());

        System.out.println("Searched tickets (with topic/reporter/description: "+name+"): "+matchedEntry.toString());
        return matchedEntry;
    }

    public static List<Map.Entry<Integer,Ticket>> getTicketsByType(Type type) {

        List<Map.Entry<Integer,Ticket>> matchedEntry =
                tickets.entrySet().stream().filter(element -> element.getValue().getType().equals(type)).collect(Collectors.toList());

        System.out.println("Searched tickets (with type: "+type+"): "+matchedEntry.toString());
        return matchedEntry;
    }

}

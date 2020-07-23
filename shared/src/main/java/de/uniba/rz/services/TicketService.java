package de.uniba.rz.services;


import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;


import java.util.*;
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
        newTicket.setId(ticketId.getAndIncrement());
        tickets.put(newTicket.getId(), newTicket);

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
        return ticket;
    }


    public static HashMap<Integer, Ticket> getAllTickets() {
        return tickets;
    }

    public static List<Map.Entry<Integer,Ticket>> getTicketsByName(String name, int offset, int limit) {
        offset--;
        offset = offset*limit;
        List<Map.Entry<Integer,Ticket>> matchedEntry =
                tickets.entrySet().stream().filter(element -> element.getValue().getReporter().contains(name)
                        ||  element.getValue().getTopic().contains(name) || element.getValue().getDescription().contains(name) )
                        .sorted(Comparator.comparing(e -> e.getValue().getPriority())).collect(Collectors.toList());

        //page offset starts from 0, but user would enter page 1 in the field
        if(offset+limit > matchedEntry.size()){
            System.out.println("Searched tickets (with topic/reporter/description: "+name+"): "+matchedEntry.toString());
            return matchedEntry;
        }
        System.out.println("Searched tickets (with topic/reporter/description: "+name+"): "+matchedEntry.subList(offset,offset+limit).toString());
        return matchedEntry.subList(offset,offset+limit);
    }

    public static List<Map.Entry<Integer,Ticket>> getTicketsByType(Type type, int offset, int limit) {
        offset--;
        offset = offset*limit;
        List<Map.Entry<Integer,Ticket>> matchedEntry =
                tickets.entrySet().stream().filter(element -> element.getValue().getType().equals(type))
                        .sorted(Comparator.comparing(e -> e.getValue().getPriority())).collect(Collectors.toList());

        //page offset starts from 0
        if(offset+limit > matchedEntry.size()){
            System.out.println("Searched tickets (with type: "+type+"): "+matchedEntry.toString());
            return matchedEntry;
        }
        System.out.println("Searched tickets (with type: "+type+"): "+matchedEntry.subList(offset,offset+limit).toString());
        return matchedEntry.subList(offset,offset+limit);

    }



}

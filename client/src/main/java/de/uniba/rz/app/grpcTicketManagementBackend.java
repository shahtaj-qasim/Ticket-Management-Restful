package de.uniba.rz.app;

import java.util.*;
import java.util.concurrent.TimeUnit;

import de.uniba.rz.entities.*;
import de.uniba.rz.io.rpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class grpcTicketManagementBackend extends TicketServiceGrpc.TicketServiceImplBase implements TicketManagementBackend {

    private final TicketServiceGrpc.TicketServiceBlockingStub syncStub;
    private final TicketServiceGrpc.TicketServiceStub asyncStub;
    private final ManagedChannel channel;

    public grpcTicketManagementBackend(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public grpcTicketManagementBackend(ManagedChannelBuilder<?> channelBuilder) {
        this.channel = channelBuilder.build();
        this.syncStub = TicketServiceGrpc.newBlockingStub(this.channel);
        this.asyncStub = TicketServiceGrpc.newStub(this.channel);
    }

    @Override
    public void triggerShutdown() {
        if (!this.channel.isShutdown()) {
            try {
                this.channel.shutdown().awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Unable to shutdown client. Check log");
                // e.printStackTrace();
            }
        }
    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) {
        TicketRequest ticketRequest = TicketRequest.newBuilder().setDescription(description)
                .setPriority(priority.toString()).setType(type.toString()).setReporter(reporter).setTopic(topic)
                .build();
        TicketResponse ticketResponse = this.syncStub.createTicket(ticketRequest);
        Ticket createdTicket = storeBackTicket(ticketResponse);
        broadcastReceiver();
        return createdTicket;
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        TicketId ticketIdRequest = TicketId.newBuilder().setTicketId(id).build();
        TicketResponse response = this.syncStub.acceptTicket(ticketIdRequest);
        Ticket acceptTicket = storeBackTicket(response);
        broadcastReceiver();
        return acceptTicket;
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        TicketId ticketIdRequest = TicketId.newBuilder().setTicketId(id).build();
        TicketResponse response = this.syncStub.rejectTicket(ticketIdRequest);
        Ticket rejectTicket = storeBackTicket(response);
        broadcastReceiver();
        return rejectTicket;
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        TicketId ticketIdRequest = TicketId.newBuilder().setTicketId(id).build();
        TicketResponse response = this.syncStub.closeTicket(ticketIdRequest);
        Ticket closedTicket = storeBackTicket(response);
        broadcastReceiver();
        return closedTicket;
    }

    private Ticket storeBackTicket(TicketResponse response) {
        Ticket tempTicket = new Ticket();
        tempTicket.setId(response.getTicketId());
        tempTicket.setReporter(response.getReporter());
        tempTicket.setType(Type.valueOf(response.getType()));
        tempTicket.setPriority(Priority.valueOf(response.getPriority()));
        tempTicket.setStatus(Status.valueOf(response.getStatus()));
        tempTicket.setTopic(response.getTopic());
        tempTicket.setDescription(response.getDescription());
        return tempTicket;
    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        Empty emptyRequest = Empty.newBuilder().build();
        TicketList ticketResponse = this.syncStub.getAllTickets(emptyRequest);
        return (List<Ticket>) Util.byteStreamToObject(ticketResponse.getAllTickets().toByteArray());
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {
        TicketId ticketIdRequest = TicketId.newBuilder().setTicketId(id).build();
        TicketResponse response = this.syncStub.getTicketById(ticketIdRequest);
        Ticket createdTicket = storeBackTicket(response);
        return createdTicket;
    }

    public void broadcastReceiver() {
        new AutoUpdateFields(this.asyncStub).start();
    }
}

class AutoUpdateFields extends Thread {

    private final TicketServiceGrpc.TicketServiceStub asycStub;

    public AutoUpdateFields(TicketServiceGrpc.TicketServiceStub asycStub) {
        this.asycStub = asycStub;
    }

    @Override
    public void run() {
        StreamObserver<AutoNewTicketRequest> observer = this.asycStub.streamNewTicket(new StreamObserver<TicketList>() {

            @Override
            public void onNext(TicketList response) {
                List<Ticket> ticketList = (List<Ticket>) Util.byteStreamToObject(response.getAllTickets().toByteArray());
                for (Ticket t : ticketList) {
                    System.out.println("Ticket Created: " + t.getReporter());
                }
                Main.mf.updateTable(ticketList);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Unable to retrieve auto list from client.");
            }

            @Override
            public void onCompleted() {
                System.out.println("Ticket list updated.");
            }
        });
        observer.onNext(AutoNewTicketRequest.newBuilder().setTicketId(1).build());
    }
}
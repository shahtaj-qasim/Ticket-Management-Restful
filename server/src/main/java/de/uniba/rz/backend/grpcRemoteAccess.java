package de.uniba.rz.backend;

import com.google.protobuf.ByteString;
import de.uniba.rz.entities.*;
import de.uniba.rz.io.rpc.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.LinkedHashSet;

public class grpcRemoteAccess implements RemoteAccess {

    private static TicketStore allTickets;
    private final int port;
    private final Server server;

    public grpcRemoteAccess(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port).addService(new TicketManagementImpl()).build();
    }

    @Override
    public void prepareStartup(TicketStore ticketStore) {
        allTickets = ticketStore;
    }

    @Override
    public void shutdown() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    public void run() {
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Server started and listened on port " + this.port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                grpcRemoteAccess.this.shutdown();
                System.err.println("*** server shut down");
            }
        });
    }

    private static class TicketManagementImpl extends TicketServiceGrpc.TicketServiceImplBase {

        private static final LinkedHashSet<StreamObserver<TicketList>> observers = new LinkedHashSet<>();

        @Override
        public void createTicket(TicketRequest request, StreamObserver<TicketResponse> responseObserver) {
            Ticket createdTicket = allTickets.storeNewTicket(request.getReporter(),
                    request.getTopic(),
                    request.getDescription(),
                    Type.valueOf(request.getType()),
                    Priority.valueOf(request.getPriority()));
            TicketResponse ticketResponse = TicketResponse.newBuilder()
                    .setDescription(createdTicket.getDescription())
                    .setTicketId(createdTicket.getId())
                    .setTopic(createdTicket.getTopic())
                    .setType(createdTicket.getType().toString())
                    .setPriority(createdTicket.getPriority().toString())
                    .setReporter(createdTicket.getReporter())
                    .setStatus(createdTicket.getStatus().toString())
                    .build();
            responseObserver.onNext(ticketResponse);
            responseObserver.onCompleted();
        }

        @Override
        public void acceptTicket(TicketId request, StreamObserver<TicketResponse> responseObserver) {
            long ticketId = Long.valueOf(request.getTicketId());
            allTickets.getAllTickets().forEach(ticket -> {
                if (ticket.getId() == ticketId) {
                    ticket.setStatus(Status.ACCEPTED);
                    TicketResponse ticketResponse = TicketResponse.newBuilder()
                            .setDescription(ticket.getDescription())
                            .setTicketId(ticket.getId())
                            .setTopic(ticket.getTopic())
                            .setType(ticket.getType().toString())
                            .setPriority(ticket.getPriority().toString())
                            .setReporter(ticket.getReporter())
                            .setStatus(ticket.getStatus().toString())
                            .build();
                    responseObserver.onNext(ticketResponse);
                    responseObserver.onCompleted();
                    return;
                }
            });
        }

        @Override
        public void rejectTicket(TicketId request, StreamObserver<TicketResponse> responseObserver) {
            long ticketId = Long.valueOf(request.getTicketId());
            allTickets.getAllTickets().forEach(ticket -> {
                if (ticket.getId() == ticketId) {
                    ticket.setStatus(Status.REJECTED);
                    TicketResponse ticketResponse = TicketResponse.newBuilder()
                            .setDescription(ticket.getDescription())
                            .setTicketId(ticket.getId())
                            .setTopic(ticket.getTopic())
                            .setType(ticket.getType().toString())
                            .setPriority(ticket.getPriority().toString())
                            .setReporter(ticket.getReporter())
                            .setStatus(ticket.getStatus().toString())
                            .build();
                    responseObserver.onNext(ticketResponse);
                    responseObserver.onCompleted();
                    return;
                }
            });
        }

        @Override
        public void closeTicket(TicketId request, StreamObserver<TicketResponse> responseObserver) {
            long ticketId = Long.valueOf(request.getTicketId());
            allTickets.getAllTickets().forEach(ticket -> {
                if (ticket.getId() == ticketId) {
                    ticket.setStatus(Status.CLOSED);
                    TicketResponse ticketResponse = TicketResponse.newBuilder()
                            .setDescription(ticket.getDescription())
                            .setTicketId(ticket.getId())
                            .setPriority(ticket.getPriority().toString())
                            .setType(ticket.getType().toString())
                            .setReporter(ticket.getReporter())
                            .setTopic(ticket.getTopic())
                            .setStatus(ticket.getStatus().toString())
                            .build();
                    responseObserver.onNext(ticketResponse);
                    responseObserver.onCompleted();
                    return;
                }
            });
        }

        @Override
        public void getTicketById(TicketId request, StreamObserver<TicketResponse> responseObserver) {
            long ticketId = Long.valueOf(request.getTicketId());
            allTickets.getAllTickets().forEach(ticket -> {
                if (ticket.getId() == ticketId) {
                    TicketResponse ticketResponse = TicketResponse.newBuilder()
                            .setDescription(ticket.getDescription())
                            .setTicketId(ticket.getId())
                            .setTopic(ticket.getTopic())
                            .setType(ticket.getType().toString())
                            .setPriority(ticket.getPriority().toString())
                            .setReporter(ticket.getReporter())
                            .setStatus(ticket.getStatus().toString())
                            .build();
                    responseObserver.onNext(ticketResponse);
                    responseObserver.onCompleted();
                    return;
                }
            });
        }

        @Override
        public void getAllTickets(Empty request, StreamObserver<TicketList> responseObserver) {
            TicketList allTicketResponse = TicketList
                    .newBuilder()
                    .setAllTickets(ByteString.copyFrom(Util.objectToStream(allTickets.getAllTickets())))
                    .build();
            responseObserver.onNext(allTicketResponse);
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<AutoNewTicketRequest> streamNewTicket(StreamObserver<TicketList> responseObserver) {
            observers.add(responseObserver);
            return new StreamObserver<AutoNewTicketRequest>() {

                @Override
                public void onNext(AutoNewTicketRequest value) {
                    TicketList ticketLS = TicketList.newBuilder().setAllTickets(ByteString.copyFrom(Util.objectToStream(allTickets.getAllTickets())))
                            .build();
                    observers.forEach(o -> o.onNext(ticketLS));
                }

                @Override
                public void onError(Throwable t) {
                    //System.out.println("Error observing server.");
                    observers.remove(responseObserver);
                }

                @Override
                public void onCompleted() {
                    //System.out.println("Streaming completed");
                    observers.remove(responseObserver);
                }
            };
        }

    }
}

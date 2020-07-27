package de.uniba.rz.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import javax.naming.NamingException;

public class TicketServerMain {
	static TicketStore  simpleStore;
	public static void main(String[] args) throws IOException, NamingException {

		switch (args[0]) {
			case "rest":
				simpleStore = new RestTicketStore();
			case "grpc":
				simpleStore = new grpcTicketStore();
		}

		List<RemoteAccess> remoteAccessImplementations = getAvailableRemoteAccessImplementations(args);

		// Starting remote access implementations:
		for (RemoteAccess implementation : remoteAccessImplementations) {
			implementation.prepareStartup(simpleStore);
			new Thread(implementation).start();
		}

		try (BufferedReader shutdownReader = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.println("Press enter to shutdown system.");
			shutdownReader.readLine();
			System.out.println("Shutting down...");
	
			// Shutting down all remote access implementations
			for (RemoteAccess implementation : remoteAccessImplementations) {
				implementation.shutdown();
			}
			System.out.println("completed. Bye!");
		}
	}

	private static List<RemoteAccess> getAvailableRemoteAccessImplementations(String[] args) {
		List<RemoteAccess> implementations = new ArrayList<>();
		switch (args[0]) {
			case "rest":
				implementations.add(new RestRemoteAccess());
			case "grpc":
				implementations.add(new grpcRemoteAccess(Integer.parseInt(args[2])));
		}

		return implementations;
	}
}

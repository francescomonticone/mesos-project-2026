package it.polimi.ingsw.Server;

import it.polimi.ingsw.Server.Controller.ServerManager;
import it.polimi.ingsw.Server.Network.SocketClientHandler;
import it.polimi.ingsw.Server.Network.RMIServerEndpoint;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainServer {
    public static void main(String[] args) {
        //create the Server Manager
        ServerManager globalManager = new ServerManager();

        // Check if the property has already been set from the terminal using -D
        String serverIp = System.getProperty("java.rmi.server.hostname");

        // If it hasn't been set, check the arguments or use the default
        if (serverIp == null) {
            if (args.length > 0) {
                serverIp = args[0]; // Use the argument if present
            } else {
                serverIp = "127.0.0.1"; // Otherwise use localhost
            }
            // Set the property only if it wasn't already set
            System.setProperty("java.rmi.server.hostname", serverIp);
        }

        System.out.println("Configured RMI Hostname: " + System.getProperty("java.rmi.server.hostname"));

        //default values
        int socketPort = 1234;
        int rmiPort = 1099;

        if (args.length > 1) {
            try {
                socketPort = Integer.parseInt(args[1]); // Socket port
            } catch (NumberFormatException e) {
                System.err.println("Socket port format is not valid: '" + args[1] + "'. it will be used default: " + socketPort);
            }
        }

        if (args.length > 2) {
            try {
                rmiPort = Integer.parseInt(args[2]); // RMI port
            } catch (NumberFormatException e) {
                System.err.println("RMI port format not valid: '" + args[2] + "'. it will be used default: " + rmiPort);
            }
        }

        // RMI SERVER INITIALIZATION

        try {

            System.setProperty("sun.rmi.transport.tcp.responseTimeout", "3000");  // 3 sec
            System.setProperty("sun.rmi.transport.connectionTimeout", "3000");     // 3 sec
            System.setProperty("sun.rmi.dgc.client.gcInterval", "3000");
            System.setProperty("java.rmi.dgc.leaseValue", "3000");

            //create the RMI Registry on port 1099 (standard RMI port)
            Registry registry = LocateRegistry.createRegistry(rmiPort);

            //instantiate our singleton entry point for RMI clients
            RMIServerEndpoint rmiServer = new RMIServerEndpoint(globalManager);

            // Bind the remote object to the registry with a specific name
            registry.rebind("MesosServer", rmiServer);

            System.out.println("RMI Server listening on port " + rmiPort + " ...");

        } catch (Exception e) {
            System.err.println("Failed to start RMI Server: " + e.getMessage());
            e.printStackTrace();
        }


        // SOCKET SERVER INITIALIZATION

        try (ServerSocket serverSocket = new ServerSocket(socketPort)) { //open a server socket on the desired port
            System.out.println("Socket Server listening on port "+ socketPort + " ...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); //new client has connected, return a new socket for this client
                System.out.println("New client connected via Socket!");

                //create the SocketClientHandler to manage that client
                SocketClientHandler clientHandler = new SocketClientHandler(clientSocket, globalManager);

                //the thread for that client starts(run override)
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
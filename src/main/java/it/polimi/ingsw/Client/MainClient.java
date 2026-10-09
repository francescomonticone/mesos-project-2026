package it.polimi.ingsw.Client;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Client.Network.RMIVirtualServer;
import it.polimi.ingsw.Client.Network.SocketVirtualServer;
import it.polimi.ingsw.Client.Network.VirtualServer;
import it.polimi.ingsw.Client.View.TUI.TuiView;

import static it.polimi.ingsw.Client.View.TUI.TuiView.ANSI_RED;
import static it.polimi.ingsw.Client.View.TUI.TuiView.ANSI_RESET;

/**
 * The main entry point for the Mesos Client application.
 * <p>
 * This class coordinates the startup sequence: initializing the view,
 * selecting the network technology, setting up the MVC components,
 * and starting the main user input loop.
 * </p>
 */
public class MainClient {

    /**
     * Main method to start the client application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Initialize the Text User Interface
        TuiView tuiView = new TuiView();

        tuiView.clearScreen();
        System.out.println(ANSI_RED+" /$$      /$$ /$$$$$$$$  /$$$$$$   /$$$$$$   /$$$$$$");
        System.out.println("| $$$    /$$$| $$_____/ /$$__  $$ /$$__  $$ /$$__  $$");
        System.out.println("| $$$$  /$$$$| $$      | $$  \\__/| $$  \\ $$| $$  \\__/");
        System.out.println("| $$ $$/$$ $$| $$$$$   |  $$$$$$ | $$  | $$|  $$$$$$");
        System.out.println("| $$  $$$| $$| $$__/    \\____  $$| $$  | $$ \\____  $$");
        System.out.println("| $$\\  $ | $$| $$       /$$  \\ $$| $$  | $$ /$$  \\ $$");
        System.out.println("| $$ \\/  | $$| $$$$$$$$|  $$$$$$/|  $$$$$$/|  $$$$$$/");
        System.out.println("|__/     |__/|________/ \\______/  \\______/  \\______/"+ANSI_RESET);


        try {

            int techChoice = chooseNetworkTechnology(tuiView);

            //1.1 Connection Configuration
            String serverIp = tuiView.readInput("\nServer IP address (default: 127.0.0.1): ");
            if (serverIp.isEmpty()) {
                serverIp = "127.0.0.1";
            }

            //1.2 Port Configuration (Default changes based on technology)
            int defaultPort = (techChoice == 1) ? 1234 : 1099; //techChoice 1 is for Socket, 2 is for RMI
            int port = getPort(tuiView, defaultPort);

            // 2. Polymorphic Network Selection
            // The method returns a VirtualServer interface, keeping the Main logic agnostic
            // of the specific underlying technology (Socket or RMI).
            VirtualServer virtualServer;
            if (techChoice == 1) {
                tuiView.showMessage("Establishing Socket connection to " + serverIp + " : " + port);
                virtualServer = new SocketVirtualServer(serverIp, port);
            } else {
                tuiView.showMessage("Establishing RMI connection to " + serverIp + " : " + port);
                virtualServer = new RMIVirtualServer(serverIp, port);
            }

            // 3. MVC Client-side Setup
            ClientController controller = new ClientController(tuiView, virtualServer);

            // Critical Step: This polymorphic call activates the specific network listeners.
            // - For Socket: Starts the background reading thread.
            // - For RMI: Links the controller to the exported callback object.
            virtualServer.setControllerRMILoopSocket(controller);

            // 4. Authentication Flow
            performLogin(tuiView, controller);

            // 5. Main Execution Loop
            // Keeps the application alive and handles terminal commands.
            tuiView.startInputLoop(controller);

        } catch (Exception e) {
            tuiView.showError("A critical error occurred during startup: " + e.getMessage());
        }
    }

    /**
     * Handles the network technology selection process.
     *
     * @param view the TuiView used for input/output
     * @return 1 for Socket, 2 for RMI
     */
    private static int chooseNetworkTechnology(TuiView view) {
        while (true) {
            String input = view.readInput("\nChoose network technology (1. Socket, 2. RMI): ");
            if ("1".equals(input)) {
                return 1;
            } else if ("2".equals(input)) {
                return 2;
            }
            view.showError("Invalid choice. Please enter 1 or 2.");
        }
    }

    /**
     * Prompts the user for a port number with validation.
     * * @param view the TuiView used for input/output
     * @param defaultPort the default port to use if the user leaves the input blank
     * @return a valid port number
     */
    private static int getPort(TuiView view, int defaultPort) {
        while (true) {
            String portStr = view.readInput("Server Port (default: " + defaultPort + "): ");
            if (portStr.isEmpty()) {
                return defaultPort; //returns the default port if the user doesn't input anything
            }
            try {
                int port = Integer.parseInt(portStr);
                if (port > 0 && port <= 65535) {
                    return port; //returns the port if it is in the valid range
                } else {
                    view.showError("Port must be between 1 and 65535.");
                }
            } catch (NumberFormatException e) {
                view.showError("Invalid port number. Please enter digits only.");
            }
        }
    }

    /**
     * Manages the credential input and initiates the login request.
     *
     * @param view       the view to interact with the user
     * @param controller the controller to handle the logic
     */
    private static void performLogin(TuiView view, ClientController controller) {
        boolean success = false;

        while (!success) {
            // Clear any stale or delayed responses before attempting a new login
            view.resetLoginState();

            String nickname = view.readInput("\nNickname: ");
            String password = view.readInput("Password: ");
            
            controller.handleLogin(nickname, password);

            success = view.waitForLoginResponse(); //we wait for a server response, but we have a timer
        }
    }
}
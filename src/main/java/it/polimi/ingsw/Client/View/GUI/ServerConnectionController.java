package it.polimi.ingsw.Client.View.GUI;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Client.Network.RMIVirtualServer;
import it.polimi.ingsw.Client.Network.SocketVirtualServer;
import it.polimi.ingsw.Client.Network.VirtualServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * FXML controller for {@code server_connection.fxml}.
 * Second screen: user enters server IP and port.
 * Creates the correct {@link VirtualServer} (Socket or RMI),
 * then builds the {@link ClientController} and injects it into
 * {@link GuiView} before moving to the login screen.
 */
@SuppressWarnings("unused") // @FXML fields and handlers are used by JavaFX at runtime
public class ServerConnectionController {

    private static final String DEFAULT_HOST       = "127.0.0.1";
    private static final int    DEFAULT_PORT_SOCKET = 1234;
    private static final int    DEFAULT_PORT_RMI    = 1099;

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Button    connectButton;
    @FXML private Label     feedbackLabel;

    private GuiView guiView;

    public void init(GuiView guiView) {
        this.guiView = guiView;
        boolean rmi = NetworkSelectionController.isUseRmi();
        hostField.setPromptText(DEFAULT_HOST);
        portField.setPromptText(String.valueOf(rmi ? DEFAULT_PORT_RMI : DEFAULT_PORT_SOCKET));
        feedbackLabel.setTextFill(Color.GRAY);
        feedbackLabel.setText("Technology: " + (rmi ? "RMI" : "Socket"));
    }

    /**
     * Handles the click event on the "Connect" button.
     * Parses and validates the host and port entered by the user, falling back to
     * default values if the fields are left blank. It then attempts to establish a connection
     * to the server (via Socket or RMI) on a separate background thread to keep the UI responsive.
     * <p>
     * If the connection is successful, it initializes the {@link ClientController}, links it
     * to the view, and safely transitions to the login screen on the JavaFX Application Thread.
     * If the connection fails, it displays an error message to the user.
     * </p>
     */
    @FXML
    private void onConnectClicked() {
        String host = hostField.getText().trim();
        if (host.isBlank()) host = DEFAULT_HOST;

        boolean rmi = NetworkSelectionController.isUseRmi();
        int defaultPort = rmi ? DEFAULT_PORT_RMI : DEFAULT_PORT_SOCKET;

        int port;
        String rawPort = portField.getText().trim();
        if (rawPort.isBlank()) {
            port = defaultPort;
        } else {
            try {
                port = Integer.parseInt(rawPort);
                if (port < 1 || port > 65535) throw new NumberFormatException();
            } catch (NumberFormatException ignored) {
                feedbackLabel.setTextFill(Color.RED);
                feedbackLabel.setText("Invalid port (1-65535).");
                return;
            }
        }

        connectButton.setDisable(true);
        feedbackLabel.setTextFill(Color.GRAY);
        feedbackLabel.setText("Connecting to " + host + ":" + port + "...");

        final String finalHost = host;
        final int    finalPort = port;

        new Thread(() -> {
            try {
                VirtualServer virtualServer;
                if (rmi) {
                    String clientIp = System.getProperty("java.rmi.server.hostname");
                    if (clientIp == null || clientIp.isBlank()) {
                        try {
                            clientIp = java.net.InetAddress.getLocalHost().getHostAddress();
                        } catch (Exception ex) {
                            clientIp = "127.0.0.1";
                        }
                        System.setProperty("java.rmi.server.hostname", clientIp);
                    }
                    System.out.println("RMI client hostname: " + clientIp);
                    virtualServer = new RMIVirtualServer(finalHost, finalPort);
                } else {
                    virtualServer = new SocketVirtualServer(finalHost, finalPort);
                }
                ClientController clientController =
                        new ClientController(guiView, virtualServer);
                virtualServer.setControllerRMILoopSocket(clientController);
                guiView.setClientController(clientController);

                Platform.runLater(() -> guiView.showLoginScene());

            } catch (Exception e) {
                Platform.runLater(() -> {
                    connectButton.setDisable(false);
                    feedbackLabel.setTextFill(Color.RED);
                    feedbackLabel.setText("Connection failed: " + e.getMessage());
                });
            }
        }, "gui-connect-thread").start();
    }
}
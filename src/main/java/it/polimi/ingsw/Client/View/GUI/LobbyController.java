package it.polimi.ingsw.Client.View.GUI;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Network.DTO.LobbyDTO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * FXML controller for {@code lobby.fxml}.
 *
 * <p>Shows open lobbies pushed by the server, allows the player to
 * create a new lobby (size 2–5) or join an existing one by ID.
 * A manual refresh button mirrors the TUI "refresh" command.</p>
 */
@SuppressWarnings("unused") // @FXML fields and handlers are used by JavaFX at runtime

public class LobbyController {

    @FXML private VBox             lobbyListBox;
    @FXML private Label            feedbackLabel;
    @FXML private Label            playerLabel;
    @FXML private Spinner<Integer> sizeSpinner;
    @FXML private Button           createButton;
    @FXML private TextField        joinIdField;
    @FXML private Button           joinButton;
    @FXML private Button           refreshButton;

    private ClientController clientController;
    private GuiView          guiView;
    private String myNickname;

    @FXML
    private Button muteButton;

    /**
     * Initializes the lobby controller, configures the initial UI state,
     * and requests the list of open lobbies from the server.
     *
     * @param clientController the main controller handling game logic and network
     * @param guiView the main view coordinator
     * @param nickname the local player's confirmed nickname
     */
    public void init(ClientController clientController, GuiView guiView, String nickname) {
        this.clientController = clientController;
        this.guiView          = guiView;
        this.myNickname = nickname;

        //initialize the mute button style
        if (guiView.getSoundManager().isMuted()) {
            muteButton.setGraphic(EmojiIcon.of("1f507", 20)); //🔇
        } else {
            muteButton.setGraphic(EmojiIcon.of("1f50a", 20)); // 🔊
        }

        playerLabel.setText("Logged in as: " + myNickname);

        SpinnerValueFactory<Integer> svf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 5, 2);
        sizeSpinner.setValueFactory(svf);

        clientController.handleRequestOpenLobbies();
    }

    // ── FXML handlers ─────────────────────────────────────────
    /**
     * Handles the request to create a new lobby using the selected size.
     */
    @FXML
    private void onCreateLobbyClicked() {
        int size = sizeSpinner.getValue();
        setFormLocked(true);
        showInfo("Creating lobby for " + size + " players...");
        clientController.handleCreateLobby(size);
    }

    /**
     * Handles the request to join a lobby by its ID.
     */
    @FXML
    private void onJoinLobbyClicked() {
        String raw = joinIdField.getText().trim();
        if (raw.isEmpty()) { showError("Enter a lobby ID."); return; }
        int lobbyId;
        try {
            lobbyId = Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            showError("Lobby ID must be a number.");
            return;
        }
        joinLobby(lobbyId);
    }

    /**
     * Requests the latest list of open lobbies from the server.
     */
    @FXML
    private void onRefreshClicked() {
        showInfo("Refreshing lobbies...");
        clientController.handleRequestOpenLobbies();
    }

    // ── Callbacks from GuiView ────────────────────────────────

    /**
     * Rebuilds the lobby list with the latest server data.
     * Called by {@link GuiView#showOpenLobbies(List)} on the JavaFX thread.
     */
    public void refreshLobbies(List<LobbyDTO> lobbies) {
        setFormLocked(false);
        lobbyListBox.getChildren().clear();

        if (lobbies == null || lobbies.isEmpty()) {
            Label empty = new Label("No open lobbies — be the first to create one!");
            empty.setStyle("-fx-text-fill: #7a7974; -fx-font-size: 12; -fx-padding: 16;");
            lobbyListBox.getChildren().add(empty);
            return;
        }

        for (LobbyDTO lobby : lobbies) {
            lobbyListBox.getChildren().add(buildLobbyRow(lobby));
        }
    }

    /**
     * Displays an error message and unlocks the lobby form.
     *
     * @param message the error message to display
     */
    public void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #a12c7b;");
        setFormLocked(false);
    }

    /**
     * Displays an informational message in the feedback label.
     *
     * @param message the info text to display
     */
    public void showInfo(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #501d0c;");
    }

    // ── Private helpers ───────────────────────────────────────

    /**
     * Builds a UI row representing a single lobby entry.
     *
     * @param lobby the lobby data to render
     * @return the configured lobby row
     */
    private HBox buildLobbyRow(LobbyDTO lobby) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #f9f8f5; "
                + "-fx-background-radius: 6; "
                + "-fx-border-color: #dcd9d5; -fx-border-radius: 6;");

        Label idLabel = new Label("#" + lobby.lobbyId());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; "
                + "-fx-text-fill: #501d0c; -fx-min-width: 36;");

        Label creatorLabel = new Label("Creator: " + lobby.creatorNickname());
        creatorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #28251d;");
        HBox.setHgrow(creatorLabel, Priority.ALWAYS);

        String nickList = lobby.connectedNicknames() != null
                ? String.join(", ", lobby.connectedNicknames()) : "—";
        Label sizeLabel = new Label(lobby.currentPlayers() + " / " + lobby.maxPlayers());
        sizeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7a7974;");
        Tooltip.install(sizeLabel, new Tooltip("In lobby: " + nickList));

        boolean isInside = lobby.connectedNicknames() != null && lobby.connectedNicknames().contains(myNickname);
        boolean isFull = lobby.currentPlayers() == lobby.maxPlayers();

        Button joinBtn = new Button();
        joinBtn.setStyle("-fx-background-color: #501d0c; -fx-text-fill: white; "
                + "-fx-font-size: 12; -fx-background-radius: 4; -fx-cursor: hand;");
        if (isInside) {
            joinBtn.setText("Joined");
            joinBtn.setDisable(true); // if you are in the lobby disable the button
        } else if (isFull) {
            joinBtn.setText("Full");
            joinBtn.setDisable(true); // if is lobby il full disable the button
        } else {
            joinBtn.setText("Join");
            joinBtn.setOnAction(e -> joinLobby(lobby.lobbyId())); // otherwise you can join
        }

        row.getChildren().addAll(idLabel, creatorLabel, sizeLabel, joinBtn);
        return row;
    }

    /**
     * Sends a request to join the specified lobby.
     *
     * @param lobbyId the identifier of the lobby to join
     */
    private void joinLobby(int lobbyId) {
        setFormLocked(true);
        showInfo("Joining lobby #" + lobbyId + "...");
        clientController.handleJoinLobby(lobbyId);
    }

    /**
     * Locks or unlocks the interactive elements of the form.
     * Prevents the user from spamming requests while waiting for the server.
     *
     * @param locked true to disable inputs, false to enable them
     */
    private void setFormLocked(boolean locked) {
        createButton.setDisable(locked);
        joinButton.setDisable(locked);
        sizeSpinner.setDisable(locked);
        joinIdField.setDisable(locked);

        // Disable the entire list so the dynamic "Join" buttons cannot be clicked
        lobbyListBox.setDisable(locked);
    }

    /**
     * Triggered when the user clicks the mute button.
     * Toggles the global mute state in the {@code SoundManager} and updates the button's icon.
     */
    @FXML
    private void onMuteClicked() {
        // Ask the GuiView's SoundManager to toggle the mute state
        boolean isNowMuted = guiView.getSoundManager().toggleMute();

        // Update the button icon accordingly
        if (isNowMuted) { //true = is muted so update the icon
            muteButton.setGraphic(EmojiIcon.of("1f507", 20)); //🔇
        } else {
            muteButton.setGraphic(EmojiIcon.of("1f50a", 20)); // 🔊
        }
    }
}
package it.polimi.ingsw.Client.View.GUI;

import it.polimi.ingsw.Client.ClientController.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

/**
 * FXML controller for {@code login.fxml}.
 * Collects nickname and password, delegates authentication to
 * {@link ClientController#handleLogin}. Password hashing happens
 * inside ClientController, not here.
 */
@SuppressWarnings("unused") // @FXML fields are injected by JavaFX at runtime
public class LoginController {

    @FXML private TextField     nicknameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         feedbackLabel;

    private ClientController clientController;
    private GuiView guiView;

    @FXML
    private Button muteButton;

    /**
     * Called by {@link GuiView} after the FXML is loaded.
     */
    public void init(ClientController clientController, GuiView guiView) {
        this.clientController = clientController;
        feedbackLabel.setText("");
        this.guiView = guiView;

        if (guiView.getSoundManager().isMuted()) {
            muteButton.setGraphic(EmojiIcon.of("1f507", 20)); //🔇
        } else {
            muteButton.setGraphic(EmojiIcon.of("1f50a", 20)); // 🔊
        }
    }

    // ── FXML handler ─────────────────────────────────────────
    /**
     * Validates the login form and sends the login request to the server.
     */
    @FXML
    private void onLoginClicked() {
        String nickname = nicknameField.getText().trim();
        String password = passwordField.getText();

        if (nickname.isBlank() || password.isBlank()) {
            showError("Nickname and password cannot be empty.");
            return;
        }

        loginButton.setDisable(true);
        showInfo("Logging in...");
        clientController.handleLogin(nickname, password);
    }

    // ── callbacks from GuiView ────────────────────────────────

    /**
     * Returns the nickname entered by the user, trimmed of surrounding spaces.
     *
     * @return the typed nickname
     */
    public String getNickname() {
        return nicknameField.getText().trim();
    }

    /**
     * Displays an error message and re-enables the login button.
     *
     * @param message the error message to display
     */
    public void showError(String message) {
        loginButton.setDisable(false);
        feedbackLabel.setTextFill(Color.RED);
        feedbackLabel.setText(message);
    }

    /**
     * Displays an informational message in the feedback label.
     *
     * @param message the info text to display
     */
    public void showInfo(String message) {
        feedbackLabel.setTextFill(Color.GRAY);
        feedbackLabel.setText(message);
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
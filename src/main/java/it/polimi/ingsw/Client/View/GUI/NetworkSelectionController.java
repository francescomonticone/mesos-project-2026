package it.polimi.ingsw.Client.View.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * FXML controller for {@code network_selection.fxml}.
 * First screen: user picks Socket or RMI.
 * The choice is stored as a static flag read by {@link ServerConnectionController}.
 */
@SuppressWarnings("unused") // @FXML fields and handlers are used by JavaFX at runtime
public class NetworkSelectionController {

    @FXML private Button socketButton;
    @FXML private Button rmiButton;
    @FXML private Label  titleLabel;

    // Mute button for music
    @FXML private Button muteButton;

    private GuiView guiView;

    private static boolean useRmi = false;

    /**
     * Initializes the controller and configures the mute button icon.
     *
     * @param guiView the main view coordinator
     */
    public void init(GuiView guiView) {
        this.guiView = guiView;
        //initialize the mute button style
        if (guiView.getSoundManager().isMuted()) {
            muteButton.setGraphic(EmojiIcon.of("1f507", 20)); //🔇
        } else {
            muteButton.setGraphic(EmojiIcon.of("1f50a", 20)); // 🔊
        }
    }

    /**
     * Returns whether the client is currently configured to use RMI.
     *
     * @return true if RMI is selected, false if socket is selected
     */
    public static boolean isUseRmi() {
        return useRmi;
    }

    /**
     * Selects the socket transport and returns to the server connection screen.
     */
    @FXML
    private void onSocketClicked() {
        useRmi = false;
        guiView.showServerConnectionScene();
    }

    /**
     * Selects the RMI transport and returns to the server connection screen.
     */
    @FXML
    private void onRmiClicked() {
        useRmi = true;
        guiView.showServerConnectionScene();
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
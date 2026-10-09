package it.polimi.ingsw.Client.View.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for the initial screen (Start Screen) of the graphical user interface.
 * Manages user interactions with the view, including starting the game,
 * toggling the audio, and accessing the credits screen.
 */
public class StartScreenController {

    @FXML
    private Button muteButton;

    private GuiView guiView;

    /**
     * Initializes the controller by linking it to the main GUI instance.
     * It also configures the initial state of the interface, such as setting the correct
     * icon for the audio button based on the current state of the {@code SoundManager}.
     *
     * @param guiView the main instance of {@link GuiView} that manages navigation and the global state of the interface
     */
    public void init(GuiView guiView) {
        this.guiView = guiView;

        if (guiView.getSoundManager().isMuted()) {
            muteButton.setGraphic(EmojiIcon.of("1f507", 20)); //🔇
        } else {
            muteButton.setGraphic(EmojiIcon.of("1f50a", 20)); // 🔊
        }
    }

    /**
     * Handles the click event on the "Play" button.
     * Delegates to the {@link GuiView} to switch to the next screen (network selection).
     */
    @FXML
    private void onPlayClicked() {
        guiView.showNetworkSelectionScene(); //next scene
    }

    /**
     * Handles the click event on the audio/mute button.
     * Toggles the mute/unmute state via the {@code SoundManager} and dynamically updates
     * the icon (emoji) shown on the button to reflect the new state.
     */
    @FXML
    private void onMuteClicked() {
        // toggle mute and update button text accordingly
        boolean isNowMuted = guiView.getSoundManager().toggleMute();

        // Update the button icon accordingly
        if (isNowMuted) { //true = is muted so update the icon
            muteButton.setGraphic(EmojiIcon.of("1f507", 20)); //🔇
        } else {
            muteButton.setGraphic(EmojiIcon.of("1f50a", 20)); // 🔊
        }
    }

    /**
     * Handles the click event on the "Credits" button.
     * Delegates to the {@link GuiView} to transition to the credits screen.
     * Prints an error to the standard error stream if {@code guiView} has not been initialized.
     */
    @FXML
    private void onCreditsClicked() {
        if (guiView != null) {
            guiView.showCreditsScene();
        } else {
            System.err.println("[GUI ERROR] guiView is null in StartScreenController!");
        }
    }
}
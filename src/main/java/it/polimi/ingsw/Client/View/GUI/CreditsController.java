package it.polimi.ingsw.Client.View.GUI;

import javafx.fxml.FXML;

/**
 * JavaFX Controller for the "Credits" screen.
 * <p>
 * This class handles the UI interactions on the credits page, primarily
 * allowing the user to navigate back to the main start screen.
 * </p>
 */
public class CreditsController {

    private GuiView guiView;

    /**
     * Initializes the controller by injecting the main GUI view coordinator.
     *
     * @param guiView the central {@link GuiView} instance responsible for scene navigation
     */
    public void init(GuiView guiView) {
        this.guiView = guiView;
    }

    /**
     * Triggered when the user clicks the "Back" button on the UI.
     * Instructs the main view to switch back to the Start Screen scene.
     */
    @FXML
    private void onBackClicked() {
        guiView.showStartScreenScene();
    }
}
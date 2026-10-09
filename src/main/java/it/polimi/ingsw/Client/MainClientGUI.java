package it.polimi.ingsw.Client;

import it.polimi.ingsw.Client.View.GUI.GuiView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the MESOS client's Graphical User Interface (GUI).
 * <p>
 * This class initializes the JavaFX runtime environment and delegates the
 * window management and scene rendering to the {@link GuiView}. It mirrors
 * the role of {@link MainClient} used for the Text User Interface (TUI).
 * </p>
 */

public class MainClientGUI extends Application {

    /**
     * The main entry point for all JavaFX applications.
     * <p>
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * It instantiates the main GUI controller and loads the initial start screen.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set
     */
    @Override
    public void start(Stage primaryStage) {
        GuiView guiView = new GuiView(primaryStage);
        guiView.showStartScreenScene();
    }

    /**
     * Standard main method to launch the JavaFX application.
     * <p>
     * This method is called by a separate non-JavaFX Launcher class
     * to safely bootstrap the JavaFX toolkit.
     * </p>
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
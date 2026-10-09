package it.polimi.ingsw.Client.View.GUI;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Client.View.View;
import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Network.DTO.PlayerDTO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.List;

/**
 * JavaFX implementation of {@link View}.
 * Flow:
 *   showNetworkSelectionScene()
 *       showServerConnectionScene()
 *           showLoginScene()
 *               showLobbyScene()
 *                   showGameScene()
 */

public class GuiView implements View {

    private final Stage stage;
    private ClientController clientController;
    private String myNickname = "";

    private LoginController loginController;
    private LobbyController lobbyController;
    private GameController  gameController;

    //action buffer
    private boolean isGameLoading = false;
    private Runnable pendingAction = null;
    private MatchDTO pendingSnapshot = null;

    //music manager
    private final SoundManager soundManager;

    private static final String FXML_ROOT = "/fxml/";

    /**
     * Constructs the GuiView, initializing the main JavaFX stage, sound manager,
     * window properties, and loading custom fonts.
     *
     * @param stage the primary JavaFX stage provided by the application launch
     */
    public GuiView(Stage stage) {
        this.stage = stage;
        this.soundManager = new SoundManager(); //initialize a new sound manager
        stage.setTitle("MESOS");
        stage.getIcons().add(new Image("img/icon.png"));
        stage.setResizable(true); //the stage can be resized by the user

        //in order to close the game by clicking the X button
        stage.setOnCloseRequest(event -> {
            System.out.println("[GUI] Closing application.");
            System.exit(0);
        });

        //loading custom font
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/font/CormorantGaramond-VariableFont_wght.ttf"), 14 //default font size
        );

    }

    // ── wiring ────────────────────────────────────────────────

    /**
     * Links the main client controller to this view, allowing the GUI to route
     * user input and network commands to the logic layer.
     *
     * @param clientController the main {@link ClientController}
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    /**
     * Stores the confirmed nickname of the local player.
     * Used later to dynamically highlight the player's own UI elements.
     *
     * @param nickname the local player's nickname
     */
    @Override
    public void setNickname(String nickname) {
        this.myNickname = nickname;
    }

    // ── scene transitions ─────────────────────────────────────

    /**
     * Transitions the UI to the Start Screen and initiates the pre-match background music.
     */
    public void showStartScreenScene() {
        //play the first music for the pre-match phase
        soundManager.playPreMatchMusic();

        loadScene("start_screen.fxml", 600, 400, loader -> {
            StartScreenController c = loader.getController();
            c.init(this);
        });
    }

    /**
     * Transitions the UI to the Credits screen.
     */
    public void showCreditsScene() {
        loadScene("credits.fxml", 600, 400, loader -> {
            CreditsController c = loader.getController();
            c.init(this);
        });
    }

    /**
     * Transitions the UI to the Network Selection screen (e.g., choosing between RMI and Socket).
     */
    public void showNetworkSelectionScene() {
        loadScene("network_selection.fxml", 420, 280, loader -> {
            NetworkSelectionController c = loader.getController();
            c.init(this);
        });
    }

    /**
     * Transitions the UI to the Server Connection screen to input the server IP and Port.
     */
    public void showServerConnectionScene() {
        loadScene("server_connection.fxml", 420, 280, loader -> {
            ServerConnectionController c = loader.getController();
            c.init(this);
        });
    }

    /**
     * Transitions the UI to the Login screen.
     * Clears any previous game or lobby controllers to prevent ghost data on reconnection.
     */
    public void showLoginScene() {
        // Clear old controllers to avoid that past values interfere with new connections!
        gameController = null;
        lobbyController = null;
        loadScene("login.fxml", 580, 330, loader -> {
            loginController = loader.getController();
            loginController.init(clientController, this);
        });
    }

    /**
     * Transitions the UI to the Lobby waiting screen.
     * Aborts the transition if the game is already actively loading (e.g., during a mid-game reconnection).
     */
    public void showLobbyScene() {

        if (isGameLoading) { //if the matchDTO snapshot is arrived and the game is loading, we don't want to display lobbyScene(reconnections)
            return;
        }

        // Clear old controllers so messages are routed correctly to the Lobby!
        gameController = null;
        loginController = null;

        loadScene("lobby.fxml", 800, 560, loader -> {
            lobbyController = loader.getController();
            lobbyController.init(clientController, this, myNickname);
        });
    }

    // ── View interface ────────────────────────────────────────

    /**
     * Handles the UI response after a login attempt.
     * <p>
     * On success, routes the user to the Lobby (or directly to the active game if reconnecting).
     * On failure, displays an error message directly on the Login screen.
     * All UI updates are queued on the JavaFX Application Thread.
     * </p>
     *
     * @param success true if authentication was successful, false otherwise
     */
    @Override
    public void showLoginResult(boolean success) {
        Platform.runLater(() -> {
            if (success) {
                if (loginController != null) {
                    myNickname = loginController.getNickname();
                }
                if (gameController == null && !isGameLoading) {
                    showLobbyScene();
                } else {
                    System.out.println("[GUI] Reconnection: Go directly to the game without loading the lobby.");
                }
            } else {
                if (loginController != null) {
                    loginController.showError("Login failed. Nickname already taken or invalid.");
                }
            }
        });
    }

    /**
     * Updates the Lobby screen with the latest list of open matches.
     *
     * @param lobbies the list of active {@link LobbyDTO}s available to join
     */
    @Override
    public void showOpenLobbies(List<LobbyDTO> lobbies) {
        Platform.runLater(() -> {
            if (lobbyController != null) {
                lobbyController.refreshLobbies(lobbies);
            }
        });
    }

    /**
     * Updates the local view with the latest game state received from the server.
     * <p>
     * If the game scene has not been loaded yet (i.e., {@code gameController} is null),
     * this method handles the transition from the lobby to the active game, starts the
     * in-game background music, and initializes the {@link GameController}.
     * If the game scene is already active, it simply passes the new snapshot to the
     * existing controller to refresh the UI.
     * </p>
     * <p>
     * All UI modifications and scene transitions are safely queued on the JavaFX
     * Application Thread using {@code Platform.runLater}.
     * </p>
     *
     * @param snapshot the {@link MatchDTO} representing the most recent state of the game
     */
    @Override
    public void updateModel(MatchDTO snapshot) {
        Platform.runLater(() -> {
            if (gameController != null) {
                //if the scene is already active update game
                gameController.updateGame(snapshot);

            }
            else{
                //save the snapshot
                pendingSnapshot = snapshot;

                if (!isGameLoading) {
                    isGameLoading = true; //we are loading the game
                    lobbyController = null;
                    loginController = null;

                    soundManager.playGameMusic();

                    loadScene("game.fxml", 0, 0, loader -> {
                        gameController = loader.getController();
                        gameController.init(clientController, this, myNickname);

                        // update Game with the pending snapshot previously saved
                        if (pendingSnapshot != null) {
                            gameController.updateGame(pendingSnapshot);
                            pendingSnapshot = null;
                        }

                        isGameLoading = false; // game loading ended

                        // execute the pending action previously saved
                        if (pendingAction != null) {
                            pendingAction.run();
                            pendingAction = null;
                        }
                    });
                }
            }
        });
    }

    /**
     * Displays specific action prompts to the user depending on the current phase.
     * Delegates to the active game controller to render GUI-specific instructions.
     *
     * @param messageTUI text instruction for TUI (ignored by GUI)
     * @param messageGUI text instruction for GUI
     */
    @Override
    public void askAction(String messageTUI, String messageGUI) {
        Platform.runLater(() -> {
            if (gameController != null) {
                gameController.showInfo(messageGUI); //show the proper message for GUI(without TUI commands)
            }
        });
    }

    /**
     * Routes a generic informational message to the currently active screen
     * (Game, Lobby, or Login) for the user to see.
     *
     * @param message the info text to display
     */
    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> {
            if (gameController != null) {
                gameController.showInfo(message);
            } else if (lobbyController != null) {
                lobbyController.showInfo(message);
            } else if (loginController != null) {
                loginController.showInfo(message);
            }
            System.out.println("[INFO] " + message);
        });
    }

    /**
     * Routes an error message to the currently active screen
     * (Game, Lobby, or Login) for the user to see.
     *
     * @param message the error text to display
     */
    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            if (gameController != null) {
                gameController.showError(message);
            } else if (lobbyController != null) {
                lobbyController.showError(message);
            } else if (loginController != null) {
                loginController.showError(message);
            }
            System.err.println("[ERROR] " + message);
        });
    }

    /**
     * Intentionally empty in the GUI implementation.
     * Tribe details are handled via a dedicated drawer/popup within the {@code GameController}
     * rather than triggering a full separate View transition.
     *
     * @param players the list of players to display
     */
    @Override
    public void showAllTribes(List<PlayerDTO> players){
    }

    /**
     * Triggered by the server when the match officially ends (either naturally or by abandonment).
     * Replaces the current game board scene with the final leaderboard screen.
     *
     * @param matchRanking      the final standings of the current match
     * @param globalLeaderboard the top historical scores from the database
     * @param numPlayers        the total number of players in this match
     */
    @Override
    public void showEndGame(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) {
        /*
         * We use Platform.runLater because network commands arrive from a background thread,
         * but UI changes MUST happen on the JavaFX Application Thread.
         */
        Platform.runLater(() -> {
            System.out.println("[GUI] Match finished! Switching to leaderboard scene...");

            //clear variables for the next game
            gameController = null;
            isGameLoading = false;
            pendingSnapshot = null;
            pendingAction = null;

            // By passing 0, 0 we trigger your clever full-screen logic inside loadScene
            loadScene("leaderboard.fxml", 0, 0, loader -> {

                // Get the controller of the new scene
                LeaderboardController controller = loader.getController();

                // Pass the data to the controller so it can render the cards
                controller.init(matchRanking, globalLeaderboard, myNickname, this);
            });
        });
    }

    /**
     * Activates the Place Totem interactive panel in the Game UI.
     * Buffers the action if the game scene is currently in the middle of loading.
     */
    @Override
    public void showPlaceTotemPanel() {
        Platform.runLater(() -> {
            if (gameController != null) {
                gameController.activatePlaceTotem();
            } else if (isGameLoading) {
                //game is loading, the action is buffered. It will be run after the game is loaded
                pendingAction = this::showPlaceTotemPanel;
            } else {
                System.err.println("[GUI WARNING] received PlaceTotemCommand but gameController is null!");
            }
        });
    }

    /**
     * Activates the Resolve Offer interactive panel in the Game UI.
     * Buffers the action if the game scene is currently in the middle of loading.
     */
    @Override
    public void showResolveOfferPanel() {
        Platform.runLater(() -> {
            if (gameController != null) {
                gameController.activateResolveOffer();
            } else if (isGameLoading) {
                pendingAction = this::showResolveOfferPanel;
            }
        });
    }

    /**
     * Activates the Skip Extra Pick interactive panel in the Game UI.
     * Buffers the action if the game scene is currently in the middle of loading.
     */
    @Override
    public void showSkipExtraPanel() {
        Platform.runLater(() -> {
            if (gameController != null) {
                gameController.activateSkipExtra();
            } else if (isGameLoading) {
                pendingAction = this::showSkipExtraPanel;
            }
        });
    }

    /**
     * Exposes the SoundManager instance so that active controllers
     * can independently toggle mute settings or change tracks dynamically.
     *
     * @return the active {@link SoundManager}
     */
    //this is used for controller in such a way that they can select mute option
    public SoundManager getSoundManager() {
        return soundManager;
    }


    // ── private helper ────────────────────────────────────────

    @FunctionalInterface
    private interface LoadCallback {
        void onLoaded(FXMLLoader loader);
    }

    /**
     * Loads an FXML scene smoothly.
     * Uses Root Swapping for full-screen transitions to avoid flickering.
     * Uses a fresh Scene for windowed mode to prevent layout clipping bugs.
     *
     * @param fxmlFile the name of the FXML file to load
     * @param width    the preferred width (0 for full-screen)
     * @param height   the preferred height (0 for full-screen)
     * @param callback a callback to initialize the controller before showing
     */
    private void loadScene(String fxmlFile, double width, double height, LoadCallback callback) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_ROOT + fxmlFile));
                Parent root = loader.load();

                boolean wantFullScreen = (width == 0 && height == 0);

                // Initialize the controller via the callback before rendering
                if (callback != null) {
                    callback.onLoaded(loader);
                }

                if (wantFullScreen) {
                    /*
                     * FULL-SCREEN MODE (Game -> Leaderboard)
                     * We use Root Swapping if a scene already exists.
                     * This keeps the OS from flickering the screen.
                     */
                    if (stage.getScene() == null) {
                        stage.setScene(new Scene(root));
                    } else {
                        stage.getScene().setRoot(root);
                    }

                    if (!stage.isFullScreen()) {
                        stage.setFullScreen(true);
                        stage.setFullScreenExitHint("Press ESC to exit full screen");
                    }
                } else {
                    /*
                     * WINDOWED MODE (Leaderboard -> Lobby)
                     * We exit full-screen first.
                     * This forces JavaFX to recalculate window decorations perfectly,
                     * preventing the layout from being clipped or bugged.
                     */
                    if (stage.isFullScreen()) {
                        stage.setFullScreen(false);
                    }

                    // Apply fresh exact dimensions
                    stage.setScene(new Scene(root, width, height));
                    stage.centerOnScreen();
                }

                // Ensure the window is actually visible
                if (!stage.isShowing()) {
                    stage.show();
                }

            } catch (Exception e) {
                System.err.println("[GUI] Failed to load scene: " + fxmlFile + " — " + e.getMessage());
            }
        });
    }
}

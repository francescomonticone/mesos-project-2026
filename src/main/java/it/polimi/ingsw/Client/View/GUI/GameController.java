package it.polimi.ingsw.Client.View.GUI;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Network.DTO.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * FXML controller for {@code game.fxml}.
 *
 * <p>Renders the full game state from a {@link MatchDTO} snapshot and
 * exposes the correct input panel depending on {@code currentPhaseName}.</p>
 */
@SuppressWarnings("unused")
public class GameController {

    // ── Header ────────────────────────────────────────────────────────────────
    @FXML private Label phaseLabel;
    @FXML private Label roundLabel;
    @FXML private Label eraLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label feedbackLabel;

    // ── Board ─────────────────────────────────────────────────────────────────
    @FXML private HBox upperRowBox;
    @FXML private HBox lowerRowBox;
    @FXML private HBox offerTrackBox;
    @FXML private VBox turnOrderBox;

    // ── Action panels ─────────────────────────────────────────────────────────
    @FXML private VBox placeTotemPanel;
    @FXML private VBox pickCardPanel;
    @FXML private HBox resolveOfferPanel;
    @FXML private HBox skipExtraPanel;
    @FXML private VBox waitingPanel;


    // ── Timer controls ────────────────────────────────────────────────────────
    @FXML private HBox disconnectionPanel;
    @FXML private ProgressBar timerProgressBar;

    private Timeline countdownTimeline;
    private boolean isTimerRunning = false;

    // ── Resolve Offer controls ───────────────────────────────────────────────
    @FXML private Button confirmResolveButton;
    @FXML private Button skipButton;
    private final PickSession pickSession = new PickSession();

    // Mute button for music
    @FXML
    private Button muteButton;

    /**
     * Helper class to manage the state of the cards selected by the local player
     * during the "Resolve Offer" phase before confirming the action.
     */
    private static class PickSession {
        private final List<String> orderedSelections = new ArrayList<>();

        /**
         * Toggles the selection state of the given card.
         *
         * @param cardId the card identifier
         */
        public void toggle(String cardId) {
            if (orderedSelections.contains(cardId)) {
                orderedSelections.remove(cardId);
            } else {
                orderedSelections.add(cardId);
            }
        }
        /**
         * Checks whether the given card is currently selected.
         *
         * @param cardId the card identifier
         * @return true if the card is selected, false otherwise
         */
        public boolean contains(String cardId) {
            return orderedSelections.contains(cardId);
        }

        /**
         * Clears the current selection.
         */
        public void clear() {
            orderedSelections.clear();
        }

        /**
         * Returns the current ordered selection as an immutable list.
         *
         * @return the selected card identifiers
         */
        public List<String> selections() {
            return List.copyOf(orderedSelections);
        }

        /**
         * Returns the position of the given card in the current selection.
         *
         * @param cardId the card identifier
         * @return the zero-based selection index, or {@code -1} if the card is not selected
         */
        public int indexOf(String cardId) {
            return orderedSelections.indexOf(cardId);
        }

        /**
         * Checks whether no cards are selected.
         *
         * @return true if the selection is empty, false otherwise
         */
        public boolean isEmpty() {
            return orderedSelections.isEmpty();
        }

        /**
         * Returns the number of selected cards.
         *
         * @return the number of selected cards
         */
        public int size() {
            return orderedSelections.size();
        }
    }


    // ── Players sidebar ───────────────────────────────────────────────────────
    @FXML private VBox playersBox;

    private ClientController clientController;
    private GuiView guiView;
    private String myNickname;
    private MatchDTO currentSnapshot;
    private String lastPhaseName;

    // ── Drawer (player stats) ─────────────────────────────────────────────────
    @FXML private HBox drawerContainer;
    @FXML private Button drawerToggleButton;
    @FXML private Label drawerShamanStars;
    @FXML private Label drawerFoodDiscount;
    @FXML private Label drawerBuildDiscount;
    @FXML private Label drawerDistinctInv;
    @FXML private Label drawerInvPairs;

    private boolean isDrawerOpen = false;

    @FXML private ComboBox<String> playerSelector;
    private String selectedDrawerPlayer = null;

    // ── PlayerCard popup tracker ──────────────────────────────────────────────
    private Stage openPlayerCardsStage = null;
    private PlayerCardsController openPlayerCardsController = null;
    private String viewedPlayerCardNickname;

    // ── EventResult popup tracker ─────────────────────────────────────────────
    private Stage openEventStage = null;

    // ── Init ──────────────────────────────────────────────────────────────────

    /**
     * Initializes the GameController with the necessary references to the
     * client architecture and sets up the initial UI state (like the mute button and sidebar).
     *
     * @param clientController the main controller handling game logic and network
     * @param guiView          the main view coordinator
     * @param nickname         the local player's confirmed nickname
     */
    public void init(ClientController clientController, GuiView guiView, String nickname) {
        this.clientController = clientController;
        this.guiView = guiView;
        this.myNickname = nickname;

        //initialize the mute button style
        if (guiView.getSoundManager().isMuted()) {
            muteButton.setGraphic(EmojiIcon.of("1f507", 20)); //🔇
        } else {
            muteButton.setGraphic(EmojiIcon.of("1f50a", 20)); // 🔊
        }

        // Shows the player's nickname with a star in the sidebar, and initializes the title
        Label title = new Label("Players");
        title.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #28251d;");

        Label me = new Label(nickname);
        me.setStyle("-fx-font-size: 12; -fx-text-fill: #28251d;");
        playersBox.getChildren().addAll(title, me);

        updateConfirmButtonState();
    }


    // ── Drawer toggle ─────────────────────────────────────────────────────────

    /**
     * Toggles the visibility of the sliding drawer containing the detailed
     * tribe statistics. Triggers a sliding translation animation.
     */
    @FXML
    private void toggleDrawer() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), drawerContainer); //animation

        if (isDrawerOpen) {
            tt.setToX(0);
            drawerToggleButton.setText("◀ Tribe Stats");
        } else {
            tt.setToX(-220); //if it is close move it -220 in the x direction to make it visible
            drawerToggleButton.setText("▶ Close");
        }

        tt.play();
        isDrawerOpen = !isDrawerOpen;
    }

    /**
     * Triggered when a player is selected from the dropdown menu in the tribe stats drawer.
     * Updates the displayed statistics to match the newly selected player.
     */
    // triggered when a player is selected from the dropdown in the drawer, updates the stats shown based on the selected player
    @FXML
    private void onPlayerSelected() {
        String selected = playerSelector.getValue();
        if (selected != null) {
            selectedDrawerPlayer = selected;
            updateDrawerStats(selected);
        }
    }

    /**
     * Updates the labels inside the tribe stats drawer based on the specific
     * discounts and elements of the given player from the current game snapshot.
     *
     * @param nickname the nickname of the player whose stats should be displayed
     */
    // updates the stats shown in the drawer based on the given player's nickname, if the player is found in the current snapshot
    private void updateDrawerStats(String nickname) {
        if (currentSnapshot == null) return;
        for (PlayerDTO p : currentSnapshot.players()) {
            if (p.nickname().equals(nickname)) {
                TribeDTO tribe = p.tribeDTO();
                int susDisc = - tribe.sustenanceDiscount();
                int buildDisc = -tribe.builderDiscount();

                drawerShamanStars.setText("⭐ Stars: " + tribe.shamanStarsCount());
                drawerFoodDiscount.setText("🍖 Food Disc: " + susDisc);
                drawerBuildDiscount.setText("🔨 Build Disc: " + buildDisc);
                drawerDistinctInv.setText("💡 Distinct Inv: " + tribe.distinctInvention());
                drawerInvPairs.setText("🔗 Inv Pairs: " + tribe.inventionPair());

                return;
            }
        }
    }

    // ── Public API called by GuiView ──────────────────────────────────────────

    /**
     * Main entry point: called every time the server pushes a new {@link MatchDTO}.
     * Rebuilds the entire UI from the snapshot.
     *
     * @param snapshot the latest game state
     */
    public void updateGame(MatchDTO snapshot) {
        this.currentSnapshot = snapshot;

        System.out.println("[GameController] updateGame called, players: "
                + snapshot.players().size());

        //check for occurred events
        List<EventResultDTO> results = snapshot.eventResults();
        if(results != null && !results.isEmpty()){
            showEventResult(results);
        }

        //check if the player opened a playerCard popup
        if(openPlayerCardsStage != null && openPlayerCardsStage.isShowing()){
            PlayerDTO updatedPlayer = snapshot.players().stream()
                    .filter(p -> p.nickname().equals(viewedPlayerCardNickname))
                    .findFirst()
                    .orElse(null);

            if(updatedPlayer != null){
                openPlayerCardsController.refresh(updatedPlayer);
            }
        }

        // Check for active players and handle disconnection timer
        int activePlayers = (int) snapshot.players().stream()
                .filter(PlayerDTO::isConnected)
                .count();
        if (activePlayers == 1) {
            startOrUpdateTimer(snapshot.totalDisconnectionTime(), snapshot.remainingDisconnectionTime()); //server side timer sent over the network
            showInfo("Waiting for opponents to reconnect...");
        } else {
            stopTimer();
        }

        if ("RESOLVE OFFER".equals(lastPhaseName)
                && !"RESOLVE OFFER".equals(snapshot.currentPhaseName())) {
            pickSession.clear();
        }

        lastPhaseName = snapshot.currentPhaseName();

        renderHeader(snapshot);
        renderBoard(snapshot.board());
        renderPlayers(snapshot.players());


        if (snapshot.currentPlayer() == null || !myNickname.equals(snapshot.currentPlayer()))
            showWaitingPanel();

        updateConfirmButtonState();
    }


    // RENDERING GUI BASED ON THE PHASE OF THE GAME
    /**
     * Hides all interactive action panels (Totem, Pick Card, Skip, Waiting).
     * Used to reset the bottom UI area before activating the specific panel for the current phase.
     */
    private void hideAllActionPanels() {
        placeTotemPanel.setVisible(false);   placeTotemPanel.setManaged(false);
        pickCardPanel.setVisible(false);     pickCardPanel.setManaged(false);
        resolveOfferPanel.setVisible(false); resolveOfferPanel.setManaged(false);
        skipExtraPanel.setVisible(false);    skipExtraPanel.setManaged(false);
        waitingPanel.setVisible(false);      waitingPanel.setManaged(false);
    }

    /**
     * Activates and displays the specific UI panel instructing the user to place their totem.
     */
    public void activatePlaceTotem() {
        hideAllActionPanels(); //hide panels when you don't have to perform action
        placeTotemPanel.setVisible(true);
        placeTotemPanel.setManaged(true);
    }

    /**
     * Activates and displays the specific UI panel with confirm/skip buttons for resolving an offer.
     */
    public void activateResolveOffer() {
        hideAllActionPanels();
        resolveOfferPanel.setVisible(true);
        resolveOfferPanel.setManaged(true);
    }


    /**
     * Activates and displays the specific UI panel prompting the user to pick an extra card or skip.
     */
    public void activateSkipExtra() {
        hideAllActionPanels();
        skipExtraPanel.setVisible(true);
        skipExtraPanel.setManaged(true);
    }

    /**
     * Activates and displays the waiting panel, disabling interactions when it is not the local player's turn.
     */
    public void showWaitingPanel() {
        hideAllActionPanels();
        waitingPanel.setVisible(true);
        waitingPanel.setManaged(true);
    }

    /**
     * Displays an error message in the feedback label, styled in red.
     *
     * @param message the error text to display
     */
    public void showError(String message) {
        if (feedbackLabel == null) {
            System.err.println("[GUI ERROR] " + message);
            return;
        }
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #ec0f0f;");
    }

    /**
     * Displays an informational message in the feedback label, styled in a neutral dark color.
     *
     * @param message the info text to display
     */
    public void showInfo(String message) {
        if (feedbackLabel == null) {
            System.out.println("[GUI INFO] " + message);
            return;
        }
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #501d0c;");
    }


    // ── Render methods ────────────────────────────────────────────────────────
    /**
     * Updates the top header labels (Round, Era, Phase, Current Player)
     * using the data from the latest match snapshot.
     *
     * @param snapshot the latest game state
     */
    private void renderHeader(MatchDTO snapshot) {

        roundLabel.setText(String.valueOf(snapshot.currentRound()));
        eraLabel.setText(String.valueOf(snapshot.currentEra()));
        phaseLabel.setText(snapshot.currentPhaseName());

        String cp = snapshot.currentPlayer();
        if (cp != null) {
            boolean isMyTurn = cp.equals(myNickname);
            currentPlayerLabel.setText(cp);
        } else {
            currentPlayerLabel.setText("Wait...");
            currentPlayerLabel.setStyle("");
        }
    }


    /**
     * Renders the main game board, including the upper and lower card rows,
     * the offer track tiles, and the turn order section.
     *
     * @param board The current state of the board received from the server.
     */
    private void renderBoard(BoardDTO board) {
        upperRowBox.getChildren().clear();
        for (String cardId : board.upperRowCardIds()) {
            upperRowBox.getChildren().add(buildCardButton(cardId, true));
        }

        lowerRowBox.getChildren().clear();
        for (String cardId : board.lowerRowCardIds()) {
            lowerRowBox.getChildren().add(buildCardButton(cardId, false));
        }

        offerTrackBox.getChildren().clear();
        for (OfferTileDTO tile : board.offerTrackDTO()) {
            offerTrackBox.getChildren().add(buildOfferTileButton(tile));
        }

        renderTurnOrder(board);
    }


    //TURN ORDER TILE

    /**
     * Renders the Turn Order tile and visually places the players' totems
     * on the designated anchor points according to the current queue.
     *
     * @param board the current state of the board
     */
    private void renderTurnOrder(BoardDTO board) {
        turnOrderBox.getChildren().clear();
        turnOrderBox.setAlignment(javafx.geometry.Pos.CENTER);

        int playerCount = currentSnapshot.players().size();
        List<String> order = board.turnOrderTileDTO().playerTopToBottom();
        Pane tilePane = buildTurnOrderTilePane(playerCount, order);
        turnOrderBox.getChildren().add(tilePane);
    }

    /**
     * Builds the pane used to display a turn-order tile and the associated player markers.
     *
     * @param playerCount the number of players
     * @param order the ordered list of player nicknames
     * @return the constructed pane
     */
    private Pane buildTurnOrderTilePane(int playerCount, List<String> order) {
        Pane pane = new Pane();
        pane.setStyle(
                "-fx-background-color: #f9f8f5;" +
                        "-fx-border-color: #dcd9d5;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        String baseImageName = "turnOrderTile" + playerCount;
        Image baseImage;
        try {
            baseImage = CardImageResolver.resolveSync(baseImageName); //uses this method because it pre-retrieves the images, otherwise when you try to use resolve getWidth and getHeight are always 0 (not already uploaded in memory)
        } catch (Exception e) {
            System.err.println("[GUI ERROR] Base Turn Order Tile not found: " + baseImageName);
            Label placeholder = new Label("Turn Order\n(" + playerCount + "p)");
            placeholder.setPrefSize(100, 140);
            placeholder.setAlignment(javafx.geometry.Pos.CENTER);
            placeholder.setStyle("-fx-font-size: 10; -fx-text-fill: #7a7974;");
            pane.getChildren().add(placeholder);
            pane.setPrefSize(100, 140);
            return pane;
        }

        double baseW = baseImage.getWidth();
        double baseH = baseImage.getHeight();
        if (baseW <= 0 || baseH <= 0) {
            System.err.println("[GUI ERROR] Unexpected zero-size image: " + baseImageName);
            pane.setPrefSize(100, 140);
            return pane;
        }

        ImageView baseView = new ImageView(baseImage);
        baseView.setPreserveRatio(true);
        baseView.setSmooth(true);

        double targetTileHeight = 160;
        baseView.setFitHeight(targetTileHeight);

        pane.getChildren().add(baseView);

        double scale = targetTileHeight / baseH;
        double targetTileWidth = baseW * scale;

        pane.setMinSize(targetTileWidth, targetTileHeight);
        pane.setPrefSize(targetTileWidth, targetTileHeight);
        pane.setMaxSize(targetTileWidth, targetTileHeight);

        List<Point2D> anchors = getTurnOrderAnchors(playerCount);

        for (int i = 0; i < Math.min(order.size(), anchors.size()); i++) {
            String nickname = order.get(i);
            if (nickname == null || nickname.isBlank()) continue;

            String totemImageName = buildTotemImageName(nickname);
            if (totemImageName == null) continue;

            try {
                Image totemImage = CardImageResolver.resolveSync(totemImageName);
                ImageView totemView = new ImageView(totemImage);
                totemView.setPreserveRatio(true);
                totemView.setSmooth(true);

                double totemDisplaySize = 35;
                totemView.setFitWidth(totemDisplaySize);

                Point2D anchor = anchors.get(i);
                double x = (anchor.getX() * scale) ;
                double y = (anchor.getY() * scale) ;

                totemView.setLayoutX(x);
                totemView.setLayoutY(y);
                pane.getChildren().add(totemView);

            } catch (Exception e) {
                System.err.println("[GUI ERROR] Totem image not found: " + totemImageName);
            }
        }

        return pane;
    }
    /**
     * Returns the initial used to identify the totem image of the given player.
     *
     * @param nickname the player's nickname
     * @return the uppercase initial of the player's totem color, or {@code null} if the player cannot be resolved
     */
    private String buildTotemImageName(String nickname) {
        if (currentSnapshot == null || currentSnapshot.players() == null) {
            return null;
        }
        for (PlayerDTO p : currentSnapshot.players()) {
            if (p.nickname().equals(nickname)) {
                String color = p.totemColor();
                if (color == null || color.isBlank()) {
                    return null;
                }
                return String.valueOf(color.toUpperCase().charAt(0));
            }
        }
        return null;
    }
    /**
     * Returns the anchor points used to place player markers on the turn-order tile.
     *
     * @param playerCount the number of players
     * @return the list of anchor points for the supported player count
     * @throws IllegalArgumentException if the player count is not supported
     */
    private List<Point2D> getTurnOrderAnchors(int playerCount) {
        return switch (playerCount) { //all the coordinates anchors from upper - left
            case 2 -> List.of(
                    new Point2D(111, 130),
                    new Point2D(111, 228)
            );
            case 3 -> List.of(
                    new Point2D(111, 102),
                    new Point2D(111, 202),
                    new Point2D(111, 303)
            );
            case 4 -> List.of(
                    new Point2D(111, 76),
                    new Point2D(111, 177),
                    new Point2D(111, 277),
                    new Point2D(111, 376)
            );
            case 5 -> List.of(
                    new Point2D(111, 37),
                    new Point2D(111, 138),
                    new Point2D(111, 237),
                    new Point2D(111, 337),
                    new Point2D(111, 436)
            );
            default -> throw new IllegalArgumentException("Unsupported player count: " + playerCount);
        };
    }
    /**
     * Returns the uppercase initial of the color associated with the given player.
     *
     * @param nickname the player's nickname
     * @return the uppercase initial of the player's totem color, or {@code '-'} if unavailable
     */
    private char getPlayerColorInitial(String nickname) {
        if (currentSnapshot == null || currentSnapshot.players() == null) {
            return '-';
        }

        for (PlayerDTO p : currentSnapshot.players()) {
            if (p.nickname().equals(nickname)) {
                String color = p.totemColor();
                if (color == null || color.isBlank()) {
                    return '-';
                }
                return Character.toUpperCase(color.charAt(0));
            }
        }

        return '-';
    }


    /**
     * Helper method to find a player's totem color based on their nickname.
     *
     * @param nickname The nickname of the player to search for.
     * @return The color of the player as a String (e.g., "YELLOW", "BLACK"), or "U" if not found.
     */
    private String getPlayerColor(String nickname) {
        if (currentSnapshot == null || currentSnapshot.players() == null) {
            return "U"; // Unknown
        }

        for (PlayerDTO p : currentSnapshot.players()) {
            if (p.nickname().equals(nickname)) {
                return p.totemColor().toUpperCase();
            }
        }
        return "U";
    }

    /**
     * Renders the list of players into the sidebar players box.
     * Clears any existing children, adds a title label, and then
     * iterates over the players to build and display their individual cards.
     *
     * @param players the list of {@link PlayerDTO} objects to render
     */
    private void renderPlayers(List<PlayerDTO> players) {
        if (playersBox == null) return;

        //ComboBox handling (Tribe Stats drawer)
        if (playerSelector.getItems().size() != players.size()) {
            String prev = playerSelector.getValue();
            playerSelector.getItems().clear();
            for (PlayerDTO p : players) {
                playerSelector.getItems().add(p.nickname()); //add all the players to the dropdown
            }
            //uses previous selected player otherwise sets the player's own nickname as default
            if (prev != null && playerSelector.getItems().contains(prev)) {
                playerSelector.setValue(prev);
                selectedDrawerPlayer = prev;
            } else {
                playerSelector.setValue(myNickname);
                selectedDrawerPlayer = myNickname;
            }
        }
        //forces the update of the selected player at each turn
        updateDrawerStats(selectedDrawerPlayer);

        playersBox.getChildren().clear();

        Label title = new Label("Players");
        title.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #28251d;");
        playersBox.getChildren().add(title);

        //extract me from other players
        PlayerDTO myPlayer = null;
        List<PlayerDTO> otherPlayers = new ArrayList<>();

        for (PlayerDTO p : players) {
            if (p.nickname().equals(myNickname)) {
                myPlayer = p;
            } else {
                otherPlayers.add(p);
            }
        }

        //insert my player box at the top
        if (myPlayer != null) {
            playersBox.getChildren().add(buildPlayerCard(myPlayer, 0));
        }

        //add the other players
        for (int i = 0; i < otherPlayers.size(); i++) {
            playersBox.getChildren().add(buildPlayerCard(otherPlayers.get(i), i + 1));
        }
    }

    // ── FXML handlers ─────────────────────────────────────────────────────────

    /**
     * FXML Handler triggered when the user clicks the "Skip" button during the extra action phase.
     */
    @FXML
    private void onSkipExtraClicked() {
        if (currentSnapshot == null || !myNickname.equals(currentSnapshot.currentPlayer())) {
            showError("Please wait, it's not your turn!");
            return;
        }
        clientController.handleSkipExtra(); //delegate to the client Controller to send the action over the network

        showInfo("Skip extra request sent.");
    }

    /**
     * FXML Handler triggered when the user confirms their card selection during the resolve offer phase.
     * Sends the picked cards to the server.
     */
    @FXML
    private void onConfirmResolveClicked() {
        if (pickSession.isEmpty()) {
            showError("Select at least one card before confirming.");
            return;
        }

        clientController.handleResolveOffer(new ArrayList<>(pickSession.selections()));
        pickSession.clear();
        refreshBoardSelectionStyles();
        updateConfirmButtonState();
        showInfo("Resolve offer request sent.");
    }

    /**
     * FXML Handler triggered when the user clicks the "Skip" button during the extra action phase.
     */
    @FXML
    private void onSkipButtonClicked() {
        //return to the client an empty list as a skip action
        clientController.handleResolveOffer(new ArrayList<>());
    }

    /**
     * FXML Handler triggered when the user confirms their card selection during the resolve offer phase.
     * Sends the picked cards to the server.
     */
    @FXML
    private void updateConfirmButtonState() {
        if (confirmResolveButton != null) {
            confirmResolveButton.setDisable(pickSession.isEmpty());
        }
    }

    /**
     * Forces a complete redraw of the game board to update visual selection styles.
     * <p>
     * This is primarily used during the "Resolve Offer" phase to immediately highlight
     * or un-highlight cards as the user clicks on them (updating the local UI instantly
     * without needing to wait for a new snapshot from the server).
     * </p>
     */
    private void refreshBoardSelectionStyles() {
        if (currentSnapshot != null) {
            renderBoard(currentSnapshot.board());
        }
    }

    // ── Card & tile builders ──────────────────────────────────────────────────

    /**
     * Builds an interactive JavaFX Button styled as a game card (Character or Building).
     *
     * @param cardId   the unique identifier of the card to render
     * @param upperRow true if the card is located in the upper row, false otherwise
     * @return a fully configured {@link Button} containing the card image and click handlers
     */
    private Button buildCardButton(String cardId, boolean upperRow) {
        Button btn = new Button();

        double scale = 1.5;

        btn.setPrefWidth(90);
        btn.setPrefHeight(110);

        Image img = CardImageResolver.resolve(cardId);
        ImageView iv = new ImageView(img);
        iv.setFitWidth(82*scale);
        iv.setFitHeight(102*scale);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        btn.setGraphic(iv);

        Tooltip tip = new Tooltip(cardId);
        tip.setShowDelay(javafx.util.Duration.millis(300));
        btn.setTooltip(tip);

        applyCardBaseStyle(btn);

        if (pickSession.contains(cardId)) {
            applyCardSelectedStyle(btn, pickSession.indexOf(cardId) + 1);
        }

        btn.setOnAction(e -> onCardClicked(cardId, upperRow, btn));
        return btn;
    }

    /**
     * Applies the default style to a card button.
     *
     * @param btn the button to style
     */
    private void applyCardBaseStyle(Button btn) {
        btn.setStyle(
                "-fx-background-color: transparent; "
                        + "-fx-border-color: #dcd9d5; "
                        + "-fx-border-radius: 6; "
                        + "-fx-background-radius: 6; "
                        + "-fx-cursor: hand; "
                        + "-fx-padding: 2;");
    }

    /**
     * Applies the selected style to a card button.
     *
     * @param btn the button to style
     * @param order the selection order
     */
    private void applyCardSelectedStyle(Button btn, int order) {
        btn.setStyle(
                "-fx-background-color: #cedcd8; "
                        + "-fx-border-color: #d68b21; "
                        + "-fx-border-width: 2; "
                        + "-fx-border-radius: 6; "
                        + "-fx-background-radius: 6; "
                        + "-fx-cursor: hand; "
                        + "-fx-padding: 2;");
        btn.setTooltip(new Tooltip("Selected as #" + order + " — " + btn.getTooltip().getText()));
    }

    /**
     * Handles the click on a card button depending on the current phase and turn.
     *
     * @param cardId the clicked card identifier
     * @param upperRow true if the card belongs to the upper row, false otherwise
     * @param btn the clicked button
     */
    private void onCardClicked(String cardId, boolean upperRow, Button btn) {
        if (currentSnapshot == null) return;

        //check if it is the player's turn
        String currentPlayer = currentSnapshot.currentPlayer();
        if (currentPlayer == null || !currentPlayer.equals(myNickname)) {
            showError("Please wait, it's not your turn!");
            return;
        }

        String phase = currentSnapshot.currentPhaseName();

        if (resolveOfferPanel.isVisible()) {
            pickSession.toggle(cardId);
            refreshBoardSelectionStyles();
            updateConfirmButtonState();

            if (pickSession.contains(cardId)) {
                showInfo("Selected #" + (pickSession.indexOf(cardId) + 1) + ": " + cardId);
            } else {
                showInfo("Removed from selection: " + cardId);
            }
            return;
        }

        if (skipExtraPanel.isVisible()) {
            //send the action
            clientController.handleExtraPick(cardId, upperRow);
            showInfo("Sending extra pick request for: " + cardId + "...");
            return;
        }

        //it's your turn, but you are clicking during the wrong phase (e.g. place totem)
        showError("You cannot pick a card during phase: " + phase);
    }

    //OFFER TILE

    /**
     * Builds an interactive JavaFX Button representing an Offer Tile.
     * Automatically adjusts the graphics if the tile is currently occupied by a player's totem.
     *
     * @param tile the {@link OfferTileDTO} containing the tile's data and current occupant
     * @return a fully configured {@link Button} representing the offer tile
     */
    private Button buildOfferTileButton(OfferTileDTO tile) {
        boolean occupied = tile.occupantNickname() != null;
        double scale = 1.4;

        Button btn = new Button();
        btn.setPrefWidth(84*scale);
        btn.setPrefHeight(110*scale);
        btn.setDisable(occupied);

        String imageName;
        if (occupied && !tile.occupantNickname().isEmpty()) {
            String color = getPlayerColor(tile.occupantNickname());
            char colorInitial = color.charAt(0);
            imageName = "offerTile/" + tile.tileId() + colorInitial;
        } else {
            imageName = "offerTile" + tile.tileId();
        }

        double targetHeight = 160;
        double targetWidth = targetHeight * (82.0 / 102.0); //original image scale

        Image img = CardImageResolver.resolve(imageName);
        ImageView iv = new ImageView(img);
        iv.setFitWidth(targetWidth);
        iv.setFitHeight(targetHeight);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        btn.setGraphic(iv);

        if (occupied) {
            btn.setOpacity(0.6);
        }

        String tipText = "Tile " + tile.tileId()
                + (occupied ? " — " + tile.occupantNickname() : " (free)");
        Tooltip tip = new Tooltip(tipText);
        tip.setShowDelay(javafx.util.Duration.millis(300));
        Tooltip.install(btn, tip);

        btn.setStyle(
                "-fx-background-color: #f9f8f5; "
                        + "-fx-border-color: #dcd9d5; "
                        + "-fx-border-radius: 6; "
                        + "-fx-background-radius: 6; "
                        + "-fx-padding: 8; "
                        + "-fx-cursor: hand;");

        btn.setOnAction(e -> {
            if (currentSnapshot == null) return;

            //verify if it's the player's turn before allowing to place the totem
            String currentPlayer = currentSnapshot.currentPlayer();
            if (currentPlayer == null || !currentPlayer.equals(myNickname)) {
                showError("Please wait, it's not your turn!");
                return;
            }
            if (placeTotemPanel.isVisible()) {
                clientController.handlePlaceTotem(tile.tileId());
                showInfo("Placing totem on tile " + tile.tileId() + "...");
            } else {
                //it's your turn, but we are not in the place totem phase
                showError("You cannot place a totem during phase: " + currentSnapshot.currentPhaseName());
            }
        });

        return btn;
    }

    /**
     * Builds a UI component representing a player's quick-stats card in the sidebar.
     * Includes the player's totem, current food, prestige points, and a button to view their tribe.
     *
     * @param p           the {@link PlayerDTO} containing the player's data
     * @param playerIndex the index of the player (used for sorting or styling)
     * @return a {@link VBox} layout representing the player's dashboard
     */
    private VBox buildPlayerCard(PlayerDTO p, int playerIndex) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(8, 10, 8, 10));

        boolean isMe = p.nickname().equals(myNickname);
        card.setStyle("-fx-background-color: " + (isMe ? "#d6bb95" : "#f9f8f5") + "; "
                + "-fx-background-radius: 6; -fx-border-color: #dcd9d5; -fx-border-radius: 6;");

        // --- 1. NAME AND TOTEM SECTION ---
        HBox nameAndTotemBox = new HBox(6);
        nameAndTotemBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String serverColor = p.totemColor();
        String totemImageName = "totem_" + serverColor.toLowerCase();

        double totemScale = 1.2;

        ImageView totemIcon = new ImageView();
        try {
            Image tImg = CardImageResolver.resolve(totemImageName);
            totemIcon.setImage(tImg);
            totemIcon.setFitWidth(48*totemScale);
            totemIcon.setFitHeight(48*totemScale);
            totemIcon.setPreserveRatio(true);
            totemIcon.setSmooth(true);
        } catch (Exception e) {
            System.out.println("Totem image not found for: " + totemImageName);
        }

        boolean isMyTurn = currentSnapshot.currentPlayer() != null &&
                currentSnapshot.currentPlayer().equals(p.nickname());

        Label name = new Label(p.nickname());

        name.setStyle("-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: #501d0c;");

        //add totem and name to the HBox
        nameAndTotemBox.getChildren().addAll(totemIcon, name);

        if (isMyTurn) {
            ImageView turnEmoji = EmojiIcon.of("1f5ff"); //🗿 emoji, it's this player's turn
            nameAndTotemBox.getChildren().add(turnEmoji);
        }

        if (!p.isConnected()) {
            ImageView disconnectionEmoji = EmojiIcon.of("274c"); //❌ emoji, player is disconnected
            nameAndTotemBox.getChildren().add(disconnectionEmoji);
        }

        // --- 2. RESOURCES SECTION ---
        HBox resourcesBox = new HBox(5);
        resourcesBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        double iconScale = 1.8;

        ImageView foodIcon = new ImageView();
        foodIcon.setImage(CardImageResolver.resolve("food_icon"));
        foodIcon.setFitWidth(24*iconScale);
        foodIcon.setFitHeight(24*iconScale);
        foodIcon.setPreserveRatio(true);

        Label foodLabel = new Label(String.valueOf(p.foodTokens()));
        foodLabel.setStyle("-fx-font-size: 20; -fx-text-fill: #000000; -fx-font-weight: bold;");

        //dynamic spacing between food and prestige points
        Region spacing = new Region();
        spacing.setMaxWidth(20);
        HBox.setHgrow(spacing, Priority.ALWAYS);


        ImageView prestigeIcon = new ImageView();
        prestigeIcon.setImage(CardImageResolver.resolve("prestige_icon"));
        prestigeIcon.setFitWidth(24*iconScale);
        prestigeIcon.setFitHeight(24*iconScale);
        prestigeIcon.setPreserveRatio(true);

        Label prestigeLabel = new Label(String.valueOf(p.prestigePoints()));
        prestigeLabel.setStyle("-fx-font-size: 20; -fx-text-fill: #000000; -fx-font-weight: bold;");

        resourcesBox.getChildren().addAll(foodIcon, foodLabel, spacing, prestigeIcon, prestigeLabel);

        // --- 3. BUTTON ---
        Button viewBtn = new Button("View cards");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setStyle("-fx-background-color: #501d0c; -fx-text-fill: white; "
                + "-fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> showPlayerCardsPopup(p));

        card.getChildren().addAll(nameAndTotemBox, resourcesBox, viewBtn);

        return card;
    }

    /**
     * Opens a popup window displaying all the cards (characters and buildings)
     * currently owned by the specified player.
     *
     * @param player the {@link PlayerDTO} whose cards should be displayed
     */
    private void showPlayerCardsPopup(PlayerDTO player) {
        if(openPlayerCardsStage != null && openPlayerCardsStage.isShowing()) {
            openPlayerCardsStage.close();
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/player_cards.fxml"));
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.initOwner(playersBox.getScene().getWindow());
            popup.initModality(javafx.stage.Modality.NONE);
            popup.setTitle(player.nickname() + "'s Cards");
            popup.getIcons().add(new Image("img/icon.png"));
            popup.setResizable(false);

            PlayerCardsController controller = loader.getController();
            controller.init(player, popup);

            //keep track of the popup
            openPlayerCardsStage = popup;
            openPlayerCardsController = controller;
            viewedPlayerCardNickname = player.nickname();
            popup.setOnHidden(e -> {
                openPlayerCardsStage = null;
                openPlayerCardsController = null;
                viewedPlayerCardNickname = null;
            });

            popup.setScene(new javafx.scene.Scene(root, 800, 575));
            popup.show();

        } catch (Exception e) {
            showError("Could not open cards popup: " + e.getMessage());
        }
    }

    /**
     * opens a popup window showing event results
     * @param results list of events with their results
     */
    private void showEventResult(List<EventResultDTO> results){
        if(openEventStage != null && openEventStage.isShowing()) {
            openEventStage.close();
        }

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/event_result.fxml"));
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.initOwner(playersBox.getScene().getWindow());
            popup.initModality(javafx.stage.Modality.NONE);
            popup.setTitle("event result");
            popup.getIcons().add(new Image("img/icon.png"));
            popup.setResizable(false);

            EventResultController controller = loader.getController();
            controller.init(results, popup);

            openEventStage = popup;
            popup.setOnHidden(e -> openEventStage = null);

            popup.setScene(new javafx.scene.Scene(root, 800, 500));
            popup.show();
        }catch(Exception e){
            showError("Could not open event results popup: " + e.getMessage());
        }

    }

    //mute button
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


    //TIMER PROGRESS BAR
    /**
     * Starts or updates the progress bar timer.
     * <p>
     * This method initializes a local JavaFX Timeline to smoothly animate the progress bar
     * without relying on continuous server ticks.
     * </p>
     *
     * @param totalSeconds     The total duration of the timer in seconds (e.g., 30).
     * @param remainingSeconds The current remaining seconds provided by the server.
     */
    public void startOrUpdateTimer(int totalSeconds, int remainingSeconds) {

        //DEBUG
        System.out.println("[DEBUG GUI] Timer Request -> Total: " + totalSeconds + ", Remaining: " + remainingSeconds);

        if (totalSeconds <= 0) {
            System.err.println("[GUI ERROR] totalSeconds from server is 0 or less! Using fallback of 30s.");
        }

        disconnectionPanel.setVisible(true);
        disconnectionPanel.setManaged(true);

        timerProgressBar.setVisible(true); //makes the progress bar visible
        timerProgressBar.setManaged(true); //includes the bar in the layout of the container


        if (remainingSeconds <= 0) { //corner case: if the server sends a negative remaining time
            if (countdownTimeline != null) countdownTimeline.stop();
            isTimerRunning = false;
            timerProgressBar.setProgress(0);
            showInfo("Time is up, we are loading results...");
            return;
        }

        // If the animation is already running, prevent recreation to avoid stuttering
        // upon receiving frequent MatchDTO updates from the server.
        if (countdownTimeline != null && isTimerRunning) {
            return;
        }

        isTimerRunning = true;
        final double[] currentRemaining = {remainingSeconds};

        countdownTimeline = new Timeline( //animations based on time , every 1 seconds you have a keyFrame
                new KeyFrame(Duration.seconds(1), event -> { //variables in the lambda must be final, so we can use an array to modify the variable currentRemaining inside the lambda
                    currentRemaining[0]--;

                    // Calculate the percentage for the progress bar (from 1.0 down to 0.0)
                    double progress = currentRemaining[0] / (double) totalSeconds;
                    timerProgressBar.setProgress(progress); //update the bar

                    // Stop the timeline when the timer reaches zero
                    if (currentRemaining[0] <= 0) {
                        countdownTimeline.stop();
                        isTimerRunning = false;
                        timerProgressBar.setProgress(0);
                        showInfo("Time is up, we are loading results...");
                    }
                })
        );
        countdownTimeline.setCycleCount(Animation.INDEFINITE); //Timeline repeats every second until it is stopped manually
        countdownTimeline.play(); //start the animation
    }

    /**
     * Stops the timer animation and hides the progress bar from the UI.
     * Called when another player reconnects or the timer ends.
     */
    public void stopTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        isTimerRunning = false;
        timerProgressBar.setVisible(false);
        timerProgressBar.setManaged(false);

        disconnectionPanel.setVisible(false);
        disconnectionPanel.setManaged(false);
    }
}
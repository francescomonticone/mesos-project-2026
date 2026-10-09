package it.polimi.ingsw.Client.ClientController;

import it.polimi.ingsw.Client.Network.VirtualServer;
import it.polimi.ingsw.Client.View.View;
import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Network.DTO.OfferTileDTO;
import it.polimi.ingsw.Network.DTO.PlayerDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Main controller for the client-side of the application.
 * <p>
 * It acts as the central intermediary between the {@link View} (UI) and the {@link VirtualServer} (Network).
 * It receives user inputs from the UI, performs client-side validation, and forwards commands
 * to the network layer. It also receives asynchronous updates from the server and updates
 * the local state and UI accordingly.
 * </p>
 */
public class ClientController {

    private final View view;
    private final VirtualServer virtualServer;
    private String myNickname; // Store the nickname for potential future use (e.g., displaying in the UI)
    private MatchDTO latestMatchState; //saves the latest match state received from the server, used for early action rejection and UI rendering

    /**
     * Constructs a new ClientController.
     *
     * @param view          the user interface (GUI or TUI) responsible for displaying the game
     * @param virtualServer the network abstraction used to send commands to the real server
     */
    public ClientController(View view, VirtualServer virtualServer) {
        this.view = view;
        this.virtualServer = virtualServer;
    }

    /**
     * Sets the player's nickname after a successful login.
     * <p>
     * This acts as the single source of truth for the client's identity,
     * used for early action rejection and UI rendering.
     * </p>
     *
     * @param nickname the confirmed nickname
     */
    public void setMyNickname(String nickname) {
        this.myNickname = nickname;
        this.view.setNickname(nickname);
    }

    /**
     * Called by the View when the user types their credentials.
     *
     * @param nickname      the typed nickname
     * @param plainPassword the typed plain-text password
     */
    public void handleLogin(String nickname, String plainPassword) {
        if (nickname == null || nickname.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            view.showError("Nickname and password cannot be empty.");
            return;
        }
        //DO OTHER CONTROLS ON CREDENTIALS?
        if (!nickname.matches("^[a-zA-Z0-9_-]{1,8}$")) {
            view.showError("Nickname can only contains letters, numbers, dashes and underscores with length 1-8 characters.");
            return;
        }

        try {

            String hashedPassword = hashPassword(plainPassword.trim()); //trim deletes spaces at the beginning and at the end of the password, but not in the middle

            //we send to server the hashed password
            virtualServer.login(nickname, hashedPassword);
        } catch (Exception e) {
            view.showError("Failed to communicate with the server.");
        }

    }

    /**
     * Handles the request to retrieve the list of currently open lobbies.
     * <p>
     * This is used both for the initial transition to the lobby browser
     * and for manual refreshes requested by the user.
     * </p>
     */
    public void handleRequestOpenLobbies() {
        try {
            view.showMessage("Fetching available lobbies..."); // Optional: feedback for the user
            virtualServer.requestOpenLobbies();
        } catch (Exception e) {
            view.showError("Failed to retrieve lobbies: " + e.getMessage());
        }
    }


    /**
     * Handles the user's request to create a new lobby.
     * Validates the requested match size before forwarding the command to the server.
     *
     * @param targetSize the number of players required to start the match (must be between 2 and 4)
     */
    public void handleCreateLobby(int targetSize) {
        if (targetSize < 2 || targetSize > 5) {
            view.showError("Match size must be between 2 and 5 players.");
            return;
        }
        try {
            view.showMessage("Lobby creation request sent for " + targetSize + " players...");
            virtualServer.createLobby(targetSize);
        } catch (Exception e) {
            view.showError("Failed to communicate with the server while creating lobby.");
        }
    }

    /**
     * Handles the user's request to join an existing lobby.
     *
     * @param lobbyId the unique ID of the lobby the user wishes to join
     */
    public void handleJoinLobby(int lobbyId) {
        try {

            view.showMessage("Attempting to join lobby " + lobbyId + "...");
            virtualServer.joinLobby(lobbyId);
        } catch (Exception e) {
            view.showError("Failed to communicate with the server while joining lobby.");
        }
    }

    /**
     * Handles the user's request to place their totem on a specific offer tile.
     * <p>
     * Performs a client-side validation to ensure the match
     * has started, and it is actually the current player's turn before sending
     * any network request to the server.
     * </p>
     *
     * @param tileId the character identifier of the chosen tile (e.g., 'A', 'B')
     */
    public void handlePlaceTotem(char tileId) {
        //Client-Side Validation of the action

        // Ensure the match is actually running
        if (this.latestMatchState == null) {
            this.view.showError("The game hasn't started yet! Wait in the lobby.");
            return;
        }

        // Ensure it is this player's turn
        String currentPlayer = this.latestMatchState.currentPlayer();

        if (currentPlayer == null || !currentPlayer.equals(this.myNickname)) {
            String turnInfo = (currentPlayer != null) ? currentPlayer : "Automatic Game Resolution";
            this.view.showError("Please wait, it's not your turn! Current turn: " + turnInfo);
            return; // Stops execution here, no network packet is sent
        }

        //Ensure the tile is Free
        boolean tileExists = false;
        // Iterate through the offer track to find the tile with the specified ID
        for (OfferTileDTO tile : this.latestMatchState.board().offerTrackDTO()) {
            if (tile.tileId() == tileId) {
                tileExists = true;
                //check if there is already an occupant
                if (tile.occupantNickname() != null && !tile.occupantNickname().isBlank()) {
                    this.view.showError("Tile '" + tileId + "' is already occupied by " + tile.occupantNickname() + "!");
                    return;
                }
                break; //more efficient
            }
        }

        if (!tileExists) {
            this.view.showError("Tile '" + tileId + "' does not exist on the board.");
            return; // stops the execution here, no network packet is sent
        }

        // NETWORK TRANSMISSION
        try {
            // Converts the char to uppercase for consistency before sending
            this.virtualServer.placeTotem(Character.toUpperCase(tileId));
        } catch (Exception e) {
            this.view.showError("Network error while placing the totem: " + e.getMessage());
        }
    }

    /**
     * Handles the user's request to resolve an offer tile by taking cards.
     * <p>
     * Performs early rejection to ensure the game is running, it's the player's turn,
     * and the requested cards actually exist on the board. It also automatically
     * separates the cards into upper and lower row lists before sending.
     * </p>
     *
     * @param requestedCards a flat list of card IDs requested by the user
     */
    public void handleResolveOffer(List<String> requestedCards) {
        // Client-Side Validation
        if (this.latestMatchState == null) {
            this.view.showError("The game hasn't started yet!");
            return;
        }

        String currentPlayer = this.latestMatchState.currentPlayer();
        if (currentPlayer == null || !currentPlayer.equals(this.myNickname)) {
            String turnInfo = (currentPlayer != null) ? currentPlayer : "Automatic Resolution";
            this.view.showError("Please wait, it's not your turn! Current turn: " + turnInfo);
            return;
        }

        List<String> boardUp = this.latestMatchState.board().upperRowCardIds();
        List<String> boardDown = this.latestMatchState.board().lowerRowCardIds();

        List<String> upCards = new java.util.ArrayList<>();
        List<String> downCards = new java.util.ArrayList<>();

        for (String cardId : requestedCards) {
            if (boardUp.contains(cardId)) {
                upCards.add(cardId);
            } else if (boardDown.contains(cardId)) {
                downCards.add(cardId);
            } else {
                this.view.showError("Card '" + cardId + "' does not exist on the board!");
                return; // Early rejection triggered!
            }
        }

        //NETWORK TRANSMISSION
        try {
            this.virtualServer.resolveOffer(upCards, downCards, requestedCards); //pass also the orderedCards
        } catch (Exception e) {
            this.view.showError("Network error while resolving offer: " + e.getMessage());
        }
    }

    //routing Network -> View methods

    /**
     * Called by the network layer when the server responds to a login request.
     * Updates internal state if successful, and notifies the View.
     *
     * @param success  true if the login was accepted, false otherwise
     * @param nickname the requested nickname
     */
    public void onLoginResult(boolean success, String nickname) {
        if (success) {
            setMyNickname(nickname); // This will also update the view's nickname internally
        }

        // The Controller tells the View to show the result
        this.view.showLoginResult(success);

        if (success) {
            try {
                virtualServer.requestOpenLobbies();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Called by the network layer when a new game state snapshot is received.
     * Updates the internal state and pushes the changes to the View.
     *
     * @param snapshot the new state of the match
     */
    public void onModelUpdated(MatchDTO snapshot) {
        this.latestMatchState = snapshot;
        this.view.updateModel(snapshot);
    }

    /**
     * Called by the network layer when the list of open lobbies is received.
     *
     * @param lobbies the list of available lobbies
     */
    public void onOpenLobbiesReceived(List<LobbyDTO> lobbies) {
        this.view.showOpenLobbies(lobbies);
    }

    /**
     * Routes an error message from the server to the View.
     *
     * @param message the error description
     */
    public void onServerMessage(String message, boolean isError) {
        if (isError) {
            this.view.showError(message);
        } else {
            this.view.showMessage(message);
        }
    }

    /**
     * Called by the network layer when the server notifies the client that it is their turn
     * to place a totem. Prompts the View to display the specific instruction panel.
     */
    public void onAskPlaceTotem() {

        view.showPlaceTotemPanel();

        view.askAction("👉 ACTION REQUIRED: Type 'place <letter>' to place your Totem (e.g., 'place A').", "👉 ACTION REQUIRED: Click on the tile you want to place your Totem");

    }

    /**
     * Called by the network layer when the server notifies the client that it is their turn
     * to resolve an offer tile. Prompts the View to display the card-picking instructions.
     */
    public void onAskResolveOffer() {
        view.showResolveOfferPanel();

        view.askAction("👉 ACTION REQUIRED: Type 'pick <CH_01> <BL_01> ... ' to resolve your offer.","👉 ACTION REQUIRED: Pick the cards according to your OfferTrack Tile" );
    }

    //EXTRA CARD PICK
    /**
     * Called by the network layer when the client is entitled to take an extra action
     * (e.g., picking an extra card). Prompts the View to display the skip/confirm instructions.
     */
    public void onAskExtraCardPick() {
        view.showSkipExtraPanel();
        view.askAction("👉 ACTION REQUIRED: Type 'extra ch_00' to pick extra card or 'skip' to skip extra action.", "👉 ACTION REQUIRED: Confirm or skip your extra action.");
    }


    /**
     * Handles the user's request to pick an extra card.
     */
    public void handleExtraPick(String cardId, boolean isUpperRow) {
        if (this.latestMatchState == null) return;

        String currentPlayer = this.latestMatchState.currentPlayer();
        if (currentPlayer == null || !currentPlayer.equals(this.myNickname)) {
            this.view.showError("It's not your turn!");
            return;
        }

        try {
            this.virtualServer.pickExtraCard(cardId, isUpperRow);
        } catch (Exception e) {
            this.view.showError("Network error while picking extra card: " + e.getMessage());
        }
    }

    /**
     * Handles the user's request to explicitly skip the extra action.
     */
    public void handleSkipExtra() {
        if (this.latestMatchState == null) return;

        String currentPlayer = this.latestMatchState.currentPlayer();
        if (currentPlayer == null || !currentPlayer.equals(this.myNickname)) {
            this.view.showError("It's not your turn!");
            return;
        }

        try {
            this.virtualServer.skipExtraCard();
        } catch (Exception e) {
            this.view.showError("Network error while skipping extra action: " + e.getMessage());
        }
    }

    /**
     * Handles the user's request to view all players' tribes (dashboards and inventories).
     * <p>
     * Extracts the player list from the latest match state and reorders it so that the
     * client's own tribe is always displayed first, then delegates the rendering to the View.
     * </p>
     */
    public void handleShowTribe() {
        if (latestMatchState == null) {
            view.showError("You are not currently in a match.");
            return;
        }

        List<PlayerDTO> allPlayers = latestMatchState.players();
        List<PlayerDTO> orderedPlayers = new java.util.ArrayList<>();

        // Find and add my player first
        for (PlayerDTO p : allPlayers) {
            if (p.nickname().equals(myNickname)) {
                orderedPlayers.add(p);
                break;
            }
        }

        // Add everyone else
        for (PlayerDTO p : allPlayers) {
            if (!p.nickname().equals(myNickname)) {
                orderedPlayers.add(p);
            }
        }

        // Delegate to the view
        view.showAllTribes(orderedPlayers);
    }

    /**
     * Called by the network layer when the server sends the endgame results.
     *
     * @param matchRanking      ranking of this game
     * @param globalLeaderboard global leaderboard from DB
     * @param numPlayers        number of players of this match
     */
    public void onEndGame(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) {
        // The Controller tells the View to show the endgame screen with the provided data
        this.view.showEndGame(matchRanking, globalLeaderboard, numPlayers);
    }

    /**
     * Hashes a plain-text password using SHA-256.
     *
     * @param password the plain-text password to hash, must not be {@code null}
     * @return the hex-encoded SHA-256 hash of the password (32 BYTE)
     * @throws IllegalStateException if SHA-256 is not available in this JVM
     */
    private String hashPassword(String password) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256"); //selecting hash function
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8)); //transform String in Byte and computing hash as an array of 32 BYTE
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b)); //2 digits of HEX for each byte (64 HEX character final string)
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Retrieves the View instance managed by this controller.
     *
     * @return the active {@link View}
     */
    public View getView() {
        return view;
    }
}
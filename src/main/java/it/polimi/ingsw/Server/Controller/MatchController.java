package it.polimi.ingsw.Server.Controller;

import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.*;
import it.polimi.ingsw.Server.Model.Match.GameState.EndGameState;
import it.polimi.ingsw.Server.Model.Match.GameState.GameAction;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.GameState.GameState;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.SnapshotBuilder;
import it.polimi.ingsw.Server.Network.VirtualView;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The main controller for a game match. It acts as the orchestrator between
 * the network layer (VirtualViews) and the game logic layer (MatchModel & GameState).
 *
 * <p>Responsibilities:</p>
 * <ul>
 * <li>Receiving actions from the clients.</li>
 * <li>Delegating the execution of actions to the current {@link GameState}.</li>
 * <li>Updating the MatchModel with the new GameState.</li>
 * <li>Broadcasting model updates to all connected clients safely without blocking the server.</li>
 * <li>Handling client disconnections robustly.</li>
 * </ul>
 *
 * <p>This controller adheres strictly to the State Pattern: it does NOT contain
 * game rules or validation logic. All action validations and model modifications
 * are handled by the specific {@link GameState} implementations.</p>
 * @see GameState
 * @see GameAction
 */
public class MatchController implements ServerActionExecutor {

    /**
     * The game model representing the state, data, and rules of the current match.
     */
    private final MatchModel model;

    /**
     * A concurrent map associating a player's nickname to their specific network abstraction.
     * Ensures thread-safe updates when clients connect or disconnect.
     */
    // maps a player's nickname to their specific network abstraction (VirtualView)
    private final Map<String, VirtualView> clientViews;

    /**
     * A monitor object used to synchronize access and ensure thread-safe processing
     * of incoming network actions and game state mutations.
     */
    // lock object to ensure thread-safe processing of incoming network actions
    private final Object lock = new Object();

    /**
     * A timer used to handle the match suspension period when only one player remains connected.
     */
    //disconnections handling Timer
    private java.util.Timer suspensionTimer;

    /**
     * The timeout duration in seconds before the match is awarded by abandonment
     * to the last connected player.
     */
    private static final int TIMEOUT_S = 30; //timeout in seconds  (will be converted in ms)

    /**
     * A callback executed when the match officially concludes.
     * Typically used to notify the ServerManager to clean up resources and remove the match from active lists.
     */
    // The callback to execute when the match is finished (e.g., to notify the ServerManager to clean up resources)
    private final Consumer<MatchController> onMatchFinishedCallback; //passed inside LobbyController as a lambda when the match is created, this code is saved and will be called at the end of the match to clean up resources in the ServerManager

    /**
     * Constructs a new MatchController.
     *
     * @param model       the game model to manage
     * @param clientViews a map linking player nicknames to their network interfaces
     * @param onMatchFinishedCallback a function to call when the match officially ends
     */
    public MatchController(MatchModel model, Map<String, VirtualView> clientViews, Consumer<MatchController> onMatchFinishedCallback) {
        this.model = model;
        this.clientViews = new java.util.concurrent.ConcurrentHashMap<>(clientViews); //ConcurrentHashMap can be modified and it is thread-safe and efficient
        this.onMatchFinishedCallback = onMatchFinishedCallback;

        // assuming the model is already initialized with an initial GameState

        for (VirtualView view : this.clientViews.values()) {
            view.setMatchController(this); //we set match controller for each view so they can call back to it when they receive network commands from the client
        }
        model.setTotalDisconnectionTime(TIMEOUT_S);
    }

    /**
     * Kicks off the match state machine and broadcasts the initial state.
     * <p>
     * This method is executed by the dedicated Match-Thread. If any player
     * disconnects during this initial broadcast, the exception is safely caught
     * individually per player, triggering the resilience mechanism without
     * crashing the main game thread.
     * </p>
     */
    public void startGame() {
        synchronized (lock) {
            System.out.println("[MatchController] Igniting the State Machine...");
            //first state of the match is set to BoardRegenerationState
            //BoardRegenerationState is automatic -> then TotemPlacementState
            checkAndResolveAutomaticStates();

            //build the first snapshot to send to the clients, we do it here while holding the lock to ensure data consistency
            MatchDTO initialSnapshot = SnapshotBuilder.build(model); //first Board snapshot, initial game
            broadcastMessage("The match has officially started! Board is ready.");
            //broadcast the update
            broadcastModelUpdate(initialSnapshot);

            if (!model.isMatchFinished()) {
                promptCurrentPlayer();
            }
            //requestActionFromTurnPlayers();
        }
    }

    /**
     * Identifies whose turn it is and sends them a request to act.
     * If the current player's connection is lost, it immediately triggers
     * the disconnection handling to skip them or suspend the game.
     */
    private void promptCurrentPlayer() {

        //If the suspension timer is active (only 1 player left), do not prompt for moves
        if (model.getSuspensionDeadline() > 0) {
            return;
        }

        //retrieve the player who is supposed to play in the current state
        Player currentPlayer = null;
        if(model.getCurrentState().getCurrentPlayer().isPresent()) { //default is empty optional, for interactive states it returns the player
           currentPlayer= model.getCurrentState().getCurrentPlayer().get();
        }

        if (currentPlayer == null) return; //if there is no current player (e.g., in an automatic state), we don't prompt anyone

        GameState currentState = model.getCurrentState();
        String nickname = currentPlayer.getNickname();

        //The player is disconnected (and the match is not suspended) so random actions handling
        if (!currentPlayer.isConnected()) {
            System.out.println("[MatchController] " + nickname + " is disconnected. Generating random move for: " + currentState.getClass().getSimpleName());

            // Delegate the creation of a valid action to the current state
            GameAction randomAction = currentState.generateRandomAction(model);

            // Execute the action with a slight asynchronous delay (e.g., 1.5 seconds).
            // This prevents deep recursion if multiple players are disconnected(otherwise we would have possibly a long chain of recursive method invocation so with the timer we prevent StackOverflowError),
            // and this visually simulates the bot "thinking" for the remaining connected players.
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    onActionReceived(randomAction); //handle the random action
                }
            }, 1500); //we make it asynchronous

            return; // Stop here, the bot will execute the move shortly (after timer ends)
        }


        VirtualView view = clientViews.get(nickname); //take the virtual view of that player from nickname

        if (view != null) {
            try {
                currentState.requestAction(view); //request the action with double dispatch

                System.out.println("[MatchController] Requested " + nickname + " for their action. Phase: " + currentState.getClass().getSimpleName());
            } catch (Exception e) {
                System.err.println("[MatchController] Failed to request action to: " + nickname + ". Connection lost.");
                // If the player dropped exactly when their turn started, handle it immediately
                handleDisconnection(nickname);
            }
        }
    }

    // action processing methods
    /**
     * Receives a {@link GameAction} from the network layer.
     * <p>
     * The controller delegates the resolution of the action to the current
     * {@link GameState}. If the state validates and executes the action successfully,
     * the controller updates the model and broadcasts the changes.
     * Network broadcasting is done asynchronously outside the synchronized block
     * to prevent slow clients from freezing the server.
     * </p>
     *
     * @param action the action requested by the client
     */
    public void onActionReceived(GameAction action) {
        MatchDTO snapshotToBroadcast = null;

        synchronized (lock) {  //synchronize access to the model and game state if there are multiple actions acting at the same time
            String nickname = action.getPlayerNickname();

            //check if the player is part of this match
            if (!isPlayerInMatch(nickname)) {
                System.err.println("[MatchController] Blocked action: " + nickname + " is not in this match.");
                return;
            }

            try {
                //GameState machine delegation: ask the current state to resolve the action
                GameState currentState = model.getCurrentState();

                //Double Dispatch: the action knows how to apply itself to the current state
                GameState nextState = action.applyTo(currentState, model);

                //update the model with the new state
                model.setCurrentState(nextState);

                //trigger automatic states (e.g., EventResolutionState, BoardResolutionState)
                checkAndResolveAutomaticStates();

                //build the snapshot while holding the lock to ensure data consistency
                snapshotToBroadcast = SnapshotBuilder.build(model);

            } catch (IllegalActionException e) {
                //if the GameState rejects the action (e.g., "Not enough food")
                notifyError(nickname, e.getMessage());
            } catch (Exception e) {
                //catch unexpected errors to prevent the server from crashing
                notifyError(nickname, "An unexpected error occurred processing your action.");
                e.printStackTrace();
            }
        } // the lock is released here. Other threads can now process actions.

        // broadcast to all virtual views the updated model
        if (snapshotToBroadcast != null && !model.isMatchFinished()) {
            broadcastModelUpdate(snapshotToBroadcast);
            //Game Loop continuation
            // Prompt the next player to make their move.
            // If the game is over, we stop asking for input.
            if (!model.isMatchFinished()) {
                promptCurrentPlayer();
            }
        }
    }

    // automatic states handling

    /**
     * Checks if the current state is an automatic state (a state that doesn't
     * wait for user input, like resolving an event or gaining automatic food).
     * If so, it executes it immediately and checks the next state recursively.
     * @see GameState
     */
    private void checkAndResolveAutomaticStates() {
        GameState currentState = model.getCurrentState();

        //GameState has an onEnter method that returns an Optional<GameState>
        Optional<GameState> nextStateOpt = currentState.onEnter(model);

        //the while loop allows us to chain multiple automatic states together (e.g. gaining automatic food from tile A)
        //as soon as a state returns an empty Optional, we stop the loop because it means we reached a state that waits for user input
        while (nextStateOpt.isPresent()) {
            model.setCurrentState(nextStateOpt.get());
            currentState = model.getCurrentState();
            nextStateOpt = currentState.onEnter(model);
        }

        if (model.isMatchFinished()) { // if the match is finished after resolving automatic states, we trigger the end game sequence
            //take data computed by the EndGameState in the model to send to the clients
            //broadcast the end game message to all clients
            this.broadcastEndGame(model.getFinalMatchRanking(), model.getGlobalLeaderboard(), model.getPlayers().size());

            //clean up the match from the ServerManager's active list with the callback
            if (onMatchFinishedCallback != null) {
                onMatchFinishedCallback.accept(this); //execute the callback that removes the match from the ServerManager's active matches list
            }
        }
    }

    //disconnection handling
    /**
     * Handles a client disconnection according to Resilience Requirements.
     * The player is marked as disconnected.
     * If only one player remains, the match is suspended and a timeout starts.
     *
     * @param nickname the nickname of the disconnected player
     */
    public void handleDisconnection(String nickname) {
        MatchDTO snapshotToBroadcast = null;
        String messageToBroadcast;

        System.out.println("[DEBUG] Disconnect called per " + nickname
                + " | Controller ID: " + this.hashCode()
                + " | is the game finished? " + model.isMatchFinished());

        synchronized (lock) {
            if (model.isMatchFinished()) return;

            Optional<Player> p = model.getPlayerByNickname(nickname);
            if (p.isPresent() && !p.get().isConnected()) {
                return; //the player is already disconnected, we don't send other messages to prevent infinite message broadcast
            }

            //removing old view from the client
            clientViews.remove(nickname); //we remove the view (if the client reconnects it will add the new virtualView)

            // mark the player as disconnected in the model
            model.setPlayerDisconnected(nickname);

            int activePlayersCount = getActivePlayersCount();

            //decide what to do based on remaining players
            if (activePlayersCount > 1) {
                messageToBroadcast = "Player " + nickname + " disconnected. Their turns will be played by the bot.";

                boolean isTheirTurn = model.getCurrentState().getCurrentPlayer()
                        .map(pl -> pl.getNickname().equals(nickname))
                        .orElse(false);

                if (isTheirTurn) {
                    System.out.println("[MatchController] Disconnected player " + nickname + " was supposed to play. Triggering bot move immediately.");
                    promptCurrentPlayer(); //triggering bot move immediately
                }
                checkAndResolveAutomaticStates();
                snapshotToBroadcast = SnapshotBuilder.build(model);

            } else if (activePlayersCount == 1) {
                messageToBroadcast = "Player " + nickname + " disconnected. Only one player left! Match suspended for "+ TIMEOUT_S +" seconds...";
                startSuspensionTimer();
                snapshotToBroadcast = SnapshotBuilder.build(model); //send a new DTO for the progress bar

            } else {
                messageToBroadcast = "All players disconnected. Match paused.";
                if (suspensionTimer != null) suspensionTimer.cancel();

                //set match as finished
                model.setMatchFinished(true);

                //execute callback
                if (onMatchFinishedCallback != null) {
                    onMatchFinishedCallback.accept(this); //we use a callback to avoid having the serverManager reference to have decoupling and to avoid circular dependencies
                }

                return;
            }
        }

        //asynchronous broadcast
        broadcastMessage(messageToBroadcast);
        broadcastModelUpdate(snapshotToBroadcast);
    }
    /**
     * Handles a client reconnection.
     * Updates the network view, marks the player as connected, and stops the suspension
     * timer if the match was suspended.
     *
     * @param nickname the reconnecting player's nickname
     * @param newView  the new VirtualView abstraction for the reconnected client
     */
    public void handleReconnection(String nickname, VirtualView newView) {
        MatchDTO snapshotToBroadcast = null;
        String messageToBroadcast = null;
        boolean shouldReprompt = false; //flag to resend the action to the client

        synchronized (lock) {
            if (model.isMatchFinished()) return;

            //update the network map with the new connection
            clientViews.put(nickname, newView);

            //rewiring the matchController, this is crucial otherwise it can't handle actions from the client anymore
            newView.setMatchController(this);//set the match controller for the new view so it can call back to it when it receives network commands from the client
            
            model.setPlayerConnected(nickname); //sets isConnected = true

            //stop the timer if it was running
            if (suspensionTimer != null) {
                suspensionTimer.cancel(); //stops the timer because total active players are now 2 again
                suspensionTimer = null;
                model.setSuspensionDeadline(0); //reset the suspension deadline in the model so that clients can hide the progress bar

                messageToBroadcast = "Match resumed! Player " + nickname + " has reconnected.";
            }
            else {
                messageToBroadcast = "Player " + nickname + " has reconnected.";
            }
            //check if it is the players turn, if so we resend ask action to play immediately
            boolean isTheirTurn = model.getCurrentState().getCurrentPlayer()
                    .map(p -> p.getNickname().equals(nickname))
                    .orElse(false);
            if (isTheirTurn) {
                shouldReprompt = true; //after the broadcast send resend the action
            }

            //prepare the current state to everyone (especially the newly reconnected player)
            snapshotToBroadcast = SnapshotBuilder.build(model);
        }
        broadcastMessage(messageToBroadcast);
        broadcastModelUpdate(snapshotToBroadcast);

        if (shouldReprompt) { //we resend the ask action after the model snapshot so the GUI will load the scene and then receive the request
            promptCurrentPlayer(); //we do this outside of the synchronized to be more efficient
        }
    }

    //timer utility
    /**
     * Starts a timer when only one player is left.
     * If the timer expires before someone reconnects, the remaining player wins.
     */
    private void startSuspensionTimer() {
        if (suspensionTimer != null) suspensionTimer.cancel(); // safety check to avoid multiple timers

        long deadline = System.currentTimeMillis() + TIMEOUT_S*1000; //set deadline of the timer
        model.setSuspensionDeadline(deadline); //the snapshotBuilder will send this to the network for the only client left for the progress bar

        suspensionTimer = new java.util.Timer(); //create the timer as a separate thread
        suspensionTimer.schedule(new java.util.TimerTask() { //schedule(Task, delay(ms)) we create a TimerTask with run() method overridden
            @Override
            public void run() {  //task to be scheduled as a lambda
                String messageToBroadcast = null;

                boolean triggerEndGame = false; // Flag to indicate if we need to show the leaderboard

                synchronized (lock) { //protects the model from multiple access and makes it thread safe
                    //when the timer expires, check if we still have only 1 player
                    if (getActivePlayersCount() == 1 && !model.isMatchFinished()) {
                        model.setMatchFinished(true); //last player will be considered the winner

                        /*
                         * We force the EndGameState and call onEnter() manually.
                         * This guarantees that final scores are calculated, the database is updated,
                         * and the leaderboard DTOs are populated before we broadcast the end of the game.
                         */
                        GameState endState = new EndGameState();
                        model.setCurrentState(endState);
                        endState.onEnter(model);

                        //find the only remaining player
                        Player winner = model.getPlayers().stream()
                                .filter(Player::isConnected)
                                .findFirst()
                                .orElse(null);

                        if (winner != null) {
                            model.setWinners(List.of(winner));
                            messageToBroadcast = "Timeout reached! " + winner.getNickname() + " wins by abandonment!";
                        }

                        // signal that the game is over, and we need to show rankings
                        triggerEndGame = true;

                    }
                }
                //asynchronous broadcast, the broadcast message can be sent without holding the lock
                if (messageToBroadcast != null) broadcastMessage(messageToBroadcast); //message that the player wins for abandonment

                if (triggerEndGame) {
                    // Force the clients to switch to the End Game Leaderboard view
                    broadcastEndGame(model.getFinalMatchRanking(), model.getGlobalLeaderboard(), model.getPlayers().size());

                    if (onMatchFinishedCallback != null) {
                        onMatchFinishedCallback.accept(MatchController.this); // Notify the ServerManager to clean up this match, we pass the matchController with this syntax because TimerTask is an inner task
                    }
                }

            }
        }, TIMEOUT_S*1000); //how much the delay of the timer is
    }

    //helpers

    /**
     * Counts how many players are currently connected.
     *
     * @return the number of active players
     */
    private int getActivePlayersCount() {
        return (int) model.getPlayers().stream()
                .filter(Player::isConnected)
                .count();
    }

    /**
     * Broadcasts a generic text message to all connected players.
     *
     * @param message the message to broadcast
     */
    private void broadcastMessage(String message) {
        clientViews.forEach((nickname, view) -> {
            try {
                view.showMessage(message); // Assuming VirtualView has a showMessage method
            } catch (Exception e) {
                // If they fail to receive, don't call handleDisconnection recursively here
                // to avoid ConcurrentModificationExceptions, just ignore.
            }
        });
    }

    //helpers

    /**
     * Broadcasts the serialized game state to all connected clients.
     *
     * @param snapshot the DTO representing the public state of the game
     */
    public void broadcastModelUpdate(MatchDTO snapshot) {
        clientViews.forEach((nickname, view) -> {
            try {
                view.sendGameUpdate(snapshot);
            } catch (Exception e) {
                // If we can't reach the client, treat it as a disconnection
                handleDisconnection(nickname);
            }
        });
    }


    /**
     * Broadcasts the final game results to all connected clients.
     * <p>
     * This method iterates through all active client views and attempts to send
     * the end-game message. If a client is unreachable (e.g., due to a network
     * drop right at the end of the match), it safely catches the exception and
     * triggers the disconnection handler for that specific player without
     * crashing the broadcast loop for the others.
     * </p>
     *
     * @param matchRanking      the sorted list of players representing the final standings of this match
     * @param globalLeaderboard the sorted list of historical best scores retrieved from the database
     * @param numPlayers        the total number of players that participated in this match
     */
    public void broadcastEndGame(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) {
        clientViews.forEach((nickname, view) -> {
            try {
                view.sendGameOver(matchRanking, globalLeaderboard, numPlayers);
            } catch (Exception e) {
                // If we can't reach the client, treat it as a disconnection
                handleDisconnection(nickname);
            }
        });
    }

    /**
     * Sends an error message to a specific player.
     *
     * @param nickname the target player's nickname
     * @param message  the error message to send
     */
    private void notifyError(String nickname, String message) {
        try {
            VirtualView view = clientViews.get(nickname);
            if (view != null) {
                view.sendError(message);
            }
        } catch (Exception e) {
            // If the message fails to send, the client is unreachable
            handleDisconnection(nickname);
        }
    }
    

    /**
     * Checks if a player with the given nickname is a participant in this match.
     * <p>
     * This check is performed in the MatchModel to ensure accuracy even if
     * the player is currently disconnected or their VirtualView has been removed.
     * </p>
     *
     * @param nickname the nickname of the player to check
     * @return {@code true} if the player is part of the match, {@code false} otherwise
     */
    public boolean isPlayerInMatch(String nickname) {
        if (nickname == null) return false;

        // we ask the model if this player exists in the match's player list
        return model.getPlayerByNickname(nickname).isPresent();
    }


    // ServerActionExecutor Implementation

    /**
     * Translates a network request to place a totem into a server-side {@link PlaceTotemAction}
     * and submits it for execution.
     *
     * @param nickname the nickname of the player making the request
     * @param tileId   the identifier of the tile where the totem should be placed
     */
    @Override
    public void executePlaceTotem(String nickname, char tileId) {
        //we create the Server model action here.
        GameAction serverAction = new PlaceTotemAction(nickname, tileId);

        //we pass it to your existing, perfectly working method!
        this.onActionReceived(serverAction);
    }

    /**
     * Translates a network request to resolve an offer tile into a server-side
     * {@link ResolveOfferTileAction} and submits it for execution.
     *
     * @param nickname     the nickname of the player making the request
     * @param upCards      the list of card IDs that were placed facing up
     * @param downCards    the list of card IDs that were placed facing down
     * @param orderedCards the final ordered sequence of cards chosen by the player
     */
    @Override
    public void executeResolveOffer(String nickname, List<String> upCards, List<String> downCards, List<String> orderedCards) {

        GameAction serverAction = new ResolveOfferTileAction(nickname, upCards, downCards, orderedCards);
        this.onActionReceived(serverAction);
    }

    /**
     * Translates a network request to pick an extra card into a server-side
     * {@link ExtraCardPickAction} and submits it for execution.
     *
     * @param nickname   the nickname of the player making the request
     * @param cardId     the identifier of the extra card chosen
     * @param isUpperRow {@code true} if the card was picked from the upper row, {@code false} if from the lower row
     */
    @Override
    public void executeExtraCardPick(String nickname, String cardId, boolean isUpperRow) {
        GameAction serverAction = new ExtraCardPickAction(nickname, cardId, isUpperRow);
        this.onActionReceived(serverAction);
    }

    /**
     * Translates a network request to voluntarily skip the extra card picking phase
     * into a server-side {@link SkipExtraAction} and submits it for execution.
     *
     * @param nickname the nickname of the player choosing to skip the action
     */
    @Override
    public void executeSkipExtra(String nickname) {
        GameAction serverAction = new SkipExtraAction(nickname);
        this.onActionReceived(serverAction);
    }


}
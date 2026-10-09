package it.polimi.ingsw.Server.Model.Match;
import it.polimi.ingsw.Network.DTO.EventResultDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.Deck;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.GameState.GameState;

import java.util.*;

/**
 * Represents the core state of a Mesos match.
 * Contains all the data structures required to play the game.
 */
public class MatchModel {
    private int matchId;
    private GameState currentState;
    private int currentRound;
    private Era currentEra;
    private Map<Era, Deck<BuildingCard>> buildingDecksByEra;
    private Deck<Card> mainDeck;
    private final List<Player> players;
    private Board board;

    //TIMER VARIABLES
    private long suspensionDeadline = 0;
    private int totalDisconnectionTime;


    private List<MatchResultDTO> finalMatchRanking; //the rank of this match
    private List<MatchResultDTO> globalLeaderboard; //the rank of all matches

    //end game variables
    private List<Player> winners; //Empty until the match is finished, then it will be set with the winning players
    private boolean matchFinished = false;

    //runtime computation
    private final int distinctCharactersCount; //number of distinct characters (a set), see PointsPerCompleteSet

    //event result lists
    private List<EventResultDTO> currentRoundEventResults = new ArrayList<>();


    /**
     * Minimal constructor used by the LobbyController to initiate the match creation.
     * Decks, Board, and States will be initialized subsequently by the game setup logic.
     *
     * @param matchId    the unique database-generated match ID
     * @param numPlayers the number of players that will participate
     */
    public MatchModel(int matchId, int numPlayers, int distinctCharactersCount) {
        this.matchId = matchId;
        this.players = new ArrayList<>(numPlayers);
        this.winners = new ArrayList<>();
        this.matchFinished = false;

        // Default starting values
        this.currentRound = 0; // start from round 0, will be incremented to 1 at the beginning of the first round in boardRegenerationState
        this.currentEra = Era.I; //start from the first ERA
        this.distinctCharactersCount = distinctCharactersCount;
    }

    public int getMatchId() {
        return matchId;
    }


    public int getCurrentRound() {
        return currentRound;
    }


    public List<MatchResultDTO> getFinalMatchRanking() {
        return finalMatchRanking;
    }

    public void setFinalMatchRanking(List<MatchResultDTO> finalMatchRanking) {
        this.finalMatchRanking = finalMatchRanking;
    }

    public List<MatchResultDTO> getGlobalLeaderboard() {
        return globalLeaderboard;
    }

    public void setGlobalLeaderboard(List<MatchResultDTO> globalLeaderboard) {
        this.globalLeaderboard = globalLeaderboard;
    }

    //setter for player connection/disconnection
    public void setPlayerDisconnected(String nickname){
        Player player = getPlayerByNickname(nickname).orElseThrow(() -> new IllegalArgumentException("Player with nickname " + nickname + " not found"));
        player.setConnected(false);
    }

    public void setPlayerConnected(String nickname){
        Player player = getPlayerByNickname(nickname).orElseThrow(() -> new IllegalArgumentException("Player with nickname " + nickname + " not found"));
        player.setConnected(true);
    }

    //TIMER DTO methods

    public void setSuspensionDeadline(long deadline) {
        this.suspensionDeadline = deadline;
    }

    public long getSuspensionDeadline() {
        return this.suspensionDeadline;
    }

    public int getTotalDisconnectionTime() {
        return this.totalDisconnectionTime;
    }
    public void setTotalDisconnectionTime(int totalDisconnectionTime) {
        this.totalDisconnectionTime = totalDisconnectionTime;
    }


    //getter and setters for MatchController
    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState currentState) {
        this.currentState = currentState;
    }

    public boolean isMatchFinished() {
        return matchFinished;
    }

    //getters and setters for GameState machine
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public Era getCurrentEra() {
        return currentEra;
    }

    public void setCurrentEra(Era currentEra) {
        this.currentEra = currentEra;
    }

    public Map<Era, Deck<BuildingCard>> getBuildingDecksByEra() {
        return buildingDecksByEra;
    }

    public void setBuildingDecksByEra(Map<Era, Deck<BuildingCard>> buildingDecksByEra) {
        this.buildingDecksByEra = buildingDecksByEra;
    }

    public Deck<Card> getMainDeck() {
        return mainDeck;
    }

    public void setMainDeck(Deck<Card> mainDeck) {
        this.mainDeck = mainDeck;
    }

    public List<Player> getPlayers() {
        return List.copyOf(players); //defensive copy
    }

    /**
     * Adds a player to the match.
     *
     * @param player the player to add
     * @throws IllegalStateException if the match is already full
     */
    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int getDistinctCharactersCount() { //distinct Characters counter
        return distinctCharactersCount;
    }

    /**
     * Flags the match as finished.
     * This signals the Game Controller to stop accepting new standard actions.
     *
     * @param matchFinished true if the match is officially over
     */
    public void setMatchFinished(boolean matchFinished) {
        this.matchFinished = matchFinished;
    }


    /**
     * Sets the winners of the match.
     * Multiple winners are allowed to support tie scenarios.
     *
     * @param winners the list of winning players
     * @throws IllegalArgumentException if the winners list is null or empty
     */
    public void setWinners(List<Player> winners) {
        if (winners == null || winners.isEmpty()) {
            throw new IllegalArgumentException("Winners list cannot be null or empty.");
        }
        this.winners = new ArrayList<>(winners); // defensive copy to avoid external modification
    }

    public Optional<Player> getPlayerByNickname(String nickname) {
        return players.stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst();
    }

    //Event Results to send over the network by the snapshot builder
    /**
     * Adds the result of a resolved event to the current round's collection.
     *
     * @param result the {@link EventResultDTO} containing the details of the applied event
     */
    public void addEventResult(EventResultDTO result) {
        this.currentRoundEventResults.add(result);
    }

    /**
     * Retrieves the list of event results for the current round.
     * <p>
     * This method returns a defensive copy of the underlying list to ensure
     * that external modifications do not affect the internal state of the model.
     * </p>
     *
     * @return a new {@link List} containing the current round's {@link EventResultDTO}s
     */
    public List<EventResultDTO> getCurrentRoundEventResults() {
        return new ArrayList<>(currentRoundEventResults);
    }

    /**
     * Clears all recorded event results.
     * <p>
     * This is called at the end of the current round to reset the event history.
     * </p>
     */
    public void clearEventResults() {
        this.currentRoundEventResults.clear();
    }
}

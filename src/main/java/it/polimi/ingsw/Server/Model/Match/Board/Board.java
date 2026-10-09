package it.polimi.ingsw.Server.Model.Match.Board;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.Card;

import java.util.*;

/**
 * Represents the central game board for a match.
 * <p>
 * The Board acts as the primary shared state for all players. It manages the physical
 * components such as the {@link OfferTrack} and the {@link TurnOrderTile}, as well as
 * the dynamic card pools available for drafting. Cards are organized into upper and
 * lower rows, with separate tracking for standard cards (Characters and Events) and
 * Building cards to facilitate the shifting mechanics at the end of each round.
 * </p>
 */
public class Board {
    private List<Card> upperRow;
    private final List<Card> lowerRow;
    private final List<Card> upperBuildings;
    private final List<Card> lowerBuildings;
    private final TurnOrderTile turnOrderTile;
    private final OfferTrack offerTrack;


    /**
     * Constructs a new Board for a brand-new match.
     * Initializes all card rows as empty lists and sets up the physical tiles
     * (TurnOrderTile and OfferTrack) based on the number of players.
     *
     * @param numPlayers the total number of players participating in the match,
     *                   used to scale the board components.
     */
    public Board(int numPlayers) {
        // Initialize empty lists for character cards and buildings
        this.upperRow = new ArrayList<>();
        this.lowerRow = new ArrayList<>();
        this.upperBuildings = new ArrayList<>();
        this.lowerBuildings = new ArrayList<>();

        // Initialize the physical board components with the specific player count
        this.turnOrderTile = new TurnOrderTile(numPlayers);
        this.offerTrack = new OfferTrack(numPlayers);
    }


    //used for testing
    public Board(List<Card> upperRow, List<Card> lowerRow, List<Card> upperBuildings, List<Card>  lowerBuildings,
                 TurnOrderTile turnOrderTile, OfferTrack offerTrack) {
        this.upperRow = new ArrayList<>(upperRow);
        this.lowerRow = new ArrayList<>(lowerRow);
        this.upperBuildings = new ArrayList<>(upperBuildings);
        this.lowerBuildings = new ArrayList<>(lowerBuildings);
        this.turnOrderTile   = turnOrderTile;
        this.offerTrack      = offerTrack;
    }

    /**
     * Shifts Character and Event cards between upper and lower rows when called
     *
     * @param newCards list of card drawn from the deck after the end of each round
     * @throws IllegalArgumentException if {@code newCards} is {@code null} or empty
     */
    public void shiftCards(List<Card> newCards){
        if(newCards == null || newCards.isEmpty()){
            throw new IllegalArgumentException("Card list cannot be null or empty");
        }
        lowerRow.clear(); //removes all the elements
        lowerRow.addAll(upperRow); //copies all the elements from the upper row to the lower row

        upperRow =  new ArrayList<>(newCards);
    }

    /**
     * Shifts Building cards between upper and lower rows when called and adds the new buildings to the upper row
     *
     * @param newBuildings list of card drawn from the deck after the end of each round
     * @throws IllegalArgumentException if {@code newBuildings} is {@code null} or empty
     */
    public void shiftBuildings(List<BuildingCard> newBuildings){
        if(newBuildings == null || newBuildings.isEmpty()){
            throw new IllegalArgumentException("Card list cannot be null or empty");
        }
        lowerBuildings.clear();//removes all the elements
        lowerBuildings.addAll(upperBuildings); //copies the elements from the upper row to the lower row
        upperBuildings.clear();
        upperBuildings.addAll(newBuildings);
    }

    //this is called by the BoardRegenerationState
    /**
     * Clears all non-building cards (Characters and Events) from the lower row.
     * <p>
     * This is typically called by the BoardRegenerationState when the lower row
     * needs to be emptied without affecting buildings.
     * </p>
     */
    public void clearLowerRowNonBuildings() {
        lowerRow.clear();
    }

    /**
     * Clears all Building cards from the lower row.
     */
    public void clearLowerRowBuildings() {lowerBuildings.clear();
    }

    // --- GETTERS --------------------------

    /**
     * Retrieves all cards currently present in the lower row.
     * This includes Character cards, Event cards, and Building cards.
     *
     * @return a consolidated List of all {@link Card}s in the lower row
     */
    public List<Card> getLowerRow() {
        List<Card> row = new ArrayList<>();
        row.addAll(lowerRow);
        row.addAll(lowerBuildings);
        return row;
    }

    /**
     * Retrieves all cards currently present in the upper row.
     * This includes Character cards, Event cards, and Building cards.
     *
     * @return a consolidated List of all {@link Card}s in the upper row
     */
    public List<Card> getUpperRow() {
        List<Card> row = new ArrayList<>();
        row.addAll(upperRow);
        row.addAll(upperBuildings);
        return row;
    }

    /**
     * Get all the Character cards from the upper row
     *
     * @return upper row's Characters as a List of Card
     */
    public List<Card> getUpperRowCharacters() { //this is used to know if you must take cards or not by the OfferTileResolutionState
        List<Card> row = new ArrayList<>(upperRow); //only events and cards
        row.removeIf(card -> !card.isPickable());
        return row;
    }

    /**
     * Get all the Character cards from the lower row
     *
     * @return lower row?s Characters as a List of Card
     */
    public List<Card> getLowerRowCharacters() { //this is used to know if you must take cards or not by the OfferTileResolutionState
        List<Card> row = new ArrayList<>(lowerRow); //only events and cards
        row.removeIf(card -> !card.isPickable());
        return row;
    }

    /**
     * Get all the pickable cards (Characters and Buildings) from the upper row
     *
     * @return upper row's pickable cards as a List of Card
     */
    public List<Card> getUpperPickable(){
        List<Card> row = getUpperRow();
        row.removeIf(card -> !card.isPickable());
        return row;
    }

    /**
     * Get all the pickable cards (Characters and Buildings) from the lower row
     *
     * @return lower row's pickable cards as a List of Card
     */
    public List<Card> getLowerPickable(){
        List<Card> row = getLowerRow();
        row.removeIf(card -> !card.isPickable());
        return row;
    }

    /**
     * Gets the OfferTrack physical component of the board.
     *
     * @return the {@link OfferTrack} associated with this board
     */
    public OfferTrack getOfferTrack(){
        return this.offerTrack;
    }


    /**
     * Gets the TurnOrderTile physical component of the board.
     *
     * @return the {@link TurnOrderTile} associated with this board
     */
    public TurnOrderTile getTurnOrderTile(){
        return this.turnOrderTile;
    }


    /**
     * Retrieves a list of all currently available (unoccupied) offer tiles
     * where a player can legally place their totem.
     * <p>
     * This method delegates the request directly to the {@link OfferTrack}.
     * </p>
     *
     * @return a list of characters representing the IDs of free tiles
     */
    public List<Character> getAvailableTotemTiles() {
        return this.offerTrack.getAvailableTiles();
    }

    // ----------------------------------------------

    /**
     * Adds a card to the lower row of the board.
     * <p>
     * Used primarily for Character cards during the initial setup
     * or when shifting cards from the upper row.
     * </p>
     *
     * @param card the card to be placed in the lower row
     */
    public void addCardToLowerRow(Card card) {
        this.lowerRow.add(card);
    }

    /**
     * Adds a card to the upper row of the board.
     * <p>
     * Used for all types of cards during refill and for Event cards
     * during the initial setup.
     * </p>
     *
     * @param card the card to be placed in the upper row
     */
    public void addCardToUpperRow(Card card) {
        this.upperRow.add(card);
    }

    /**
     * Adds a building to the proper upper row of only buildings of the board.
     * Used by buildings during setup of the board, then the upper row of buildings is shifted to the lower row of buildings and refilled with new buildings during the EraTransitionState.
     *
     * @param card the card to be placed in the upper row
     */
    public void addBuildingToUpperRow(Card card) {
        this.upperBuildings.add(card);
    }

    //methods for OfferTileResolutionState

    /**
     * Removes a specific card from the upper row.
     * <p>
     * It attempts to remove the card from the regular upper row first.
     * If not found, it attempts to remove it from the upper buildings row.
     * </p>
     *
     * @param card the {@link Card} to remove
     * @throws IllegalStateException if the card is not present in any upper row list
     */
    public void removeCardFromUpperRow(Card card) {
        if (!upperRow.remove(card)) //the remove returns true if the object is found, and it is removed from the list
            if (!upperBuildings.remove(card)) //if the card is not in the buildings list too
                throw new IllegalStateException("Card not found in upper row: " + card.getId());
    }

    /**
     * Removes a specific card from the lower row.
     * <p>
     * It attempts to remove the card from the regular lower row first.
     * If not found, it attempts to remove it from the lower buildings row.
     * </p>
     *
     * @param card the {@link Card} to remove
     * @throws IllegalStateException if the card is not present in any lower row list
     */
    public void removeCardFromLowerRow(Card card) {
        if (!lowerRow.remove(card)) //the remove returns true if the object is found, and it is removed from the list
            if (!lowerBuildings.remove(card)) //if the card is not in the buildings list too
                throw new IllegalStateException("Card not found in lower row: " + card.getId());
    }
}

package it.polimi.ingsw.Server.Model.Match;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;

import java.util.Objects;

/**
 * Represents a player in the game.
 * Manages the player's personal state, including resources (food and prestige points),
 * their tribe of cards, board presence (totem), and network connection status.
 */

public class Player {
    private final String nickname; //this is unique and assigned during Player's construction
    private final Tribe tribe;
    private int foodToken;
    private int prestigePoints;
    private TotemColour totem;
    private boolean isConnected;

    private boolean finalPrestigeCalculated = false;

    //constructor for testing
    public Player(String nickname, Tribe tribe, int foodToken, int prestigePoints, TotemColour totem, boolean isConnected) {
        if (nickname == null || nickname.isBlank())
            throw new IllegalArgumentException("Nickname cannot be null or blank");
        this.nickname      = nickname;
        this.tribe = tribe;
        this.foodToken = foodToken;
        this.prestigePoints = prestigePoints;
        this.totem = totem;
        this.isConnected = isConnected;
    }

    /**
     * Constructs a new player with the given nickname.
     * Initializes a new empty Tribe, 0 food, and 0 prestige points.
     * Totem is initially unassigned.
     *
     * @param nickname the unique player identifier
     */
    public Player(String nickname) {
        if (nickname == null || nickname.isBlank())
            throw new IllegalArgumentException("Nickname cannot be null or blank");

        this.nickname = nickname;
        this.tribe = new Tribe(); // Every player starts with an empty Tribe
        this.foodToken = 0;
        this.prestigePoints = 0;
        this.totem = null; // Will be assigned during Game Setup
        this.isConnected = true;
    }

    public String getNickname() {
        return nickname;
    }

    public Tribe getTribe() {
        return tribe;
    }

    /**
     * Gets the current amount of food tokens owned by the player.
     * @return the number of food tokens
     */
    public int getFoodToken() {
        return foodToken;
    }

    public int getPrestigePoint() {
        return prestigePoints;
    }

    public TotemColour getTotem() {
        return totem;
    }

    public void setTotem(TotemColour totem) {
        this.totem = totem;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    /**
     * Adds the specified amount of food to the player's reserve.
     * @param food the amount of food tokens to add
     */
    public void addFood(int food){
        this.foodToken += food;
    }

    /**
     * Removes the specified amount of food from the player's reserve.
     *
     * @param amount the amount of food to remove
     * @throws IllegalArgumentException if the amount to remove is negative
     * @throws IllegalStateException    if the player does not have enough food
     */
    public void removeFood(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount to remove cannot be negative");
        if (this.foodToken < amount)
            throw new IllegalStateException( getNickname() + " does not have enough food: " + "has " + this.foodToken + ", tried to remove " + amount);
        this.foodToken -= amount;
    }

    /**
     * Adds the specified amount of prestige points to the player's score.
     * @param points the amount of prestige points to add
     */
    public void addPrestigePoint(int points){this.prestigePoints += points;}

    /**
     * Removes the specified amount of prestige points from the player's score.
     *
     * @param amount the amount of prestige points to remove
     * @throws IllegalArgumentException if the amount to remove is negative
     */
    public void removePrestigePoint(int amount){
        if (amount < 0)
            throw new IllegalArgumentException("Amount to remove cannot be negative");
        this.prestigePoints -= amount;
    }

    //methods for the Turn Order tile

    /**
     * Adds the food bonus from the Turn Order tile to the player's resources,
     * including any extra bonuses provided by the tribe's buildings.
     * Note: building bonuses are only applied if the base bonus is greater than zero.
     * @param baseBonus the base food bonus provided by the board space
     */
    public void addTurnOrderBonus(int baseBonus) {
        //if the base bonus is 0 or negative, it means that the space didn't give any food, so I don't need to call the Tribe for extra bonuses.
        if (baseBonus <= 0) { //this is because of rules
            return;
        }
        //now the food bonus must be added, so I can call the Tribe for extra bonuses
        int extraBonus = this.getTribe().getExtraTurnOrderBonus();
        this.addFood(baseBonus + extraBonus);
    }

    /**
     * Applies the penalty for placing the totem on the last space of the Turn Order tile.
     * The player pays 1 food if possible; otherwise, they lose 2 Prestige Points.
     */
    public void applyLastSpacePenalty() {
        if (this.getFoodToken() >= 1) { //the player in the last position has to pay 1 food or 2 PP
            this.removeFood(1); //if the player has at least 1 food, he can pay it
        } else {
            this.removePrestigePoint(2);
        }
    }


    //method for End Game computation
    /**
     * Computes the absolute final score at the end of the game.
     * <p>
     * This method sums up the base prestige points collected during the match
     * with all end-game specific bonuses granted by Builders, Inventors, Artists,
     * and specific Building effects.
     * </p>
     */
    public void computeAndApplyFinalPrestige(){
        if (finalPrestigeCalculated)
            throw new IllegalStateException("Final prestige already calculated for " + nickname);

        Tribe t = this.getTribe();
        this.addPrestigePoint(t.getFinalBuildingPrestigePoints()); //adds buildings final PP
        this.addPrestigePoint(t.getFinalBuilderPoints()); //adds builders PP (takes into account building effects)
        this.addPrestigePoint(t.getTotalInventorCount() * t.getDistinctInventionsCount()); //adds distinct inventors bonus
        this.addPrestigePoint(t.getFinalCoupleArtistBonus()); //adds 10 PP for each pair of Artists
        this.addPrestigePoint(t.calculateEndGameBuildingEffects()); //All end game building effects except multipliers (effect that doubles Builder Points )
        finalPrestigeCalculated = true;
    }

    /**
     * Adds a Character card to the player's tribe.
     * This action automatically calculates and adds any passive food bonuses
     * triggered by the player's existing buildings.
     *
     * @param character the {@link CharacterCard} to add to the tribe
     */
    public void addCharacterToTribe(CharacterCard character){
        int buildingFoodBonus = this.tribe.addCharacter(character);
        this.addFood(buildingFoodBonus);
    }

    /**
     * Adds a Building card to the player's tribe.
     * This delegates the addition and the initialization of the building's
     * passive effects to the {@link Tribe} instance.
     *
     * @param building the {@link BuildingCard} to add to the tribe
     */
    public void addBuildingToTribe(BuildingCard building) {
        this.tribe.addBuilding(building);
    }

    /**
     * Compares this player to another object for equality.
     * Two players are considered equal if they share the same nickname.
     *
     * @param o the object to compare to
     * @return {@code true} if the object is a Player with the same nickname, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player other = (Player) o;
        return this.nickname.equals(other.nickname);
    }

    @Override
    public int hashCode() { //equals and hashCode must be consistent
        return Objects.hash(nickname);
    }
}

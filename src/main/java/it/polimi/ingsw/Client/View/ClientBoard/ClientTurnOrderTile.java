package it.polimi.ingsw.Client.View.ClientBoard;

import java.util.List;

/**
 * Represents the turn-order tile, with the number of players and the food bonus list.
 *
 * @param numPlayers the number of players
 * @param foodBonus list of food gain. when the player returns to the turn order tile
 *                  he will gain food based on the index.
 */
public record ClientTurnOrderTile(int numPlayers, List<Integer> foodBonus){}

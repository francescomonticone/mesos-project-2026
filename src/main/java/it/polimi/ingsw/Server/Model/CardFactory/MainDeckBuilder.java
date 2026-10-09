package it.polimi.ingsw.Server.Model.CardFactory;

import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.Deck;
import it.polimi.ingsw.Server.Model.Match.Era;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class responsible for assembling the main deck according to the official Mesos rules.
 */
public class MainDeckBuilder {

    /**
     * Builds the main game deck by combining Characters and Events.
     * It filters cards by the minimum player count, groups them by Era,
     * shuffles each Era separately, and stacks them (Era I is on top).
     *
     * @param allCharacters the complete collection of Character cards
     * @param allNonFinalEvents     the complete collection of Event cards
     * @param finalEvents   the complete collection of Final Event cards
     * @param numPlayers    the exact number of players in the match
     * @return the fully prepared Deck of Cards
     */
    public static Deck<Card> build(List<? extends Card> allCharacters, List<? extends Card> allNonFinalEvents, List<? extends Card> finalEvents, int numPlayers) {
        List<Card> combinedCards = new ArrayList<>();

        //Filter both types of cards based on the match's player count
        allCharacters.stream()
                .filter(c -> c.getMinPlayers() <= numPlayers)
                .forEach(combinedCards::add); //add filtrated characters

        allNonFinalEvents.stream()
                .filter(e -> e.getMinPlayers() <= numPlayers)
                .forEach(combinedCards::add);

        //Group the filtered cards by Era
        Map<Era, List<Card>> cardsByEra = combinedCards.stream()
                                            .collect(Collectors.groupingBy(Card::getEra)); // e.g.   ERA_1 → [Card1, Card3], ERA_2 → [Card2]

        //Shuffle each Era deck separately
        for (List<Card> eraDeck : cardsByEra.values()) {
            Collections.shuffle(eraDeck);
        }

        //Create an empty deck and stack the Eras using addCardsToTop
        Deck<Card> mainDeck = new Deck<>();

        List<Card> orderedDeck = new ArrayList<>(); //this is the deck order and will be reverted in deck class (last card is top)

        if (cardsByEra.containsKey(Era.I)) {
            orderedDeck.addAll(cardsByEra.get(Era.I));
        }
        if (cardsByEra.containsKey(Era.II)) {
            orderedDeck.addAll(cardsByEra.get(Era.II));
        }
        if (cardsByEra.containsKey(Era.III)) {
            orderedDeck.addAll(cardsByEra.get(Era.III));
        }

        orderedDeck.addAll(finalEvents); //final events are added at the end of the deck

        mainDeck.addCardsToTop(orderedDeck);

        return mainDeck;
    }
}
package it.polimi.ingsw.Server.Model.Cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic Deck representing a stack of cards.
 * <p>
 * The "Top" of the deck is considered the LAST element of the list (index size - 1).
 * This allows for highly efficient O(1) card drawing without shifting the array.
 * </p>
 */
public class Deck<T> {

    private List<T> cards;

    public Deck(List<T> cards) {
        // Defensive copy: prevents external lists from modifying the deck directly
        this.cards = new ArrayList<>(cards);
    }
    public Deck() {
        this.cards = new ArrayList<>();
    }

    /**
     * Returns a read-only view of the remaining cards.
     * Useful for inspection without allowing accidental modification.
     */
    public List<T> getCards() {
        return Collections.unmodifiableList(cards);
    }

    /**
     * Appends a list of cards to the TOP of the deck.
     * The list is reversed before being added, ensuring that the FIRST card
     * of the input list becomes the absolute TOP of the deck (drawn first).
     *
     * @param newCards the list of cards to place on top
     */
    public void addCardsToTop(List<T> newCards) {
        if (newCards == null || newCards.isEmpty()) {
            return;
        }
        List<T> cardsToAdd = new ArrayList<>(newCards); //make a defensive copy

        Collections.reverse(cardsToAdd); //reverse collection so the last card is the top of the deck

        this.cards.addAll(cardsToAdd);
    }

    /**
     * Draws the top card from the deck (which is the last element of the list).
     *
     * @return the drawn card, or null if the deck is empty
     */
    public T draw() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.removeLast();
    }

    /**
     * Draws the next N cards from the top of the deck.
     * The cards are returned in the exact order they were drawn
     * (the absolute top card will be at index 0 of the returned list).
     *
     * @param n the number of cards to draw
     * @return a list of drawn cards
     * @throws IllegalArgumentException if n is negative
     */
    public List<T> drawNextNCard(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Cannot draw a negative number of cards");
        }

        // Ensure we don't try to draw more cards than we have
        int amountToDraw = Math.min(n, cards.size());

        if (amountToDraw == 0) {
            return new ArrayList<>();
        }

        // Calculate the starting index for the slice (top of deck is at the end)
        int startIndex = cards.size() - amountToDraw;

        // Extract the sublist of cards
        List<T> drawnCards = new ArrayList<>(cards.subList(startIndex, cards.size()));

        // Remove the drawn slice from the actual deck in a single operation
        cards.subList(startIndex, cards.size()).clear();

        // Reverse the drawn list so the very first card drawn is at index 0.
        // E.g., if Deck is [A, B, C], drawing 2 gives [C, B]
        Collections.reverse(drawnCards);

        return drawnCards;
    }

    /**
     * Draws and removes ALL remaining cards from the deck.
     *
     * @return a list of all drawn cards in drawing order (top to bottom)
     */
    public List<T> drawAll() {
        // We elegantly reuse the consistent logic of drawNextNCard
        // to properly empty the deck and maintain top-to-bottom order.
        return drawNextNCard(cards.size());
    }

    /**
     * Checks if the deck is empty.
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
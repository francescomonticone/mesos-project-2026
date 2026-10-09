package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect;


/**
 * Base interface for all building effects on the client side.
 * Defines common attributes and the entry point for the Visitor pattern.
 *
 * @author Gabriele Maiolo
 */
public interface ClientBuildingEffect {
    String type();

    /**
     * Entry point for the Visitor pattern.
     * Accepts a visitor for polymorphic processing of card data.
     *
     * @param visitor The effect visitor implementation.
     * @return The result of the visit operation.
     */
    <T> T accept(ClientEffectVisitor<T> visitor);
}

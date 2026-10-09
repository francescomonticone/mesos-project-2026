package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect;

/**
 * Visitor interface for building effects.
 * Defines visit methods for every concrete implementation of {@link ClientBuildingEffect}.
 *
 * @param <T> The return type of the visit methods (e.g., List of Strings for rendering).
 */
public interface ClientEffectVisitor<T> {
    T visit(ClientFoodPerCompleteSet effect);
    T visit(ClientSustenanceGatherersDiscount effect);
    T visit(ClientSustenanceArtistDiscount effect);
    T visit(ClientShamanicShield effect);
    T visit(ClientExtraFoodTurnEnd effect);
    T visit(ClientSameInventorPairBonus effect);
    T visit(ClientDoubleShamanicPoints effect);
    T visit(ClientExtraShamanicStar effect);
    T visit(ClientSustenanceInventorsDiscount effect);
    T visit(ClientHuntEventBonus effect);
    T visit(ClientCavePaintingBonus effect);
    T visit(ClientPointsPerCompleteSet effect);
    T visit(ClientExtraPointPerHunter effect);
    T visit(ClientExtraPointPerGatherer effect);
    T visit(ClientExtraPointPerShaman effect);
    T visit(ClientExtraPointPerBuilder effect);
    T visit(ClientExtraPointPerArtist effect);
    T visit(ClientExtraPointPerInventor effect);
    T visit(ClientDoubleBuilderPoints effect);
    T visit(ClientExtraCardPick effect);
    T visit(ClientBonusPoints effect);
}

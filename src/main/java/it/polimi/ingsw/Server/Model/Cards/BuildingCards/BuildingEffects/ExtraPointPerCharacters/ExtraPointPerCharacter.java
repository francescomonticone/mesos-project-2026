package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BuildingEffect;
import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Abstract building effect that computes end-game bonus points based on the
 * number of specific character cards in the tribe.
 */
public abstract class ExtraPointPerCharacter extends BuildingEffect {
    public abstract int onEndOfGame(Tribe tribe);
}

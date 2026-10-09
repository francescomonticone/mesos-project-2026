package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BuildingEffect;
import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Base class for building effects that apply a sustenance discount during
 * the sustenance event.
 */
public abstract class SustenanceDiscount extends BuildingEffect {
    @Override
    public abstract int onSustenanceEvent(Tribe tribe);
}

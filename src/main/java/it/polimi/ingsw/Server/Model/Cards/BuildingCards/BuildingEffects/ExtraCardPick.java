package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

/**
 * Building effect that grants extra card picks from the upper and lower rows.
 */
public class ExtraCardPick extends BuildingEffect{
    private final int extraUpCardPick;
    private final int extraDownCardPick;

    /**
     * Creates an extra card pick effect.
     *
     * @param extraUpCardPick the number of extra picks from the upper row
     * @param extraDownCardPick the number of extra picks from the lower row
     */
    public ExtraCardPick(int extraUpCardPick, int extraDownCardPick) {
        this.extraUpCardPick = extraUpCardPick;
        this.extraDownCardPick = extraDownCardPick;
    }
    /**
     * Returns the total number of extra card picks granted by this effect.
     *
     * @return the sum of upper-row and lower-row extra picks
     */
    @Override
    public int howManyExtraCardPicks() {
        return extraUpCardPick +  extraDownCardPick;
    }

    /**
     * Returns the number of extra picks granted from the upper row.
     *
     * @return the number of extra upper-row picks
     */
    @Override
    public int getExtraUpCardPicks() {
        return extraUpCardPick;
    }

    /**
     * Returns the number of extra picks granted from the lower row.
     *
     * @return the number of extra lower-row picks
     */
    @Override
    public int getExtraDownCardPicks() {
        return extraDownCardPick;
    }
}
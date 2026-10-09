package it.polimi.ingsw.Server.Model.Match;

public enum Era {
    I(1),
    II(2),
    III(3);

    private final int value;

    Era(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }

    /**
     * Determines if the transition into this Era requires discarding
     * the building cards currently residing in the lower row of the board.
     * <p>
     * According to standard Mesos rules, this happens exclusively at the
     * start of Era III. The logic ({@code value >= 3}) is designed to be
     * forward-compatible with potential future expansions (e.g., Era IV).
     * </p>
     *
     * @return {@code true} if the lower row buildings must be cleared, {@code false} otherwise
     */
    public boolean requiresLowerBuildingDiscard() {
        return this.value >= 3;
    }
}

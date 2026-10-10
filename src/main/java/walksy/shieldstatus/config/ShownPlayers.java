package walksy.shieldstatus.config;

public enum ShownPlayers {
    ALL,
    SELF_ONLY,
    OTHER_PLAYERS_ONLY;

    public ShownPlayers next() {
        final ShownPlayers[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}

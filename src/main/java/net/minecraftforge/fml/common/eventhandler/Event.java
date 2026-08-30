package net.minecraftforge.fml.common.eventhandler;

public class Event {
    public static final boolean LIQUIDBOUNCE_MCP_STUB = true;

    public static enum Result {
        DENY,
        DEFAULT,
        ALLOW;

        public static final boolean LIQUIDBOUNCE_MCP_STUB = true;
    }

    private Result result = Result.DEFAULT;

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result value) {
        this.result = value;
    }

    public boolean isCancelable() {
        return false;
    }

    public boolean isCanceled() {
        return false;
    }

    public void setCanceled(boolean cancel) {
    }
}

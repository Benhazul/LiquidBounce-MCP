package net.minecraftforge.fml.common.eventhandler;

public class Event {
    public static enum Result {
        DENY,
        DEFAULT,
        ALLOW
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

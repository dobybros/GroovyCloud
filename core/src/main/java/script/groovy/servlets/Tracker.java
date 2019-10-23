package script.groovy.servlets;

public class Tracker {
    public static ThreadLocal<Tracker> trackerThreadLocal = new ThreadLocal<>();

    private String trackId;
    private String parentTrackId;

    public Tracker(String trackId, String parentTrackId) {
        this.trackId = trackId;
        this.parentTrackId = parentTrackId;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getParentTrackId() {
        return parentTrackId;
    }

    public void setParentTrackId(String parentTrackId) {
        this.parentTrackId = parentTrackId;
    }
}

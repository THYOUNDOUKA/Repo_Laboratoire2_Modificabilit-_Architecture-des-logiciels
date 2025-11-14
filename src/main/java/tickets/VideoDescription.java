package tickets;

public class VideoDescription implements TicketDescription {
    private final String pathOrUrl;

    public VideoDescription(String pathOrUrl) { this.pathOrUrl = pathOrUrl; }

    @Override
    public void execute() { System.out.println("[VideoDescription] " + pathOrUrl); }

    public String getPathOrUrl() { return pathOrUrl; }
}

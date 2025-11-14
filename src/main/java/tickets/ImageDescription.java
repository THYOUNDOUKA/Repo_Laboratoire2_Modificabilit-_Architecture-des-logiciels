package tickets;

public class ImageDescription implements TicketDescription {
    private final String pathOrUrl;

    public ImageDescription(String pathOrUrl) { this.pathOrUrl = pathOrUrl; }

    @Override
    public void execute() { System.out.println("[ImageDescription] " + pathOrUrl); }

    public String getPathOrUrl() { return pathOrUrl; }
}

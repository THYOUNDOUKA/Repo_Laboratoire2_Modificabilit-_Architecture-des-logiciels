package tickets;

public class TextDescription implements TicketDescription {
    private final String text;

    public TextDescription(String text) { this.text = text; }


    @Override
    public void execute() { System.out.println("[TextDescription] " + text); }

    // Getter uniquement pour que la couche de "fabrication pure" puisse lire la donnée
    public String getText() { return text; }
}

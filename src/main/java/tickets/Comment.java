package tickets;




import java.util.Date;
import java.util.Objects;

public class Comment {

    private final User author;
    private final TicketDescription content;
    private final Date timestamp;

    public Comment(User author, TicketDescription content) {
        this.author = Objects.requireNonNull(author, "author null");
        this.content = Objects.requireNonNull(content, "content null");
        this.timestamp = new Date();
    }

    public User getAuthor() {
        return author;
    }

    public TicketDescription getContent() {
        return content;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public void show() {
        System.out.println("[" + timestamp + "] " + author.getName() + " a commenté :");
        content.execute();
    }


}
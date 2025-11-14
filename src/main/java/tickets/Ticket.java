package tickets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class Ticket {

    // --- Attributs (diagramme)
    private final int ticketID;
    private String title;
    private TicketDescription description;       // description principale (texte, image, vidéo…)
    private TicketStatus status;                 // OUVERT, ASSIGNE, VALIDATION, TERMINE
    private Priority priority;                   // BASSE, MOYENNE, HAUTE, URGENTE
    private final Date creationDate;
    private Date updateDate;

    private User creator;
    private Developer assignee;


    private final List<Comment> comments = new ArrayList<>();

    // --- Constructeur principal
    public Ticket(int ticketID, String title, User creator, Priority priority) {
        if (ticketID <= 0) throw new IllegalArgumentException("ticketID doit être > 0");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title obligatoire");
        this.ticketID = ticketID;
        this.title = title.trim();
        this.creator = Objects.requireNonNull(creator, "creator null");
        this.priority = Objects.requireNonNull(priority, "priority null");
        this.status = TicketStatus.OUVERT;   // comme l’énoncé le dit
        this.creationDate = new Date();
        this.updateDate = new Date();
    }

    // --- Getters
    public int getTicketID() { return ticketID; }
    public String getTitle() { return title; }
    public TicketDescription getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public Date getCreationDate() { return new Date(creationDate.getTime()); }
    public Date getUpdateDate() { return new Date(updateDate.getTime()); }
    public User getCreator() { return creator; }
    public Developer getAssignee() { return assignee; }

    public List<Comment> getComments() {
        return comments;
    }

    /** assignTo(user: User) ; on exige à l’exécution que ce soit un Developer */
    public void assignTo(User user) {
        Objects.requireNonNull(user, "user null");
        if (!(user instanceof Developer)) {
            throw new IllegalArgumentException("assignTo exige un Developer");
        }
        this.assignee = (Developer) user;

        if (this.status == TicketStatus.OUVERT) {
            this.status = TicketStatus.ASSIGNE;
        }
        touch();
    }

    /** updateStatus(status: enum) – vérifie le flux OUVERT→ASSIGNE→VALIDATION→TERMINE */
    public void updateStatus(TicketStatus newStatus) {
        Objects.requireNonNull(newStatus, "status null");


        switch (this.status) {
            case OUVERT -> {
                if (newStatus != TicketStatus.ASSIGNE && newStatus != TicketStatus.TERMINE)
                    throw new IllegalStateException("Depuis OUVERT : ASSIGNE ou TERMINE (fermeture directe)");
            }

            case ASSIGNE -> {
                if (newStatus != TicketStatus.VALIDATION)
                    throw new IllegalStateException("Depuis ASSIGNE : VALIDATION uniquement");
            }
            case VALIDATION -> {
                if (newStatus != TicketStatus.TERMINE && newStatus != TicketStatus.ASSIGNE)
                    throw new IllegalStateException("Depuis VALIDATION : TERMINE ou retour ASSIGNE");
            }
            case TERMINE -> throw new IllegalStateException("Ticket déjà TERMINE (état final)");
        }

        this.status = newStatus;

        if (this.status == TicketStatus.TERMINE) {
            this.assignee = null;
        }
        touch();
    }


    public void setTicketDescription(TicketDescription description) {
        this.description = Objects.requireNonNull(description, "description null");
        touch();
    }
public void showDescription(){
    if (description == null) System.out.println("[Ticket] description vide");
    else description.execute();
}
    /** setPriority(p: Priority) */
    public void setPriority(Priority priority) {
        this.priority = Objects.requireNonNull(priority, "priority null");
        touch();
    }

    /** addComment(comment: TicketDescription) */
    public void addComment(User author, TicketDescription content) {
        Objects.requireNonNull(author, "author null");
        Objects.requireNonNull(content, "content null");
        Comment newComment = new Comment(author, content);
        comments.add(newComment);
        touch();
        System.out.println( author.getName() + " a ajouté un commentaire au ticket #" + ticketID);
    }

    // --- utilitaire interne
    private void touch() { this.updateDate = new Date(); }

    // (Optionnel) setTitle si tu veux pouvoir renommer un ticket
    public void setTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title obligatoire");
        this.title = title.trim();
        touch();
    }


}

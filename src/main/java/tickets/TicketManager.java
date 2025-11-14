package tickets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class TicketManager {

    private  List<User> users;
    private  List<Ticket> tickets;
    /** Acteur courant (ex. admin ou user connecté) */
    private  User currentUser;

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public TicketManager(List<User> users, List<Ticket> tickets, User currentUser) {
        this.users = new ArrayList<>(Objects.requireNonNull(users));
        this.tickets = new ArrayList<>(Objects.requireNonNull(tickets));
        this.currentUser = Objects.requireNonNull(currentUser);
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }
    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }
    public User getCurrentUser() { return currentUser; }

    /** Création avec validations basiques (ID unique, titre non vide). */
    public void createTicket(Ticket ticket) {
        Objects.requireNonNull(ticket, "ticket null");

        boolean exists = tickets.stream()
                .anyMatch(t -> t.getTicketID() == ticket.getTicketID());
        if (exists) {
            System.out.println("Erreur: ticket ID " + ticket.getTicketID() + " existe déjà");
            return;
        }
        tickets.add(ticket);
        System.out.println("Ticket " + ticket.getTicketID() + " créé par " +
                (ticket.getCreator() != null ? ticket.getCreator().getName() : "(inconnu)") +
                " — enregistré par " + currentUser.getName());
    }

    /** Lecture avec usage de la stratégie. */
    public void viewTicket(Ticket ticket) {
        if (ticket == null) {
            System.out.println("Erreur: Ticket introuvable");
            return;
        }
        System.out.println("Détails du Ticket #" + ticket.getTicketID());
        System.out.println("Titre: " + ticket.getTitle());
        System.out.println("Statut: " + ticket.getStatus());
        System.out.println("Priorité: " + ticket.getPriority());
        System.out.println("Créateur: " + (ticket.getCreator() == null ? "(?)" : ticket.getCreator().getName()));
        System.out.println("Assigné: " + (ticket.getAssignee() == null ? "aucun" : ticket.getAssignee().getName()));
        System.out.println("Création: " + ticket.getCreationDate());
        System.out.println("MAJ: " + ticket.getUpdateDate());
        // Utilise la STRATEGY (au lieu d'imprimer l'objet)
        ticket.showDescription();
    }

    /** Mise à jour ciblée (ex: titre/priorité/description). */
    public void updateTicket(int ticketId, String newTitle, Priority newPriority, TicketDescription newDescription) {
        Optional<Ticket> opt = tickets.stream()
                .filter(t -> t.getTicketID() == ticketId)
                .findFirst();

        if (opt.isEmpty()) {
            System.out.println("Erreur: ticket " + ticketId + " introuvable");
            return;
        }
        Ticket t = opt.get();

        if (newTitle != null && !newTitle.isBlank()) t.setTitle(newTitle);
        if (newPriority != null) t.setPriority(newPriority);
        if (newDescription != null) t.setTicketDescription(newDescription);

        System.out.println("Ticket " + t.getTicketID() + " mis à jour par " + currentUser.getName());
    }

    /** Assignation — l'acteur courant réalise l'opération; on passe la cible (dev) séparément. */
    public void assignTicket(Ticket ticket, Developer developer) {
        // Vérifie que l'utilisateur courant (currentUser) est un DEV
        if (!(currentUser instanceof Developer)) {
            System.out.println("Erreur : Seuls les développeurs peuvent assigner un ticket.");
            return;
        }

        if (ticket == null || developer == null) {
            System.out.println("Erreur : ticket ou développeur invalide (null).");
            return;
        }

        try {
            ticket.assignTo(developer);
            System.out.println(currentUser.getName() + " (développeur) a assigné le ticket " +
                    ticket.getTicketID() + " à " + developer.getName());
        } catch (Exception e) {
            System.out.println("Erreur lors de l’assignation : " + e.getMessage());
        }
    }
    /** Fermer (terminer) en respectant les transitions de Ticket.updateStatus. */
    public void closeTicket(Ticket ticket) {
        if (!(currentUser instanceof Developer)) {
            System.out.println("Erreur : Seuls les développeurs peuvent fermer un ticket.");
            return;
        }

        if (ticket == null) {
            System.out.println("Erreur : ticket invalide (null).");
            return;
        }

        try {
            switch (ticket.getStatus()) {
                case OUVERT -> {
                    ticket.updateStatus(TicketStatus.ASSIGNE);
                    ticket.updateStatus(TicketStatus.VALIDATION);
                    ticket.updateStatus(TicketStatus.TERMINE);
                }
                case ASSIGNE -> {
                    ticket.updateStatus(TicketStatus.VALIDATION);
                    ticket.updateStatus(TicketStatus.TERMINE);
                }
                case VALIDATION -> ticket.updateStatus(TicketStatus.TERMINE);
                case TERMINE -> {
                    System.out.println("Le ticket " + ticket.getTicketID() + " est déjà terminé.");
                    return;
                }
            }
            System.out.println(currentUser.getName() + " (développeur) a fermé le ticket " + ticket.getTicketID());
        } catch (Exception e) {
            System.out.println("Erreur lors de la fermeture du ticket: " + e.getMessage());
        }
    }
    /** Liste interne, pas de paramètre inutile. */
    public List<Ticket> viewAllTickets() {
        if (tickets.isEmpty()) {
            System.out.println("Aucun ticket à afficher");
            return List.of();
        }
        tickets.forEach(t -> System.out.println(
                "Ticket " + t.getTicketID() + " - " + t.getTitle() +
                        " [" + t.getStatus() + "] - Priorité: " + t.getPriority()
        ));
        return Collections.unmodifiableList(tickets);
    }
    public void exportTicket(Ticket ticket, ExportStrategy strategy, OutputStream out) {
        if (ticket == null) { System.out.println("Erreur: ticket null"); return; }
        if (strategy == null) { System.out.println("Erreur: strategy null"); return; }
        if (out == null) { System.out.println("Erreur: OutputStream null"); return; }
        try {
            strategy.export(ticket, out);
        } catch (IOException e) {
            System.out.println("Erreur export: " + e.getMessage());
        }
    }
    public void addUser(User u) {
        if (u == null) throw new IllegalArgumentException("Utilisateur null");
        users.add(u);
    }
}

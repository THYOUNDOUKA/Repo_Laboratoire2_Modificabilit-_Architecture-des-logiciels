package com.mycompany.tickets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Admin {
    private int adminID;
    private String name;
    private String email;
public Admin() {

}
    public Admin(int adminID, String name, String email) {
        this.adminID = adminID;
        this.name = name;
        this.email = email;
    }

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Assigne un ticket à un utilisateur.
     *
     * Si le ticket est valide, il est assigné au  User
     * spécifié et un message est affiché en console.
     *
     * @param ticket ticket à assigner
     * @param user   utilisateur qui reçoit le ticket
     */

    public void assignTicket(Ticket ticket, User user) {
        Optional.ofNullable(ticket)
                .ifPresentOrElse(
                        t -> {
                            try {
                                t.assignTo(user);
                                System.out.println("Admin " + this.name + " a assigné le ticket " +
                                        t.getTicketID() + " à l'utilisateur " + user.getName());
                            } catch (Exception e) {
                                System.out.println("Erreur lors de l'assignation : " + e.getMessage());
                            }
                        },
                        () -> System.out.println("Erreur: ticket invalide (null)")
                );
    }
    /**
     * Ferme un ticket.
     *
     * Si le ticket est valide, son statut est mis à jour à {@code "FERMER"}.
     *
     * @param ticket ticket à fermer
     */

    public void closeTicket(Ticket ticket) {
        Optional.ofNullable(ticket)
                .ifPresentOrElse(
                        t -> {
                            try {
                                t.updateStatus("FERMER"); // utilise la logique déjà codée dans Ticket
                                System.out.println("Admin " + this.name + " a fermé le ticket " + t.getTicketID());
                            } catch (Exception e) {
                                System.out.println("Erreur lors de la fermeture du ticket: " + e.getMessage());
                            }
                        },
                        () -> System.out.println("Erreur: ticket invalide (null)")
                );
    }

    /**
     * Affiche en console la liste de tous les tickets existants.
     *
     *
     * @param allTickets liste de tous les tickets
     * @return copie de la liste de tickets passée en paramètre
     */
    public List<Ticket> viewAllTickets(List<Ticket> allTickets) {
        if (allTickets == null || allTickets.isEmpty()) {
            System.out.println("Aucun ticket à afficher");
            return new ArrayList<>();
        }

        allTickets.forEach(t -> {
            System.out.println("Ticket " + t.getTicketID() + " - " + t.getTitle()
                    + " [" + t.getStatus() + "] - Priorité: " + t.getPriority());
        });

        return new ArrayList<>(allTickets);
    }

    // toString
    @Override
    public String toString() {
        return "Admin{" +
                "adminID=" + adminID +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // equals & hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Admin)) return false;
        Admin admin = (Admin) obj;
        return adminID == admin.adminID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminID);
    }
}

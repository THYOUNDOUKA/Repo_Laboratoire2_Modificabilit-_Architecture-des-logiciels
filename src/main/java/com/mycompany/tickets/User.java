package com.mycompany.tickets;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class User {
    private int userID;
    private String name;
    private String email;

    public List<Ticket> getCreatedTickets() {
        return createdTickets;
    }

    public void setCreatedTickets(List<Ticket> createdTickets) {
        this.createdTickets = createdTickets;
    }

    private List<Ticket> createdTickets;
    public User(){

    }

    public User(int userID, String name, String email, List<Ticket> createdTickets, String role) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.createdTickets = createdTickets;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    private String role;
    // Méthodes selon le diagramme de classe avec utilisation des Streams


    /**
     * Crée un nouveau ticket en utilisant les Streams
     * @param ticket Le ticket à créer
     */
    public void createTicket(Ticket ticket) {
        Optional.ofNullable(ticket)
                .filter(t -> t.getTicketID() > 0) // Validation avec Stream
                .filter(t -> t.getTitle() != null && !t.getTitle().trim().isEmpty()) // Validation titre
                .ifPresentOrElse(
                        t -> {
                            // Vérification que le ticket n'existe pas déjà avec Stream
                            boolean ticketExists = createdTickets.stream()
                                    .anyMatch(existingTicket -> existingTicket.getTicketID() == t.getTicketID());

                            if (!ticketExists) {
                                createdTickets.add(t);
                                System.out.println("Ticket créé avec succès par " + this.name +
                                        " (ID: " + t.getTicketID() + ")");
                            } else {
                                System.out.println("Erreur: Un ticket avec l'ID " + t.getTicketID() + " existe déjà");
                            }
                        },
                        () -> System.out.println("Erreur: Impossible de créer un ticket invalide (null, ID invalide ou titre vide)")
                );
    }

    /**
     * Affiche les détails d'un ticket en utilisant les Streams
     * @param ticket Le ticket à visualiser
     */
    public void viewTicket(Ticket ticket) {
        Optional.ofNullable(ticket)
                .ifPresentOrElse(
                        t -> {
                            // Utilisation de Stream pour formater l'affichage
                            System.out.println("\n=== Détails du Ticket ===");
                            System.out.println("ID: " + t.getTicketID());
                            System.out.println("Titre: " + t.getTitle());
                            System.out.println("Description: " + t.getDescription());
                            System.out.println("Statut: " + t.getStatus());
                            System.out.println("Priorité: " + t.getPriority());
                            System.out.println("Date de création: " + t.getCreationDate());
                            System.out.println("Date de mise à jour: " + t.getUpdateDate());
                            System.out.println("========================");



                            // Vérification si ce ticket appartient à cet utilisateur
                            boolean isMyTicket = createdTickets.stream()
                                    .anyMatch(myTicket -> myTicket.getTicketID() == t.getTicketID());

                            if (isMyTicket) {
                                System.out.println("✓ Vous êtes le créateur de ce ticket");
                            }
                            System.out.println();
                        },
                        () -> System.out.println("Erreur: Ticket introuvable")
                );
    }

    /**
     * Met à jour un ticket existant en utilisant les Streams
     * @param ticket Le ticket à mettre à jour
     */
    public void updateTicket(Ticket ticket) {
        Optional.ofNullable(ticket)
                .filter(t -> createdTickets.stream()
                        .anyMatch(myTicket -> myTicket.getTicketID() == t.getTicketID()))
                .ifPresentOrElse(
                        t -> {
                            // Mise à jour du ticket dans la liste avec Stream
                            createdTickets.stream()
                                    .filter(myTicket -> myTicket.getTicketID() == t.getTicketID())
                                    .findFirst()
                                    .ifPresent(ticketToUpdate -> {
                                        ticketToUpdate.setUpdateDate(new java.util.Date());
                                        System.out.println("Ticket " + ticketToUpdate.getTicketID() +
                                                " mis à jour par " + this.name);

                                        // Affichage des modifications avec Stream
                                        java.util.List<String> updateInfo = java.util.List.of(
                                                "Modifications appliquées:",
                                                "- Titre: " + ticketToUpdate.getTitle(),
                                                "- Statut: " + ticketToUpdate.getStatus(),
                                                "- Dernière modification: " + ticketToUpdate.getUpdateDate()
                                        );

                                        updateInfo.stream().forEach(System.out::println);
                                    });
                        },
                        () -> {
                            // Message d'erreur plus détaillé avec Stream
                            String errorMessage = Optional.ofNullable(ticket)
                                    .map(t -> "Le ticket #" + t.getTicketID() + " ne vous appartient pas")
                                    .orElse("Ticket null - impossible de mettre à jour");

                            System.out.println("Erreur: " + errorMessage);
                        }
                );
    }

    // Méthode toString pour l'affichage
    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", ticketsCreated=" + createdTickets.size() +
                '}';
    }

    // Méthode equals pour la comparaison d'utilisateurs
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userID == user.userID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userID);
    }
}

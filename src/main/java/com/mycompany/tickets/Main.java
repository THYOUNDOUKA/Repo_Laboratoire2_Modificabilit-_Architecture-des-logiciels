package com.mycompany.tickets;// doit matcher le dossier

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        List<Ticket> allTickets = new ArrayList<>();
        List<Ticket> user1Tickets = new ArrayList<>();
        List<Ticket> user2Tickets = new ArrayList<>();

        // --- Création des utilisateurs ---
        User user1 = new User(1, "Eliel", "eliel@mail.com", user1Tickets, "Employé");
        User user2 = new User(2, "Maelle", "maelle@mail.com", user2Tickets, "Technicien");

        // --- Création de l’admin ---
        Admin admin = new Admin(100, "Mickael", "Mickael.admin@mail.com");

        // --- Création de tickets ---
        Ticket t1 = new Ticket(101, "Problème de connexion",
                "Impossible de se connecter au réseau",
                "OUVERT", "MOYENNE", new Date(), null);

        Ticket t2 = new Ticket(102, "Erreur application",
                "Crash lors du lancement",
                "OUVERT", "URGENTE", new Date(), null);

        // --- User crée ses tickets ---
        user1.createTicket(t1);
        user2.createTicket(t2);

        // Ajout à la liste globale
        allTickets.add(t1);
        allTickets.add(t2);

        // --- Affichage des tickets créés ---
        System.out.println("--- Tickets créés ---");
        user1.viewTicket(t1);
        user2.viewTicket(t2);

        // --- Admin assigne un ticket ---
        System.out.println("--- Assignation par Admin ---");
        admin.assignTicket(t1, user2);

        // --- User travaille sur le ticket ---
        t1.addComment("Analyse du problème en cours...");
        t1.updateStatus("VALIDATION");

        // --- Admin ferme un ticket ---
        System.out.println("--- Fermeture du ticket ---");
        admin.closeTicket(t2);

        // --- Admin visualise tous les tickets ---
        System.out.println("--- Vue globale Admin ---");
        admin.viewAllTickets(allTickets);

        // --- Résumé des objets ---
        System.out.println("Résumé des objets :");
        System.out.println(user1);
        System.out.println(user2);
        System.out.println(admin);
    }
}
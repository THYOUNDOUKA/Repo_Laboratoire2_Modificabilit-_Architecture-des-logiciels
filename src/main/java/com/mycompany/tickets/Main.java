package com.mycompany.tickets;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // --- Utilisateurs
        User alice = new User(1, "Alice", "alice@uqac.ca");        // non-dev
        Developer bob = new Developer(2, "Bob", "bob@uqac.ca");    // dev
        Developer chris = new Developer(3, "Chris", "chris@uqac.ca");

        List<User> users = List.of(alice, bob, chris);
        List<Ticket> tickets = new ArrayList<>();

        // === TICKET 1 : flux classique OUVERT -> ASSIGNE -> VALIDATION -> TERMINE
        Ticket t1 = new Ticket(101, "Bug: sauvegarde", alice, Priority.HAUTE);
        t1.setTicketDescription(new TextDescription("Crash quand je clique sur 'Enregistrer'"));
        t1.addComment(new ImageDescription("screenshots/save_error.png"));
        t1.addComment(new VideoDescription("videos/repro.mp4"));

        // Manager avec utilisateur courant non-dev (Alice)
        TicketManager mUser = new TicketManager(users, tickets, alice);
        mUser.createTicket(t1);

        System.out.println("\n== Vue initiale (Alice) ==");
        mUser.viewTicket(t1);

        System.out.println("\n== Tentative d'assignation par Alice (non-dev) ==");
        mUser.assignTicket(t1, bob); // doit être refusé (contrôle de rôle)

        // Manager avec utilisateur courant dev (Bob)
        TicketManager mDev = new TicketManager(users, tickets, bob);

        System.out.println("\n== Assignation par Bob (dev) ==");
        mDev.assignTicket(t1, bob); // passe en ASSIGNE

        System.out.println("\n== Fermeture via service (ASSIGNE -> VALIDATION -> TERMINE) ==");
        mDev.closeTicket(t1);
        mDev.viewTicket(t1);

        // === TICKET 2 : fermeture directe SANS assignation (OUVERT -> TERMINE)
        Ticket t2 = new Ticket(102, "Demande non prioritaire", alice, Priority.BASSE);
        t2.setTicketDescription(new TextDescription("Cas spécifique utilisateur, fermeture directe."));
        mDev.createTicket(t2);

        System.out.println("\n== Fermeture directe SANS assignation (OUVERT -> TERMINE) ==");
        try {
            // IMPORTANT : nécessite votre modification dans Ticket.updateStatus (autoriser OUVERT -> TERMINE)
            t2.updateStatus(TicketStatus.TERMINE);
            System.out.println("Fermeture directe réussie pour le ticket #" + t2.getTicketID());
        } catch (Exception e) {
            System.out.println("Échec fermeture directe: " + e.getMessage());
        }
        mDev.viewTicket(t2);

        // === Export PDF (texte formaté) via Strategy
        System.out.println("\n== Export PDF simulé (texte) ==");
        ExportStrategy exporter = new PdfExporter();
        try (FileOutputStream f1 = new FileOutputStream("ticket101_export.txt")) {
            exporter.export(t1, f1);
        }
        try (FileOutputStream f2 = new FileOutputStream("ticket102_export.txt")) {
            exporter.export(t2, f2);
        }
        System.out.println("Exports générés : ticket101_export.txt, ticket102_export.txt");

        // === Liste de tous les tickets
        System.out.println("\n== Liste des tickets ==");
        mDev.viewAllTickets();
    }
}

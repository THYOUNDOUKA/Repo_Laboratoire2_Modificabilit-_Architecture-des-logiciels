package com.mycompany.tickets;


public class Main {
    private static void printTicket(String label, Ticket t) {
        System.out.println("---- " + label + " ----");
        System.out.println("id=" + t.getTicketID()
                + ", title=" + t.getTitle()
                + ", status=" + t.getStatus()
                + ", priority=" + t.getPriority()
                + ", closed=" + t.isClosed()
                + ", assigned=" + t.getAssignedUser());
        System.out.println("updateDate=" + t.getUpdateDate());
        System.out.println();
    }

    public static void main(String[] args) {


        
        try {
            // 1) Création: statut par défaut = OUVERT, priorité = MOYENNE
            Ticket t1 = new Ticket(1, "Bug login", "Le login échoue", null, null, null, null);
            printTicket("Après création (t1)", t1);

            // 2) Transition illégale (OUVERT -> VALIDATION) => doit lever une exception
            try {
                t1.updateStatus("VALIDATION");
                System.out.println("❌ ERREUR: transition OUVERT->VALIDATION aurait dû échouer");
            } catch (Exception e) {
                System.out.println("✅ Attendu: transition illégale refusée: " + e.getMessage());
            }
            System.out.println();

            // 3) OUVERT -> ASSIGNÉ (via updateStatus, ou via assignTo)
            t1.updateStatus("ASSIGNÉ");
            printTicket("Après OUVERT -> ASSIGNÉ (t1)", t1);
             Thread.sleep(1000); // ← pause 1s

            // 4) ASSIGNÉ -> VALIDATION
            t1.updateStatus("VALIDATION");
            printTicket("Après ASSIGNÉ -> VALIDATION (t1)", t1);
             Thread.sleep(1000); // ← pause 1s

            // 5) Commentaire autorisé avant finalisation
            t1.addComment("Je reproduis le bug et propose un correctif.");
            System.out.println("✅ Commentaire ajouté avant finalisation.\n");

            // 6) VALIDATION -> TERMINÉ
            t1.updateStatus("TERMINÉ");
            printTicket("Après VALIDATION -> TERMINÉ (t1)", t1);
             Thread.sleep(1000); // ← pause 1s

            // 7) Commentaire après TERMINÉ => doit échouer
            try {
                t1.addComment("Test post-terminé (ne doit pas passer)");
                System.out.println("❌ ERREUR: commentaire après TERMINÉ aurait dû échouer");
            } catch (Exception e) {
                System.out.println("✅ Attendu: commentaire refusé après TERMINÉ: " + e.getMessage());
            }
            System.out.println();

            // 8) Nouveau ticket: fermeture (action) sans assignation
            Ticket t2 = new Ticket(2, "Suggestion UI", "Changer la couleur du bouton", null, "BASSE", null, null);
            printTicket("Après création (t2)", t2);

            // On peut aussi tester assignTo avant fermeture
            User alice = new User(101, "Alice",  "alice@example.com", "DEVELOPPEUR");
            t2.assignTo(alice); // OUVERT -> ASSIGNÉ implicite
            printTicket("Après assignTo (t2)", t2);

            // Action "FERMER" (ce n'est PAS un statut) -> closed=true, le ticket est gelé
            t2.updateStatus("FERMER");
            printTicket("Après action FERMER (t2)", t2);

            // 9) Toute modif/assignation après fermeture doit échouer
            try {
                t2.setTitle("Nouveau titre (devrait échouer)");
                System.out.println("❌ ERREUR: modification après fermeture aurait dû échouer");
            } catch (Exception e) {
                System.out.println("✅ Attendu: modif refusée après fermeture: " + e.getMessage());
            }

            try {
                t2.assignTo(new User(102, "Bob", "Bobi22@gmail.com","Developpeur"));
                System.out.println("❌ ERREUR: assignation après fermeture aurait dû échouer");
            } catch (Exception e) {
                System.out.println("✅ Attendu: assignation refusée après fermeture: " + e.getMessage());
            }

        } catch (Exception fatal) {
            System.out.println("FATAL: " + fatal);
        }
    }
}

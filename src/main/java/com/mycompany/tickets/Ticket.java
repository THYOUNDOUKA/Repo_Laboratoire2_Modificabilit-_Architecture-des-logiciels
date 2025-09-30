package com.mycompany.tickets;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Ticket {
    private final int ticketID;
    private String title;
    private String description;
    private String status;       // String imposé par le prof
    private String priority;     // String imposé par le prof
    private final Date creationDate;



    private Date updateDate;

    // --- Étape 2: états/fermeture sans ajouter de méthode publique ---
    private boolean closed = false;      // action de fermeture (pas un statut)
    private User assignedUser;           // destinataire actuel (optionnel)
    private final List<String> comments = new ArrayList<>();

    // Jeux de valeurs autorisées (en String)
    private static final Set<String> ALLOWED_STATUS_NORM = Set.of(
            "OUVERT", "ASSIGNE", "VALIDATION", "TERMINE"
    );
    private static final Set<String> ALLOWED_PRIORITY_NORM = Set.of(
            "BASSE", "MOYENNE", "HAUTE", "URGENTE"
    );
    // Mot-clé d'action de fermeture (pas un statut)
    private static final Set<String> CLOSE_ACTIONS_NORM = Set.of(
            "FERMER"
    );

    // --- Helpers de normalisation/canonisation (privés) ---
    private static String normalizeKey(String s) {
        String base = (s == null) ? "" : s.trim().toUpperCase(Locale.ROOT);
        // retire les accents
        return Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }
    private static String canonicalStatus(String s) {
        String n = normalizeKey(s);
        switch (n) {
            case "OUVERT": return "OUVERT";
            case "ASSIGNE": return "ASSIGNÉ";
            case "VALIDATION": return "VALIDATION";
            case "TERMINE": return "TERMINÉ";
            default: throw new IllegalArgumentException("status invalide: " + s);
        }
    }
    private static String canonicalPriority(String s) {
        String n = normalizeKey(s);
        switch (n) {
            case "BASSE": return "BASSE";
            case "MOYENNE": return "MOYENNE";
            case "HAUTE": return "HAUTE";
            case "URGENTE": return "URGENTE";
            default: throw new IllegalArgumentException("priority invalide: " + s);
        }
    }

    public Ticket(int ticketID, String title, String description, String status, String priority, Date creationDate, Date updateDate) {
        // Validation stricte sur l'identifiant et le titre
        if (ticketID <= 0) {
            throw new IllegalArgumentException("ticketID doit être > 0"); // Envoie une exception en bloquant la création d'un ticket nul ou négatif.
        }
        String t = (title == null) ? "" : title.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("title obligatoire");
        }

        //Création de l'object ticket
        this.ticketID = ticketID;
        this.title = t;
        this.description = (description == null) ? "" : description.trim();

        // Statut (4 valeurs) et priorité avec défauts + canonisation
        this.status   = (status == null || status.trim().isEmpty())
                ? "OUVERT"
                : canonicalStatus(status.trim()); // Le ticket commence toujours avec le statut ouvert .
        this.priority = (priority == null || priority.trim().isEmpty())
                ? "MOYENNE"
                : canonicalPriority(priority.trim());// on choit que le ticket commence avec une priorité moyenne.

        // Dates protégées et jamais nulles (le ticket aura toujours une date de création)
        Date created = (creationDate == null) ? new Date() : new Date(creationDate.getTime());
        this.creationDate = created;

        Date updated = (updateDate == null) ? created : new Date(updateDate.getTime());
        this.updateDate = updated;
    }

    // The getters
    public int getTicketID() { return ticketID; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public Date getCreationDate() { return new Date(creationDate.getTime()); }
    public Date getUpdateDate()   { return new Date(updateDate.getTime()); }

    // (optionnels pour inspection/tests)
    public boolean isClosed() { return closed; }
    public User getAssignedUser() { return assignedUser; }
    public List<String> getComments() { return new ArrayList<>(comments); }

    //The setters
    public void setDescription(String description) {
        if (closed) throw new IllegalStateException("modification interdite: ticket clôturé");
        this.description = (description == null) ? "" : description.trim();
        touch();
    }
    public void setTitle(String title) {
        if (closed) throw new IllegalStateException("modification interdite: ticket clôturé");
        String t = (title == null) ? "" : title.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("title obligatoire");
        this.title = t;
        touch();
    }
    public void setStatus(String status) {
        if (closed) throw new IllegalStateException("transition interdite: ticket clôturé");
        if (status == null || !ALLOWED_STATUS_NORM.contains(normalizeKey(status))) {
            throw new IllegalArgumentException("status invalide");
        }
        this.status = canonicalStatus(status);
        touch();
    }
    public void setPriority(String priority) {
        if (closed) throw new IllegalStateException("modification interdite: ticket clôturé");
        if (priority == null || !ALLOWED_PRIORITY_NORM.contains(normalizeKey(priority))) {
            throw new IllegalArgumentException("priority invalide");
        }
        this.priority = canonicalPriority(priority);
        touch();
    }
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    // Les fonctions à implémenter
    void assignTo(User user){

        //Ici je dois gérer comment l'aasignation du ticket se fait:
        /*
        but :Assigner à un utilisateur a un ticket ou plusieurrs tickets afin qu'il travail dessus au besoin.
         Logique d'assigantion du ticket:
         Un ticket n'est assigné que s'il est en statut ouvert;
         -Il faut que les utilisateurs aient accès au ticket avant de l'assigner à quelqu'un d'autre;
         -C'est une fois que le ticket est assigné, quand change son statut qui passe d'ouvert à assigné.

         */
        if (user == null) {
            throw new IllegalArgumentException("assignation impossible: user null");
        }
        if (closed) {
            throw new IllegalStateException("assignation impossible: ticket clôturé");
        }
        String st = normalizeKey(this.status);
        // si le ticket est OUVERT, on le passe à ASSIGNÉ lors de l'assignation
        this.assignedUser = user;
        if ("OUVERT".equals(st)) {
            this.status = "ASSIGNÉ";
        }
        touch();
    }

    void updateStatus(String status){

         /*
          Ici je fais la gestion des status du ticket(son passage d'ouvert-Assigné-Validé-Terminé et fermé )
          le fichier doit etre ouvert avant de passer aux autres  status
          un fichier n,as pas besoin d'etre assigné avant d'etre fermé dans les conditions suivantes:
          -si le problème est jugé spécifique à l’utilisateur
          - s’il n’est pas considéré comme prioritaire.
          -une fois le ticket fermé peut-il etre réouvert?Non implémenté le fait qu'un ticket fermé ne peut-etre réouvert.Ni assigné.

             */
        if (status == null) {
            throw new IllegalArgumentException("nouveau status invalide");
        }

        // --- Action de fermeture (pas un statut) via updateStatus ---
        String nextKey = normalizeKey(status);
        if (CLOSE_ACTIONS_NORM.contains(nextKey)) {
            // on clôture le ticket SANS changer 'status'
            if (!closed) {
                closed = true;
                assignedUser = null; // libère l'assigné le cas échéant
                touch();
            }
            return; // rien d'autre à faire
        }

        // transitions normales entre les 4 statuts
        if (closed) {
            throw new IllegalStateException("transition interdite: ticket clôturé");
        }
        if (!ALLOWED_STATUS_NORM.contains(nextKey)) {
            throw new IllegalArgumentException("nouveau status invalide");
        }

        final String current = normalizeKey(this.status);

        // Transitions autorisées (sans "FERMÉ" car ce n'est pas un statut) :
        // OUVERT -> ASSIGNE
        // ASSIGNE -> VALIDATION
        // VALIDATION -> TERMINE ou ASSIGNE (retour)
        boolean ok = false;
        switch (current) {
            case "OUVERT":
                ok = "ASSIGNE".equals(nextKey);
                break;
            case "ASSIGNE":
                ok = "VALIDATION".equals(nextKey);
                break;
            case "VALIDATION":
                ok = ("TERMINE".equals(nextKey) || "ASSIGNE".equals(nextKey));
                break;
            case "TERMINE":
                ok = false; // final
                break;
        }
        if (!ok) {
            throw new IllegalStateException("transition illégale: " + this.status + " -> " + canonicalStatus(status));
        }

        // Appliquer
        this.status = canonicalStatus(status);

        // Si on termine, on peut libérer l'assigné (logique courante)
        if ("TERMINE".equals(normalizeKey(this.status))) {
            this.assignedUser = null;
        }
        touch();
    }

    void addComment(String comment){

       /* Ici après qu'on est assigné un ticket à l'utilisateur
        il travaille dessus .
        C'est ici que l'on fait la documentation de la résolution du ticket .Cette fonction doit gérer ceci:
        Description enrichie : l’utilisateur peut documenter son problème avec du texte, mais aussi des captures d’écran ou des vidéos.

Consultation et exportation : la description du ticket est visible directement dans la plateforme, et peut aussi être exportée en PDF.
        */
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("commentaire vide");
        }
        if (closed) {
            throw new IllegalStateException("ajout de commentaire interdit: ticket clôturé");
        }
        if ("TERMINE".equals(normalizeKey(this.status))) {
            throw new IllegalStateException("ajout de commentaire interdit: ticket terminé");
        }

        comments.add(comment.trim());
        touch();
    }

    // utilitaire interne
    private void touch() {
        this.updateDate = new Date();
    }
}

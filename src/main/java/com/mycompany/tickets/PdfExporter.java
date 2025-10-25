package com.mycompany.tickets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class PdfExporter implements ExportStrategy {

    @Override
    public void export(Ticket t, OutputStream out) throws IOException {
        if (t == null) {
            throw new IllegalArgumentException("Le ticket ne peut pas être nul.");
        }
        if (out == null) {
            throw new IllegalArgumentException("Le flux de sortie est nul.");
        }

        // --- Description principale
        String desc = (t.getDescription() != null)
                ? getDescriptionAsString(t.getDescription())
                : "(vide)";

        // --- Commentaires
        String comments = t.getComments().isEmpty()
                ? "(aucun)"
                : t.getComments().stream()
                .map(this::getDescriptionAsString)
                .collect(Collectors.joining("\n"));

        // --- Corps du rapport
        String body =
                "=== TICKET " + t.getTicketID() + " ===\n" +
                        "Titre: " + t.getTitle() + "\n" +
                        "Statut: " + t.getStatus() + "\n" +
                        "Priorité: " + t.getPriority() + "\n" +
                        "Créateur: " + (t.getCreator() != null ? t.getCreator().getName() : "(?)") + "\n" +
                        "Assigné: " + (t.getAssignee() != null ? t.getAssignee().getName() : "(non assigné)") + "\n" +
                        "Création: " + t.getCreationDate() + "\n" +
                        "Mise à jour: " + t.getUpdateDate() + "\n\n" +
                        "----- Description -----\n" + desc + "\n\n" +
                        "----- Commentaires -----\n" + comments + "\n";

        // --- Écriture dans le flux
        out.write(body.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Méthode utilitaire pour convertir une TicketDescription en texte.
     * (sans polluer la classe domaine)
     */
    private String getDescriptionAsString(TicketDescription description) {
        if (description instanceof TextDescription td) {
            return "TEXT: " + td.getText();
        } else if (description instanceof ImageDescription id) {
            return "IMAGE: " + id.getPathOrUrl();
        } else if (description instanceof VideoDescription vd) {
            return "VIDEO: " + vd.getPathOrUrl();
        }
        return description.toString();
    }
}

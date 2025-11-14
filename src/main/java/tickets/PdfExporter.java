package tickets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class PdfExporter implements ExportStrategy {

    @Override
    public void export(Ticket t, OutputStream out) throws IOException {
        // Description principale
        String desc = (t.getDescription() != null)
                ? t.getDescription().toString()
                : "(aucune description)";

        // Liste des commentaires
        String comments = t.getComments().isEmpty()
                ? "(aucun commentaire)"
                : t.getComments().stream()
                .map(c -> "- " + c.getAuthor().getName() + " (" + c.getTimestamp() + "): " + c.getContent().toString())
                .collect(Collectors.joining("\n"));

        // Corps du "PDF"
        String body =
                "========================" +
                        "       TICKET #" + t.getTicketID() + "\n" +
                        "========================\n" +
                        "Titre      : " + t.getTitle() + "\n" +
                        "Statut     : " + t.getStatus() + "\n" +
                        "Priorité   : " + t.getPriority() + "\n" +
                        "Créateur   : " + (t.getCreator() != null ? t.getCreator().getName() : "(?)") + "\n" +
                        "Assigné à  : " + (t.getAssignee() != null ? t.getAssignee().getName() : "(non assigné)") + "\n" +
                        "Créé le    : " + t.getCreationDate() + "\n" +
                        "Modifié le : " + t.getUpdateDate() + "\n\n" +
                        "----- Description -----\n" + desc + "\n\n" +
                        "----- Commentaires -----\n" + comments + "\n";

        // Écriture dans le flux
        out.write(body.getBytes(StandardCharsets.UTF_8));
    }
}

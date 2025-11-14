import com.mycompany.tickets.ui.TicketGUI;
import tickets.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
                // Création d'utilisateurs de test
                Developer dev1 = new Developer(1, "Alice Dupont", "alice@dev.com");
                Developer dev2 = new Developer(2, "Bob Martin", "bob@dev.com");
                User user1 = new User(3, "Charlie User", "charlie@user.com");


                List<User> users = new ArrayList<>(List.of(dev1, dev2, user1));

                // Création de tickets de test
                Ticket t1 = new Ticket(101, "Bug connexion", user1, Priority.HAUTE);
                t1.setTicketDescription(new TextDescription("L'utilisateur ne peut pas se connecter"));

                Ticket t2 = new Ticket(102, "Nouvelle fonctionnalité", dev1, Priority.MOYENNE);
                t2.setTicketDescription(new TextDescription("Ajouter export Excel"));
                t2.assignTo(dev1);

                Ticket t3 = new Ticket(103, "Erreur critique", user1, Priority.URGENTE);
                t3.setTicketDescription(new ImageDescription("/images/screenshot_error.png"));

                List<Ticket> tickets = new ArrayList<>(List.of(t1, t2, t3));


                // Création du TicketManager avec dev1 comme utilisateur courant
                TicketManager manager = new TicketManager(users, tickets, dev1);

                // Lancement de l'interface
                TicketGUI gui = new TicketGUI(manager);
                gui.setVisible(true);
        });
}

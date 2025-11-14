package com.mycompany.tickets.ui;

import tickets.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * Pure Fabrication: UI class not part of the domain model.
 * Improves look and feel and ergonomics while keeping a clear separation from business logic.
 */
public class TicketGUI extends JFrame {

    // --- Domain
    private final TicketManager ticketManager;

    // --- UI data and components
    private final JTable ticketTable;
    private final DefaultTableModel tableModel;
    private final JTextArea detailsArea;
    private final JComboBox<User> userComboBox;
    private final JComboBox<Developer> devComboBox;
    private final JTextField searchField;

    // Status bar
    private final JLabel statusLabel = new JLabel("Pret.");

    // --- Color palette
    private static final Color COL_BG        = new Color(246, 248, 250);
    private static final Color COL_BORDER    = new Color(225, 229, 234);
    private static final Color COL_HEADER_BG = new Color(236, 239, 244);
    private static final Color COL_HEADER_FG = new Color(45, 45, 45);

    private static final Color COL_PRIMARY   = new Color(33, 111, 219);
    private static final Color COL_SUCCESS   = new Color(0, 158, 96);
    private static final Color COL_WARNING   = new Color(224, 160, 0);
    private static final Color COL_DANGER    = new Color(214, 69, 65);
    private static final Color COL_INFO      = new Color(90, 130, 200);
    private static final Color COL_MUTED     = new Color(120, 120, 120);

    public TicketGUI(TicketManager ticketManager) {
        // Look and Feel (Nimbus) with fallback
        installLookAndFeel();
        this.ticketManager = ticketManager;

        setTitle("Systeme de Gestion de Tickets");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // Top toolbar (actions + search)
        JToolBar toolbar = buildToolbar();
        add(toolbar, BorderLayout.NORTH);

        // Center split (table left, details right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(8);

        // Table model
        String[] columns = {"ID", "Titre", "Statut", "Priorite", "Createur", "Assigne"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Integer.class;
                    default -> String.class;
                };
            }
        };

        // Table
        ticketTable = new JTable(tableModel);
        enhanceTableAppearance(ticketTable);
        installColoredRenderers(ticketTable);
        ticketTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) displaySelectedTicketDetails();
        });

        JScrollPane tableScroll = wrapTitled(new JScrollPane(ticketTable), "Liste des tickets");

        // Details
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        detailsArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        JScrollPane detailsScroll = wrapTitled(new JScrollPane(detailsArea), "Details du ticket");

        splitPane.setLeftComponent(tableScroll);
        splitPane.setRightComponent(detailsScroll);
        splitPane.setDividerLocation(0.55);
        add(splitPane, BorderLayout.CENTER);

        // Status bar
        add(buildStatusBar(), BorderLayout.SOUTH);

        // Combos (users / devs)
        userComboBox = new JComboBox<>();
        devComboBox  = new JComboBox<>();
        updateUserComboBoxes();

        // Search field to toolbar
        searchField = new JTextField();
        addSearchFieldToToolbar(toolbar);

        // Load
        refreshTicketTable();
        showWelcomeToast();
        pack();
    }

    // ================================
    //  Appearance and UI components
    // ================================

    private void installLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("control", COL_BG);
                    UIManager.put("nimbusBlueGrey", new Color(190, 198, 207));
                    UIManager.put("text", new Color(30, 33, 37));
                    break;
                }
            }
        } catch (Exception ignored) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignore2) { /* no-op */ }
        }
    }

    private JToolBar buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0, COL_BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        tb.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        // Visibility tweaks for Nimbus toolbar
        tb.setBackground(Color.WHITE);
        tb.putClientProperty("JToolBar.isRollover", Boolean.FALSE);

        JButton createBtn  = primaryButton("Nouveau",   e -> showCreateTicketDialog());
        JButton updateBtn  = tintedButton("Modifier",   COL_INFO, e -> showUpdateTicketDialog());
        JButton assignBtn  = tintedButton("Assigner",   COL_INFO, e -> showAssignTicketDialog());

        // ⬇️ Renommé pour être explicite : termine le ticket immédiatement (même non assigné)
        JButton closeBtn   = tintedButton("Terminer",   COL_SUCCESS, e -> closeSelectedTicket());

        JButton commentBtn = tintedButton("Commenter",  COL_PRIMARY, e -> showAddCommentDialog());
        JButton exportBtn  = tintedButton("Exporter",   COL_PRIMARY.darker(), e -> exportSelectedTicket());

        // Bouton cycle de vie avec icone
        JButton lifecycleBtn = tintedButton("Cycle", COL_WARNING, e -> advanceLifecycle());
        lifecycleBtn.setIcon(new StatusFlowIcon(14, new Color(80, 90, 100)));

        // New: button to add a developer
        JButton addDevBtn  = tintedButton("Ajouter dev", COL_PRIMARY, e -> showAddDeveloperDialog());

        JButton viewAllBtn = tintedButton("Voir tous",  COL_MUTED, e -> {
            clearFilterAndReload();
            showAllTicketsDialog(); // la fenêtre "Tous les tickets" montre aussi les TERMINE pour l'historique
        });

        // Shortcuts
        createBtn.setMnemonic(KeyEvent.VK_N);
        createBtn.setToolTipText("Creer un ticket (Ctrl+N)");
        assignAccelerator(createBtn, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));

        tb.add(createBtn);
        tb.add(updateBtn);
        tb.add(assignBtn);
        tb.add(closeBtn);
        tb.add(commentBtn);
        tb.add(exportBtn);

        // Bouton Cycle
        tb.add(lifecycleBtn);

        tb.add(addDevBtn);
        tb.addSeparator(new Dimension(16, 0));
        tb.add(viewAllBtn);

        return tb;
    }

    private void addSearchFieldToToolbar(JToolBar tb) {
        tb.add(Box.createHorizontalStrut(16));
        tb.add(new JLabel("Rechercher: "));
        JTextField field = new JTextField(24);
        field.setToolTipText("Filtrer par ID, Titre, Statut, Priorite, Createur ou Assigne (Ctrl+F)");
        field.putClientProperty("JComponent.sizeVariant", "small");
        field.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        assignAccelerator(field, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));

        this.searchField.setText("");
        this.searchField.setDocument(field.getDocument());

        tb.add(field);
        JButton clear = tintedOutlineButton("Effacer", e -> { field.setText(""); applyFilter(); });
        clear.setToolTipText("Effacer le filtre");
        tb.add(clear);
    }

    private JPanel buildStatusBar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBorder(new MatteBorder(1, 0, 0, 0, COL_BORDER));
        sb.setBackground(Color.WHITE);

        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        sb.add(statusLabel, BorderLayout.WEST);

        JLabel brand = new JLabel("TicketGUI • Pure Fabrication");
        brand.setBorder(new EmptyBorder(6, 10, 6, 10));
        brand.setForeground(COL_MUTED);
        sb.add(brand, BorderLayout.EAST);

        return sb;
    }

    private void enhanceTableAppearance(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
        header.setForeground(COL_HEADER_FG);
        header.setBackground(COL_HEADER_BG);

        // Zebra striping
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(252, 253, 255) : new Color(243, 246, 250));
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        // Column widths
        int[] widths = {70, 320, 120, 120, 160, 160};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
    }

    /** Colored renderers for Status and Priority columns. */
    private void installColoredRenderers(JTable table) {
        TableCellRenderer base = table.getDefaultRenderer(Object.class);

        table.getColumnModel().getColumn(2).setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            Component c = base.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            if (!isSelected) c.setForeground(colorForStatus(String.valueOf(value)));
            return c;
        });

        table.getColumnModel().getColumn(3).setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            Component c = base.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            if (!isSelected) c.setForeground(colorForPriority(String.valueOf(value)));
            return c;
        });
    }

    private Color colorForStatus(String s) {
        if (s == null) return Color.BLACK;
        s = s.toUpperCase();
        if (s.contains("TERMINE"))    return COL_SUCCESS;
        if (s.contains("VALIDATION")) return COL_WARNING;
        if (s.contains("ASSIG"))      return new Color(123, 72, 196);
        if (s.contains("OUVERT"))     return COL_INFO;
        return Color.BLACK;
    }

    private Color colorForPriority(String p) {
        if (p == null) return Color.BLACK;
        p = p.toUpperCase();
        if (p.contains("URGENTE")) return COL_DANGER;
        if (p.contains("HAUTE"))   return COL_WARNING;
        if (p.contains("MOYENNE")) return COL_INFO;
        if (p.contains("BASSE"))   return COL_SUCCESS.darker();
        return Color.BLACK;
    }

    private JScrollPane wrapTitled(JScrollPane content, String title) {
        TitledBorder tb = new TitledBorder(
                new CompoundBorder(
                        new EmptyBorder(6, 6, 6, 6),
                        new MatteBorder(1, 1, 1, 1, COL_BORDER)
                ),
                " " + title + " "
        );
        tb.setTitleFont(tb.getTitleFont().deriveFont(Font.BOLD, 13f));
        tb.setTitleColor(new Color(80, 90, 100));
        content.setBorder(tb);
        return content;
    }

    // Base for toolbar buttons: forces visibility under Nimbus
    private JButton buttonBase(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.addActionListener(al);
        b.setUI(new BasicButtonUI());
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.setFocusPainted(false);
        b.setRolloverEnabled(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        b.setPreferredSize(new Dimension(Math.max(100, b.getPreferredSize().width), 34));
        return b;
    }

    private JButton primaryButton(String text, ActionListener al) {
        JButton b = buttonBase(text, al);
        b.setBackground(COL_PRIMARY);
        b.setForeground(Color.WHITE);
        b.setBorder(new CompoundBorder(new LineBorder(COL_PRIMARY.darker()),
                new EmptyBorder(6, 12, 6, 12)));
        return b;
    }

    private JButton tintedButton(String text, Color color, ActionListener al) {
        JButton b = buttonBase(text, al);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setBorder(new CompoundBorder(new LineBorder(color.darker()),
                new EmptyBorder(6, 12, 6, 12)));
        return b;
    }

    private JButton tintedOutlineButton(String text, ActionListener al) {
        JButton b = buttonBase(text, al);
        b.setBackground(Color.WHITE);
        b.setForeground(COL_PRIMARY.darker());
        b.setBorder(new CompoundBorder(new LineBorder(COL_PRIMARY.darker()),
                new EmptyBorder(5, 11, 5, 11)));
        return b;
    }

    private void assignAccelerator(JComponent c, KeyStroke ks) {
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "acc");
        c.getActionMap().put("acc", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (c instanceof JButton btn) btn.doClick();
                if (c instanceof JTextField txt) txt.requestFocusInWindow();
            }
        });
    }

    private void setStatus(String s) { statusLabel.setText(s); }

    private void showWelcomeToast() {
        setStatus("Connecte : " + userDisplay(ticketManager.getCurrentUser()) +
                " • " + ticketManager.getTickets().size() + " ticket(s).");
    }

    private String userDisplay(User u) {
        String role = (u != null && u.isDeveloper()) ? "Developpeur" : "Utilisateur";
        return (u == null ? "N/A" : u.getName() + " (" + role + ")");
    }

    // ================================
    //  Actions
    // ================================

    // N'affiche PAS les tickets TERMINE dans la table principale
    private void refreshTicketTable() {
        tableModel.setRowCount(0);
        List<Ticket> tickets = ticketManager.getTickets();
        for (Ticket t : tickets) {
            if (!isVisibleInMainList(t)) continue; // <--- filtre des TERMINES
            Object[] row = {
                    t.getTicketID(),
                    t.getTitle(),
                    t.getStatus(),
                    t.getPriority(),
                    t.getCreator()  != null ? t.getCreator().getName()   : "N/A",
                    t.getAssignee() != null ? t.getAssignee().getName()  : "Non assigne"
            };
            tableModel.addRow(row);
        }
        setStatus("Liste mise a jour • " + tableModel.getRowCount() + " ticket(s) actifs.");
        // si la sélection pointe vers un ticket désormais masqué, vider le panneau de détails
        if (ticketTable.getSelectedRow() == -1) {
            detailsArea.setText("");
        }
    }

    private void displaySelectedTicketDetails() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            detailsArea.setText("");
            return;
        }
        int ticketId = (int) tableModel.getValueAt(selectedRow, 0);
        Ticket ticket = findTicketById(ticketId);
        if (ticket == null) { detailsArea.setText(""); return; }

        StringBuilder sb = new StringBuilder(512);
        sb.append("=======================================\n");
        sb.append("         TICKET #").append(ticket.getTicketID()).append("\n");
        sb.append("=======================================\n\n");
        sb.append("Titre      : ").append(ticket.getTitle()).append("\n");
        sb.append("Statut     : ").append(ticket.getStatus()).append("\n");
        sb.append("Priorite   : ").append(ticket.getPriority()).append("\n");
        sb.append("Createur   : ").append(ticket.getCreator() != null ? ticket.getCreator().getName() : "N/A").append("\n");
        sb.append("Assigne a  : ").append(ticket.getAssignee() != null ? ticket.getAssignee().getName() : "Non assigne").append("\n");
        sb.append("Cree le    : ").append(ticket.getCreationDate()).append("\n");
        sb.append("Modifie le : ").append(ticket.getUpdateDate()).append("\n\n");

        sb.append("-- Description --\n");
        if (ticket.getDescription() != null) {
            TicketDescription desc = ticket.getDescription();
            if (desc instanceof TextDescription td) {
                sb.append(td.getText()).append("\n");
            } else if (desc instanceof ImageDescription id) {
                sb.append("[Image: ").append(id.getPathOrUrl()).append("]\n");
            } else if (desc instanceof VideoDescription vd) {
                sb.append("[Video: ").append(vd.getPathOrUrl()).append("]\n");
            }
        } else {
            sb.append("(aucune description)\n");
        }

        sb.append("\n-- Commentaires (").append(ticket.getComments().size()).append(") --\n");
        if (!ticket.getComments().isEmpty()) {
            for (Comment c : ticket.getComments()) {
                sb.append("\n• ").append(c.getAuthor().getName());
                sb.append(" [").append(c.getTimestamp()).append("]\n  ");
                if (c.getContent() instanceof TextDescription td) {
                    sb.append(td.getText());
                }
                sb.append("\n");
            }
        } else {
            sb.append("(aucun commentaire)\n");
        }

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
        setStatus("Ticket #" + ticket.getTicketID() + " selectionne.");
    }

    private void showCreateTicketDialog() {
        JDialog dialog = modal("Creer un ticket", 520, 420);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 18, 8, 18));
        GridBagConstraints c = gb();

        JTextField idField    = new JTextField();
        JTextField titleField = new JTextField();
        JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
        JTextArea descArea = new JTextArea(5, 20);

        addRow(form, c, 0, "ID:", idField);
        addRow(form, c, 1, "Titre:", titleField);
        addRow(form, c, 2, "Priorite:", priorityBox);
        addRowArea(form, c, 3, "Description:", descArea);

        JPanel actions = buttonRow(
                button("Creer", e -> {
                    try {
                        int id = Integer.parseInt(idField.getText().trim());
                        String title = titleField.getText().trim();
                        if (title.isBlank()) throw new IllegalArgumentException("Le titre est obligatoire.");
                        Priority priority = (Priority) priorityBox.getSelectedItem();

                        Ticket ticket = new Ticket(id, title, ticketManager.getCurrentUser(), priority);
                        if (!descArea.getText().isBlank()) {
                            ticket.setTicketDescription(new TextDescription(descArea.getText()));
                        }

                        ticketManager.createTicket(ticket);
                        refreshTicketTable();
                        dialog.dispose();
                        setStatus("Ticket #" + id + " cree.");
                        toast("Ticket cree avec succes.");
                    } catch (Exception ex) {
                        error(dialog, ex);
                    }
                }),
                button("Annuler", e -> dialog.dispose())
        );

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showUpdateTicketDialog() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;

        JDialog dialog = modal("Modifier Ticket #" + ticket.getTicketID(), 520, 380);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 18, 8, 18));
        GridBagConstraints c = gb();

        JTextField titleField = new JTextField(ticket.getTitle());
        JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
        priorityBox.setSelectedItem(ticket.getPriority());
        JTextArea descArea = new JTextArea(5, 20);
        if (ticket.getDescription() instanceof TextDescription td) descArea.setText(td.getText());

        addRow(form, c, 0, "Titre:", titleField);
        addRow(form, c, 1, "Priorite:", priorityBox);
        addRowArea(form, c, 2, "Description:", descArea);

        JPanel actions = buttonRow(
                button("Mettre a jour", e -> {
                    try {
                        String newTitle = titleField.getText().isBlank() ? null : titleField.getText().trim();
                        Priority newPriority = (Priority) priorityBox.getSelectedItem();
                        TicketDescription newDesc = descArea.getText().isBlank() ? null : new TextDescription(descArea.getText());

                        ticketManager.updateTicket(ticket.getTicketID(), newTitle, newPriority, newDesc);
                        refreshTicketTable();
                        displaySelectedTicketDetails();
                        dialog.dispose();
                        setStatus("Ticket #" + ticket.getTicketID() + " mis a jour.");
                        toast("Modifications enregistrees.");
                    } catch (Exception ex) {
                        error(dialog, ex);
                    }
                }),
                button("Annuler", e -> dialog.dispose())
        );

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showAssignTicketDialog() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;

        updateUserComboBoxes();
        if (devComboBox.getItemCount() == 0) {
            warn("Aucun developpeur disponible.");
            return;
        }

        Developer dev = (Developer) JOptionPane.showInputDialog(
                this,
                "Selectionner un developpeur :",
                "Assigner Ticket #" + ticket.getTicketID(),
                JOptionPane.QUESTION_MESSAGE,
                null,
                getDevelopersArray(),
                null
        );

        if (dev != null) {
            ticketManager.assignTicket(ticket, dev);
            refreshTicketTable();
            displaySelectedTicketDetails();
            setStatus("Ticket #" + ticket.getTicketID() + " assigne a " + dev.getName() + ".");
        }
    }

    // Terminer immédiatement (même non assigné) et enlever de la liste
    private void closeSelectedTicket() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Terminer le ticket #" + ticket.getTicketID() + " ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Soit via le manager si tu as une règle métier:
                ticketManager.closeTicket(ticket);
            } catch (Exception ex) {
                // fallback si nécessaire
                try { ticket.updateStatus(TicketStatus.TERMINE); } catch (Exception ignored) {}
            }
            refreshTicketTable();      // sera retiré de l'affichage (filtré)
            detailsArea.setText("");   // vider les détails au cas où la sélection n'existe plus
            setStatus("Ticket #" + ticket.getTicketID() + " termine et retire de la liste.");
        }
    }

    private void showAddCommentDialog() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;

        JTextArea commentArea = new JTextArea(5, 30);
        commentArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(commentArea);
        scroll.setPreferredSize(new Dimension(460, 160));

        int result = JOptionPane.showConfirmDialog(
                this, scroll,
                "Ajouter un commentaire au Ticket #" + ticket.getTicketID(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION && !commentArea.getText().isBlank()) {
            TextDescription content = new TextDescription(commentArea.getText());
            ticket.addComment(ticketManager.getCurrentUser(), content);
            displaySelectedTicketDetails();
            toast("Commentaire ajoute.");
        }
    }

    private void exportSelectedTicket() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ExportStrategy pdfExporter = new PdfExporter();
            ticketManager.exportTicket(ticket, pdfExporter, baos);

            String content = baos.toString("UTF-8");
            JTextArea textArea = new JTextArea(content);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(700, 460));

            JOptionPane.showMessageDialog(
                    this,
                    scrollPane,
                    "Export PDF — Ticket #" + ticket.getTicketID(),
                    JOptionPane.INFORMATION_MESSAGE
            );
            setStatus("Export PDF affiche.");
        } catch (Exception ex) {
            error(this, ex);
        }
    }

    // =========================================
    //  New feature: add a new developer (UI)
    // =========================================
    private void showAddDeveloperDialog() {
        JDialog dialog = modal("Ajouter un developpeur", 460, 280);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 18, 8, 18));
        GridBagConstraints c = gb();

        JTextField idField    = new JTextField();
        JTextField nameField  = new JTextField();
        JTextField emailField = new JTextField();

        addRow(form, c, 0, "ID:", idField);
        addRow(form, c, 1, "Nom:", nameField);
        addRow(form, c, 2, "Email:", emailField);

        JPanel actions = buttonRow(
                button("Ajouter", e -> {
                    try {
                        String sid = idField.getText().trim();
                        String name = nameField.getText().trim();
                        String email = emailField.getText().trim();

                        if (sid.isEmpty())  throw new IllegalArgumentException("ID obligatoire.");
                        if (name.isEmpty()) throw new IllegalArgumentException("Nom obligatoire.");
                        if (email.isEmpty())throw new IllegalArgumentException("Email obligatoire.");

                        int id = Integer.parseInt(sid);
                        Developer dev = new Developer(id, name, email);

                        // Add to manager
                        ticketManager.addUser(dev);

                        // Update UI
                        updateUserComboBoxes();
                        setStatus("Developpeur ajoute: " + name + " (" + email + ")");
                        dialog.dispose();

                        JOptionPane.showMessageDialog(this, "Developpeur ajoute avec succes.", "OK",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        error(dialog, ex);
                    }
                }),
                button("Annuler", e -> dialog.dispose())
        );

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ================================
    //  All Tickets dialog
    // ================================
    private void showAllTicketsDialog() {
        JDialog dialog = modal("Tous les tickets", 950, 560);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.setBorder(new EmptyBorder(8, 8, 0, 8));
        JTextField quick = new JTextField();
        top.add(new JLabel("Filtre rapide: "), BorderLayout.WEST);
        top.add(quick, BorderLayout.CENTER);
        dialog.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Titre", "Statut", "Priorite", "Createur", "Assigne", "Cree le", "Modifie le", "Commentaires"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Integer.class;
                    case 8 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        JTable tbl = new JTable(mdl);
        tbl.setAutoCreateRowSorter(true);
        tbl.setRowHeight(26);
        JTableHeader header = tbl.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        header.setBackground(COL_HEADER_BG);
        header.setForeground(COL_HEADER_FG);

        Runnable fill = () -> {
            mdl.setRowCount(0);
            String q = quick.getText() == null ? "" : quick.getText().trim().toLowerCase();
            for (Ticket t : ticketManager.getTickets()) {
                // "Tous les tickets" = inclut aussi les TERMINE
                if (q.isEmpty() || matches(t, q)) {
                    mdl.addRow(new Object[]{
                            t.getTicketID(),
                            t.getTitle(),
                            t.getStatus(),
                            t.getPriority(),
                            t.getCreator()  != null ? t.getCreator().getName()  : "N/A",
                            t.getAssignee() != null ? t.getAssignee().getName() : "Non assigne",
                            String.valueOf(t.getCreationDate()),
                            String.valueOf(t.getUpdateDate()),
                            t.getComments() == null ? 0 : t.getComments().size()
                    });
                }
            }
        };
        fill.run();
        quick.getDocument().addDocumentListener(new SimpleDocListener(fill));

        int[] w = {60, 250, 110, 110, 150, 150, 160, 160, 110};
        for (int i = 0; i < w.length; i++) {
            tbl.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        TableCellRenderer base = tbl.getDefaultRenderer(Object.class);
        tbl.getColumnModel().getColumn(2).setCellRenderer((table, value, isSel, hasFocus, row, col) -> {
            Component cpt = base.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
            if (!isSel) cpt.setForeground(colorForStatus(String.valueOf(value)));
            return cpt;
        });
        tbl.getColumnModel().getColumn(3).setCellRenderer((table, value, isSel, hasFocus, row, col) -> {
            Component cpt = base.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
            if (!isSel) cpt.setForeground(colorForPriority(String.valueOf(value)));
            return cpt;
        });

        dialog.add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton refresh = tintedButton("Rafraichir", COL_INFO, e -> fill.run());
        JButton close   = tintedOutlineButton("Fermer", e -> dialog.dispose());
        bottom.add(refresh);
        bottom.add(close);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void clearFilterAndReload() {
        if (searchField.getDocument() != null) {
            try {
                searchField.setText("");
            } catch (Exception ignored) {}
        }
        refreshTicketTable();
        setStatus("Tous les tickets affiches.");
    }

    // ================================
    //  Cycle de vie
    // ================================

    // Avance le ticket selectionne dans le cycle OUVERT -> ASSIGNE -> VALIDATION -> TERMINE
    private void advanceLifecycle() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        TicketStatus s = t.getStatus();
        if (s == null) {
            warn("Statut inconnu.");
            return;
        }

        try {
            switch (s) {
                case OUVERT -> {
                    // Pour passer a ASSIGNE, un developpeur doit etre present
                    if (t.getAssignee() == null) {
                        int rep = JOptionPane.showConfirmDialog(
                                this,
                                "Ce ticket est OUVERT mais non assigne. Voulez-vous l'assigner maintenant ?",
                                "Cycle de vie",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (rep == JOptionPane.YES_OPTION) {
                            showAssignTicketDialog();
                        }
                        return;
                    }
                    setStatusAndRefresh(t, TicketStatus.ASSIGNE, "Passe de OUVERT a ASSIGNE.");
                }
                case ASSIGNE -> setStatusAndRefresh(t, TicketStatus.VALIDATION, "Passe de ASSIGNE a VALIDATION.");
                case VALIDATION -> setStatusAndRefresh(t, TicketStatus.TERMINE,   "Passe de VALIDATION a TERMINE.");
                case TERMINE -> warn("Le ticket est deja TERMINE.");
                default -> warn("Transition non supportee.");
            }
        } catch (Exception ex) {
            error(this, ex);
        }
    }

    // Applique le nouveau statut et rafraichit l'UI
    private void setStatusAndRefresh(Ticket t, TicketStatus next, String msg) {
        try {
            // Si tu as une API manager: ticketManager.changeStatus(t, next);
            try { t.updateStatus(next); } catch (Exception e) {  t.updateStatus(next); }
            refreshTicketTable();                 // retirera le ticket si TERMINE
            selectTicketRow(t.getTicketID());     // si encore visible
            if (next == TicketStatus.TERMINE) {
                detailsArea.setText("");
            } else {
                displaySelectedTicketDetails();
            }
            setStatus(msg);
        } catch (Exception ex) {
            error(this, ex);
        }
    }

    // Reselectionne la ligne correspondant au ticketID
    private void selectTicketRow(int ticketId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object v = tableModel.getValueAt(i, 0);
            if (v instanceof Integer && (Integer) v == ticketId) {
                int viewIndex = ticketTable.convertRowIndexToView(i);
                ticketTable.getSelectionModel().setSelectionInterval(viewIndex, viewIndex);
                ticketTable.scrollRectToVisible(ticketTable.getCellRect(viewIndex, 0, true));
                return;
            }
        }
        // pas trouvé (probablement TERMINE et donc masqué)
        ticketTable.clearSelection();
    }

    // ================================
    //  Helpers
    // ================================

    private boolean isVisibleInMainList(Ticket t) {
        return t.getStatus() != TicketStatus.TERMINE;
    }

    private Ticket getSelectedTicket() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) {
            warn("Selectionnez un ticket d'abord.");
            return null;
        }
        int ticketId = (int) ticketTable.getValueAt(row, 0);
        return findTicketById(ticketId);
    }

    private Ticket findTicketById(int id) {
        return ticketManager.getTickets().stream()
                .filter(t -> t.getTicketID() == id)
                .findFirst()
                .orElse(null);
    }

    private void updateUserComboBoxes() {
        userComboBox.removeAllItems();
        devComboBox.removeAllItems();
        for (User u : ticketManager.getUsers()) {
            userComboBox.addItem(u);
            if (u instanceof Developer d) devComboBox.addItem(d);
        }
    }

    private Developer[] getDevelopersArray() {
        List<Developer> devs = new ArrayList<>();
        for (User u : ticketManager.getUsers()) if (u instanceof Developer d) devs.add(d);
        return devs.toArray(new Developer[0]);
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        for (Ticket t : ticketManager.getTickets()) {
            if (!isVisibleInMainList(t)) continue; // <--- filtre des TERMINES aussi dans le filtre
            if (matches(t, q)) {
                tableModel.addRow(new Object[]{
                        t.getTicketID(), t.getTitle(), t.getStatus(), t.getPriority(),
                        t.getCreator() != null ? t.getCreator().getName() : "N/A",
                        t.getAssignee() != null ? t.getAssignee().getName() : "Non assigne"
                });
            }
        }
        setStatus("Filtre: \"" + q + "\" • " + tableModel.getRowCount() + " resultat(s).");
    }

    private boolean matches(Ticket t, String q) {
        if (q.isEmpty()) return true;
        return String.valueOf(t.getTicketID()).contains(q)
                || contains(t.getTitle(), q)
                || contains(String.valueOf(t.getStatus()), q)
                || contains(String.valueOf(t.getPriority()), q)
                || contains(t.getCreator() != null ? t.getCreator().getName() : "", q)
                || contains(t.getAssignee() != null ? t.getAssignee().getName() : "", q);
    }

    private boolean contains(String s, String q) {
        return s != null && s.toLowerCase().contains(q);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
        setStatus(msg);
    }

    private void error(Component parent, Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        setStatus("Erreur : " + ex.getMessage());
    }

    private void toast(String msg) {
        setStatus(msg);
    }

    // Layout helpers (forms)
    private static GridBagConstraints gb() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        return c;
    }

    private static void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        GridBagConstraints l = (GridBagConstraints) c.clone();
        l.gridx = 0; l.gridy = row; l.weightx = 0;
        p.add(new JLabel(label), l);
        GridBagConstraints f = (GridBagConstraints) c.clone();
        f.gridx = 1; f.gridy = row; f.weightx = 1.0;
        p.add(field, f);
    }

    private static void addRowArea(JPanel p, GridBagConstraints c, int row, String label, JTextArea area) {
        GridBagConstraints l = (GridBagConstraints) c.clone();
        l.gridx = 0; l.gridy = row; l.weightx = 0; l.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel(label), l);
        GridBagConstraints f = (GridBagConstraints) c.clone();
        f.gridx = 1; f.gridy = row; f.weightx = 1.0; f.weighty = 1.0; f.fill = GridBagConstraints.BOTH;
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(new CompoundBorder(new LineBorder(new Color(220, 224, 229)), new EmptyBorder(6,6,6,6)));
        p.add(sp, f);
    }

    private static JPanel buttonRow(JButton left, JButton right) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        row.setBorder(new EmptyBorder(0, 12, 12, 12));
        row.add(left);
        row.add(right);
        return row;
    }

    private static JButton button(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.addActionListener(al);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        return b;
    }

    private static JDialog modal(String title, int w, int h) {
        JDialog d = new JDialog((Frame) null, title, true);
        d.setLayout(new BorderLayout(0, 0));
        d.setSize(w, h);
        d.setLocationRelativeTo(null);
        return d;
    }

    // Simple document listener
    private record SimpleDocListener(Runnable onChange) implements javax.swing.event.DocumentListener {
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
    }

    // Petite icone en forme de fleche, pour le bouton Cycle
    private static class StatusFlowIcon implements Icon {
        private final int size;
        private final Color color;

        StatusFlowIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);

            int w = size;
            int h = size;
            int cx = x;
            int cy = y + h / 2;

            g2.setStroke(new BasicStroke(Math.max(2f, size / 8f)));
            g2.drawLine(cx + 2, cy, cx + w - h/2, cy);

            Polygon p = new Polygon();
            p.addPoint(cx + w - h/2, cy - h/3);
            p.addPoint(cx + w - 2,   cy);
            p.addPoint(cx + w - h/2, cy + h/3);
            g2.fillPolygon(p);

            g2.dispose();
        }
    }
}

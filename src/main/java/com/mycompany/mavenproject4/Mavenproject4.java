/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mavenproject4;

/**
 *
 * @author ASUS
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Mavenproject4 extends JFrame {

    private JTable visitTable;
    private DefaultTableModel tableModel;
    
    private JTextField nameField;
    private JTextField nimField;
    private JComboBox<String> studyProgramBox;
    private JComboBox<String> purposeBox;
    private JButton addButton;
    private JButton clearButton;
    
    private boolean actionColumnsAdded = false;

    private List<VisitLog2> temp = new ArrayList<>();

    public Mavenproject4() {
        setTitle("Library Visit Log");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        nameField = new JTextField();
        nimField = new JTextField();
        studyProgramBox = new JComboBox<>(new String[] {"Sistem dan Teknologi Informasi", "Bisnis Digital", "Kewirausahaan"});
        purposeBox = new JComboBox<>(new String[] {"Membaca", "Meminjam/Mengembalikan Buku", "Research", "Belajar"});
        addButton = new JButton("Add");
        clearButton = new JButton("Clear");

        inputPanel.setBorder(BorderFactory.createTitledBorder("Visit Entry Form"));
        inputPanel.add(new JLabel("NIM:"));
        inputPanel.add(nimField);
        inputPanel.add(new JLabel("Name Mahasiswa:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Program Studi:"));
        inputPanel.add(studyProgramBox);
        inputPanel.add(new JLabel("Tujuan Kunjungan:"));
        inputPanel.add(purposeBox);
        inputPanel.add(addButton);
        inputPanel.add(clearButton);

        addButton.addActionListener(e -> addEntry());
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> updateEntry());
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteEntry());

        inputPanel.add(editBtn);
        inputPanel.add(deleteBtn);

        add(inputPanel, BorderLayout.NORTH);

        String[] columns = {"Waktu Kunjungan", "NIM", "Nama", "Program Studi", "Tujuan Kunjungan"};
        tableModel = new DefaultTableModel(columns, 0);
        visitTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(visitTable);
        add(scrollPane, BorderLayout.CENTER);

        
        setVisible(true);
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control G"), "showActions");

        getRootPane().getActionMap().put("showActions", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!actionColumnsAdded) {
                    addActionColumns();
                    actionColumnsAdded = true;
                }
            }
        });
    }

    private void loadEntries() {
        try {
            String query = "query { allVisitLogs { id studentName studentId studyProgram purpose visitTime } }";
            String response = sendGraphQLRequest(query);

            
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonObject data = jsonObject.get("data").getAsJsonObject();
            
            // This is the real data we need
            JsonArray allVisitLogs = data.get("allVisitLogs").getAsJsonArray();
            
            // model is the DefaultTableModel
            tableModel.setRowCount(0);
            for (int i = 0; i < allVisitLogs.size(); i++) {
                JsonObject log = allVisitLogs.get(i).getAsJsonObject();
                var objs = new Object[] { log.get("visitTime").getAsString(),
                log.get("studentId").getAsString(), log.get("studentName").getAsString(), log.get("studyProgram").getAsString(), log.get("purpose").getAsString() };
                temp.add(new VisitLog2(i, log.get("studentName").getAsString(), log.get("studentId").getAsString(), log.get("studyProgram").getAsString(), log.get("purpose").getAsString(), log.get("visitTime").getAsString()));
                tableModel.addRow(objs);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addEntry() {
        try {
            String query = String.format(
                    "mutation { addVisitLog(studentName: \"%s\", studentId: \"%s\", studyProgram: \"%s\", purpose: \"%s\") { id } }",
                    nameField.getText(),
                    nimField.getText(),
                    (String)studyProgramBox.getSelectedItem(),
                    (String)purposeBox.getSelectedItem());
            sendGraphQLRequest(query);

            loadEntries();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateEntry() {
        try {
            var selectedRow = visitTable.getSelectedRow();
            if (selectedRow == -1) {
                throw new Exception("No row selected");
            }
        
            var id = ((VisitLog2)temp.get(selectedRow)).id;
        
            String query = String.format(
                    "mutation { updateVisitLog(id: %s, studentName: \"%s\", studentId: \"%s\", studyProgram: \"%s\", purpose: \"%s\") { id studentName } }",
                    id,
                    nameField.getText(),
                    nimField.getText(),
                    (String)studyProgramBox.getSelectedItem(),
                    (String)purposeBox.getSelectedItem());
            sendGraphQLRequest(query);
        
            // reload here
            loadEntries();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteEntry() {
        try {
            var selectedRow = visitTable.getSelectedRow();
            if (selectedRow == -1) {
                throw new Exception("No row selected");
            }
        
            var id = ((VisitLog2)temp.get(selectedRow)).id;
        
            String query = String.format(
                    "mutation { deleteVisitLog(id: %s) }",
                    id);
            sendGraphQLRequest(query);
        
            // reload here
            loadEntries();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private static int col = 0;
    private void addActionColumns() {
        if (col > 1) return;
        ++col;
        tableModel.addColumn("Action");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (col == 1)
                tableModel.setValueAt("Action", i, tableModel.getColumnCount() - 1);
                else if (col == 2)
                tableModel.setValueAt("Delete", i, tableModel.getColumnCount() - 1);
        }

        visitTable.getColumn("Action").setCellRenderer(new ButtonRenderer());

        visitTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    @SuppressWarnings("deprecation")
    private String sendGraphQLRequest(String query) throws Exception {
        String json = new Gson().toJson(new GraphQLQuery(query));
        URL url = new URL("http://localhost:4567/graphql");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Mavenproject4::new);
    }

    class GraphQLQuery {
        @SuppressWarnings("unused")
        String query;

        GraphQLQuery(String query) {
            this.query = query;
        }
    }
}

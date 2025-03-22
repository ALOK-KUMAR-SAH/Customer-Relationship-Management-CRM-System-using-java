import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
public class CRMSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/crm_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Kumaralok@123";

    private JFrame frame;
    private JTextField nameField, emailField, phoneField, addressField;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    public CRMSystem() {
        // 🔹 Set up the Swing GUI
        frame = new JFrame("CRM System");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 🔹 Input Fields
        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        panel.add(phoneField);

        panel.add(new JLabel("Address:"));
        addressField = new JTextField();
        panel.add(addressField);

        JButton addButton = new JButton("Add Customer");
        JButton updateButton = new JButton("Update Customer");
        JButton deleteButton = new JButton("Delete Customer");
        panel.add(addButton);
        panel.add(updateButton);

        frame.add(panel, BorderLayout.NORTH);

        // 🔹 Customer Table
        String[] columnNames = {"ID", "Name", "Email", "Phone", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0);
        customerTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 🔹 Delete Button
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(deleteButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // 🔹 Database Operations
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            createTable(conn);
            loadCustomers(conn);

            // 🔹 Button Actions
            addButton.addActionListener(e -> addCustomer(conn));
            updateButton.addActionListener(e -> updateCustomer(conn));
            deleteButton.addActionListener(e -> deleteCustomer(conn));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Database Connection Failed!", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        frame.setVisible(true);
    }

    private void createTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS customers (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "email VARCHAR(100) UNIQUE, " +
                "phone VARCHAR(15) UNIQUE, " +
                "address TEXT)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void loadCustomers(Connection conn) {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM customers";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading customers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCustomer(Connection conn) {
        try {
            String sql = "INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, emailField.getText());
            pstmt.setString(3, phoneField.getText());
            pstmt.setString(4, addressField.getText());
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Customer added successfully!");
            loadCustomers(conn);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error adding customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer(Connection conn) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a customer to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            String sql = "UPDATE customers SET name=?, email=?, phone=?, address=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, emailField.getText());
            pstmt.setString(3, phoneField.getText());
            pstmt.setString(4, addressField.getText());
            pstmt.setInt(5, id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Customer updated successfully!");
            loadCustomers(conn);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error updating customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer(Connection conn) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a customer to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            String sql = "DELETE FROM customers WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Customer deleted successfully!");
            loadCustomers(conn);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error deleting customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CRMSystem::new);
    }
}
//javac -cp ".;mysql-connector-j-9.2.0.jar" CRMSystem.java
//java -cp ".;mysql-connector-j-9.2.0.jar" CRMSystem
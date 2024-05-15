import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import net.proteanit.sql.DbUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Lecturer {
    private JButton signOutButton;
    private JTable table1;
    private JTextField textField1;
    private JButton searchButton;
    private JComboBox<String> comboBox1;
    private JButton addButton;
    private JPanel Main;
    private JButton editButton;

    Connection con;
    PreparedStatement pst;

    // Map to hold display names and corresponding table names
    private final Map<String, String> tableMap = new HashMap<>();

    public Lecturer() {
        connect();

        // Define the fields to populate in the comboBox1
        String[] displayFields = {"Student", "Student Grade", "IT Supports", "Question", "Examination", "View Examination"};
        String[] tableNames = {"student", "grade_student", "it_support", "question", "examination", "view_examination"};

        // Populate the map
        for (int i = 0; i < displayFields.length; i++) {
            tableMap.put(displayFields[i], tableNames[i]);
        }

        comboBox1.setModel(new JComboBox<>(displayFields).getModel());

        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String displayValue = (String) comboBox1.getSelectedItem();
                String tableName = tableMap.get(displayValue);
                loadSelectedTable(tableName);
            }
        });

        // Add action listener for the search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = textField1.getText();
                String displayValue = (String) comboBox1.getSelectedItem();
                String tableName = tableMap.get(displayValue);
                searchInTable(tableName, searchTerm);
            }
        });

        // Add action listener for the add button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStudent addForm = new addStudent(Lecturer.this);
                addForm.setContentPane(addForm.Main);
                addForm.setVisible(true);
            }
        });
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table1.getSelectedRow();
                System.out.println("Selected row index:" + selectedRow);
            }
        });
    }

    public JPanel getMainPanel() {
        return Main;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Lecturer");
        frame.setContentPane(new Lecturer().getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void connect() {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setUser("sa");
        ds.setPassword("123456");
        ds.setServerName("DESKTOP-EJIGPN3\\SQLEXPRESS");
        ds.setPortNumber(1433);
        ds.setDatabaseName("OnlineExaminationSystem");
        ds.setEncrypt(false);

        try {
            con = ds.getConnection();
            System.out.println("Connection successful");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void table_load() {
        try {
            pst = con.prepareStatement("SELECT * FROM lecturer");
            ResultSet rs = pst.executeQuery();
            table1.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    void loadSelectedTable(String tableName) {
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName);
            ResultSet rs = pst.executeQuery();
            table1.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    void searchInTable(String tableName, String searchTerm) {
        try {
            // Get metadata to retrieve column names
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);

            // Construct the query dynamically
            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE ");
            boolean first = true;
            while (columns.next()) {
                if (!first) {
                    queryBuilder.append(" OR ");
                }
                String columnName = columns.getString("COLUMN_NAME");
                queryBuilder.append(columnName).append(" LIKE ?");
                first = false;
            }

            pst = con.prepareStatement(queryBuilder.toString());

            // Set the search term for all columns
            columns.beforeFirst();
            int index = 1;
            while (columns.next()) {
                pst.setString(index++, "%" + searchTerm + "%");
            }

            ResultSet rs = pst.executeQuery();
            table1.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

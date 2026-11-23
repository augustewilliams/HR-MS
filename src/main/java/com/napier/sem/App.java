package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        // Create new Application
        App a = new App();

        // Connect to database
        a.connect();

        // Get employees for a specific role (e.g., "Senior Engineer")
        ArrayList<Employee> employees = a.getSalariesByRole("Senior Engineer");

        // Display results
        a.printSalaries(employees);

        // Disconnect from database
        a.disconnect();
    }

    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect() {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start
                Thread.sleep(30000);
                // Connect to database
                con = DriverManager.getConnection(
                        "jdbc:mysql://db:3306/employees?allowPublicKeyRetrieval=true&useSSL=false",
                        "root",
                        "example"
                );
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     * Gets all current employees and their salaries for a given role.
     *
     * @param title The job title/role to filter by.
     * @return A list of matching employees, or null if there is an error.
     */
    public ArrayList<Employee> getSalariesByRole(String title) {
        try {
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, titles.title, salaries.salary "
                            + "FROM employees "
                            + "INNER JOIN salaries ON employees.emp_no = salaries.emp_no "
                            + "INNER JOIN titles ON employees.emp_no = titles.emp_no "
                            + "WHERE salaries.to_date = '9999-01-01' "
                            + "AND titles.to_date = '9999-01-01' "
                            + "AND titles.title = ? "
                            + "ORDER BY employees.emp_no ASC";

            // Use PreparedStatement to safely bind the role parameter
            PreparedStatement pstmt = con.prepareStatement(strSelect);
            pstmt.setString(1, title);

            // Execute SQL statement
            ResultSet rset = pstmt.executeQuery();

            // Extract employee information
            ArrayList<Employee> employees = new ArrayList<>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.title = rset.getString("title");
                emp.salary = rset.getInt("salary");
                employees.add(emp);
            }
            return employees;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    /**
     * Prints a list of employees.
     *
     * @param employees The list of employees to print.
     */
    public void printSalaries(ArrayList<Employee> employees) {
        // Check employees is not null
        if (employees == null) {
            System.out.println("No employees");
            return;
        }

        // Print header (5 format specifiers for 5 columns)
        System.out.println(String.format("%-10s %-15s %-20s %-20s %-10s",
                "Emp No", "First Name", "Last Name", "Title", "Salary"));

        // Loop over all employees in the list
        for (Employee emp : employees) {
            if (emp == null) continue;
            String emp_string = String.format("%-10s %-15s %-20s %-20s %-10s",
                    emp.emp_no, emp.first_name, emp.last_name, emp.title, emp.salary);
            System.out.println(emp_string);
        }
    }
}
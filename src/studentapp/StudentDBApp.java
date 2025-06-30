package studentapp;

import java.sql.*;
import java.util.*;
import java.io.*;

public class StudentDBApp {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/studentdb";
        String user = "root";
        String password = "Ramya000."; 

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("✅ Connected to MySQL Database!");

            while (true) {
                System.out.println("\n📘 STUDENT DATABASE MENU");
                System.out.println("1️⃣ Add Student");
                System.out.println("2️⃣ View All Students");
                System.out.println("3️⃣ Update Student");
                System.out.println("4️⃣ Delete Student");
                System.out.println("5️⃣ Total Student Count");
                System.out.println("6️⃣ Search Student");
                System.out.println("7️⃣ Export to File");
                System.out.println("8️⃣ Clear Console");
                System.out.println("9️⃣ Exit");
                System.out.print("👉 Enter your choice: ");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1 -> addStudent(conn, scanner);
                    case 2 -> viewStudents(conn);
                    case 3 -> updateStudent(conn, scanner);
                    case 4 -> deleteStudent(conn, scanner);
                    case 5 -> showTotalCount(conn);
                    case 6 -> searchStudent(conn, scanner);
                    case 7 -> exportToFile(conn);
                    case 8 -> clearConsole();
                    case 9 -> {
                        System.out.println("👋 Exiting...");
                        return;
                    }
                    default -> System.out.println("❌ Invalid choice.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }
    }

    public static void addStudent(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter student name: ");
            String name = scanner.next();
            
            System.out.print("Enter student age: ");
            int age = scanner.nextInt();

            scanner.nextLine(); // Clear the newline

            System.out.print("Enter student email: ");
            String email = scanner.nextLine();

            System.out.print("Enter student course: ");
            String course = scanner.nextLine();

            String sql = "INSERT INTO students (name, age, email, course) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, email);
            pstmt.setString(4, course);

            pstmt.executeUpdate();
            System.out.println("✅ Student added successfully!");
        } catch (Exception e) {
            System.out.println("❌ Error occurred while adding student.");
            e.printStackTrace();
        }
    }

    public static void viewStudents(Connection conn) {
        String query = "SELECT * FROM students";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("📋 Student Records:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String email = rs.getString("email");
                String course = rs.getString("course");

                System.out.println("ID: " + id + " | Name: " + name + " | Age: " + age + 
                                   " | Email: " + email + " | Course: " + course);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving data.");
            e.printStackTrace();
        }
    }

    public static void updateStudent(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter student ID to update: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // consume newline

            System.out.print("Enter new name: ");
            String name = scanner.nextLine();

            System.out.print("Enter new age: ");
            int age = scanner.nextInt();
            scanner.nextLine(); // consume newline

            System.out.print("Enter new email: ");
            String email = scanner.nextLine();

            System.out.print("Enter new course: ");
            String course = scanner.nextLine();

            String query = "UPDATE students SET name=?, age=?, email=?, course=? WHERE id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, email);
                pstmt.setString(4, course);
                pstmt.setInt(5, id);

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("✅ Student updated successfully!");
                } else {
                    System.out.println("⚠️ No student found with that ID.");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error occurred while updating.");
            e.printStackTrace();
        }
    }


    public static void deleteStudent(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter student ID to delete: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // consume newline

            // Confirm deletion
            System.out.print("Are you sure you want to delete this student? (yes/no): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (confirmation.equals("yes")) {
                String query = "DELETE FROM students WHERE id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, id);

                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        System.out.println("🗑️ Student deleted successfully!");
                    } else {
                        System.out.println("⚠️ No student found with that ID.");
                    }
                }
            } else {
                System.out.println("❌ Deletion cancelled.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error occurred while deleting.");
            e.printStackTrace();
        }
    }

    public static void showTotalCount(Connection conn) {
        try {
            String query = "SELECT COUNT(*) AS total FROM students";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("📊 Total students: " + total);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching total count.");
            e.printStackTrace();
        }
    }
    public static void searchStudent(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter student ID or Name to search: ");
            String input = scanner.nextLine().trim();

            String query;
            PreparedStatement pstmt;

            if (input.matches("\\d+")) {
                // Input is all digits: treat as ID
                query = "SELECT * FROM students WHERE id = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, Integer.parseInt(input));
            } else {
                // Otherwise: treat as name (partial match allowed)
                query = "SELECT * FROM students WHERE LOWER(name) LIKE ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, "%" + input.toLowerCase() + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            boolean found = false;

            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String email = rs.getString("email");
                String course = rs.getString("course");

                System.out.println("🔍 ID: " + id + " | Name: " + name + " | Age: " + age +
                                   " | Email: " + email + " | Course: " + course);
            }

            if (!found) {
                System.out.println("❌ No student found with that ID or Name.");
            }

            rs.close();
            pstmt.close();
        } catch (Exception e) {
            System.out.println("❌ Error occurred while searching.");
            e.printStackTrace();
        }
    }


    public static void exportToFile(Connection conn) {
        try {
            String query = "SELECT * FROM students";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            FileWriter writer = new FileWriter("student_records.txt");
            while (rs.next()) {
                writer.write("ID: " + rs.getInt("id") +
                             ", Name: " + rs.getString("name") +
                             ", Age: " + rs.getInt("age") + "\n");
            }
            writer.close();
            System.out.println("📝 Exported successfully to student_records.txt");
        } catch (IOException | SQLException e) {
            System.out.println("❌ Error exporting to file.");
            e.printStackTrace();
        }
    }

    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("❌ Could not clear console.");
        }
    }

}




/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mysqljdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.Statement;
/**
 *
 * @author Administrator
 */
public class MYSQLJDBC {

    /**
     * @param args the command line arguments
     */
    public static void writeBlob(int candidateId, String filename) {
        // update sql
        String updateSQL = "UPDATE candidates "
                + "SET resume = ? "
                + "WHERE id=?";
 
        try (Connection conn = MySQLJDBCUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
 
            // read the file
            File file = new File(filename);
            FileInputStream input = new FileInputStream(file);
 
            // set parameters
            pstmt.setBinaryStream(1, input);
            pstmt.setInt(2, candidateId);
 
            // store the resume file in database
            System.out.println("Reading file " + file.getAbsolutePath());
            System.out.println("Store file in the database.");
            pstmt.executeUpdate();
 
        } catch (SQLException | FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void getSkills(int candidateId) {
        // 
        String query = "{ call get_candidate_skill(?) }";
        ResultSet rs;
 
        try (Connection conn = MySQLJDBCUtil.getConnection();
                CallableStatement stmt = conn.prepareCall(query)) {
 
            stmt.setInt(1, candidateId);
 
            rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println(String.format("%s - %s",
                        rs.getString("first_name") + " "
                        + rs.getString("last_name"),
                        rs.getString("skill")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
    public static void addCandidate(String firstName,String lastName,Date dob, 
                                    String email, String phone, int[] skills) {
        
        Connection conn = null;
        
        // for insert a new candidate
        PreparedStatement pstmt = null;
        
        // for assign skills to candidate
        PreparedStatement pstmtAssignment = null;
        
        // for getting candidate id
        ResultSet rs = null;
 
        try {
            conn = MySQLJDBCUtil.getConnection();
            // set auto commit to false
            conn.setAutoCommit(false);
            // 
            // Insert candidate
            // 
            String sqlInsert = "INSERT INTO candidates(first_name,last_name,dob,phone,email) "
                              + "VALUES(?,?,?,?,?)";
            
            pstmt = conn.prepareStatement(sqlInsert,Statement.RETURN_GENERATED_KEYS);
 
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setDate(3, dob);
            pstmt.setString(4, phone);
            pstmt.setString(5, email);
 
            int rowAffected = pstmt.executeUpdate();
            
             // get candidate id
            rs = pstmt.getGeneratedKeys();
            int candidateId = 0;
            if(rs.next())
                candidateId = rs.getInt(1);
            //    
            // in case the insert operation successes, assign skills to candidate
            //
            if(rowAffected == 1)
            {
                // assign skills to candidates
                String sqlPivot = "INSERT INTO candidate_skills(candidate_id,skill_id) "
                                 + "VALUES(?,?)";
                
                pstmtAssignment = conn.prepareStatement(sqlPivot);
                for(int skillId : skills) {
                    
                    pstmtAssignment.setInt(1, candidateId);
                    pstmtAssignment.setInt(2, skillId);
                    
                    pstmtAssignment.executeUpdate();
                }
                conn.commit();
            } else {
                conn.rollback();
            }
        } catch (SQLException ex) {
            // roll back the transaction
            try{
                if(conn != null)
                    conn.rollback();
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            
            
            System.out.println(ex.getMessage());
        } finally {
            try {
                if(rs != null)  rs.close();
                if(pstmt != null) pstmt.close();
                if(pstmtAssignment != null) pstmtAssignment.close();
                if(conn != null) conn.close();
                
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public static int insertCandidate(String firstName,String lastName,Date dob, 
                                       String email, String phone) {
        // for insert a new candidate
        ResultSet rs = null;
        int candidateId = 0;
        
        String sql = "INSERT INTO candidates(first_name,last_name,dob,phone,email) "
                   + "VALUES(?,?,?,?,?)";
        
        try (Connection conn = MySQLJDBCUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);) {
            
            // set parameters for statement
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setDate(3, dob);
            pstmt.setString(4, phone);
            pstmt.setString(5, email);
 
            int rowAffected = pstmt.executeUpdate();
            if(rowAffected == 1)
            {
                // get candidate id
                rs = pstmt.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
 
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if(rs != null)  rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        
        return candidateId;
    }
    public static void update() {
 
        String sqlUpdate = "UPDATE candidates "
                + "SET last_name = ? "
                + "WHERE id = ?";
 
        try (Connection conn = MySQLJDBCUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
 
            // prepare data for update
            String lastName = "William";
            int id = 100;
            pstmt.setString(1, lastName);
            pstmt.setInt(2, id);
 
            int rowAffected = pstmt.executeUpdate();
            System.out.println(String.format("Row affected %d", rowAffected));
 
            // reuse the prepared statement
            lastName = "Grohe";
            id = 101;
            pstmt.setString(1, lastName);
            pstmt.setInt(2, id);
 
            rowAffected = pstmt.executeUpdate();
            System.out.println(String.format("Row affected %d", rowAffected));
 
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
        String sql = "SELECT first_name, last_name, email " +
                     "FROM candidates";
        
        try (Connection conn = MySQLJDBCUtil.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
           
            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getString("first_name") + "\t" + 
                                   rs.getString("last_name")  + "\t" +
                                   rs.getString("email"));
                    
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        
        update();
        
        int id = insertCandidate("Bush", "Lily", Date.valueOf("1980-01-04"), 
                        "bush.l@yahoo.com", "(408) 898-6666");
         
         System.out.println(String.format("A new candidate with id %d has been inserted.",id));
         
        int[] skills = {1,2,3};
        addCandidate("John", "Doe", Date.valueOf("1990-01-04"), 
                        "john.d@yahoo.com", "(408) 898-5641", skills);
        
        getSkills(122);
        */
        writeBlob(122, "johndoe_resume.pdf");
 
    }
    
}

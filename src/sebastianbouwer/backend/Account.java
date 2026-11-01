/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */
public class Account {
    private String userID, username, password, joinDate;
    private boolean isAdmin;
    
    public Account(String sUI, String sU, String sP, String sJD, boolean sIA) {
        userID = sUI;
        username = sU;
        password = sP;
        joinDate = sJD;
        isAdmin = sIA;
    }
    
    public String getUserID() {
        return userID;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getJoinDate() {
        return joinDate;
    }
    public boolean getAdmin() {
        return isAdmin;
    }
    public void setPassword(String pass) {
    password = pass;
    }
    public void setUsername(String user) {
        username = user;
    }
}

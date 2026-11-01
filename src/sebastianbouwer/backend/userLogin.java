/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import sebastianbouwer.backend.Account;

public class userLogin { 
    private AccountManager manager;
    
    public userLogin() {
        manager = new AccountManager();
    }
    
    public boolean registerAcc(String username, String password) {
        if (manager.searchAccUser(username) != null) {//if username already exists
            return false; //say no i wont make this account
        } 
        
        String newID = UUID.randomUUID().toString();//creates a random string userID for the account
        String joinDate = LocalDate.now().toString();
        String theme = "Dark", reminderTime = "20:00", localSavePath = "", onlineSavePath = ""; 
        int maxCacheSize = 200; //Mb
        boolean notifications = true;
        Account newAcc = new Account(newID, username, password, joinDate, false, 
            theme, reminderTime, localSavePath, onlineSavePath, notifications, maxCacheSize);
        manager.addAcc(newAcc);
        return true;//say yes i will make this account
    }
    
    public Account loginUser(String username, String password) {
        Account acc = manager.searchAccUser(username);
        if (acc != null /*does account exist*/ && acc.getPassword().equals(password)) {
            return acc;
        }
        else {
            System.out.println("wrong username or password");
        }
        return null;
    }
}

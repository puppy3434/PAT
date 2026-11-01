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
    
    //settings
    private String theme, reminderTime, localSavePath, onlineSavePath;
    private boolean notifications;
    private int maxCacheSize;
    
    public Account(String sUI, String sU, String sP, String sJD, boolean sIA, 
            /*settings*/ String sT, String sRT, String sLSP, String sOSP, boolean bN, int iMCS) {
        userID = sUI;
        username = sU;
        password = sP;
        joinDate = sJD;
        isAdmin = sIA;
        theme = sT;
        reminderTime = sRT;
        localSavePath = sLSP;
        onlineSavePath = sOSP;
        notifications = bN;
        maxCacheSize = iMCS;
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

    public String getTheme() {
        return theme;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public String getLSavePath() {
        return localSavePath;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public void setSavePath(String savePath) {
        localSavePath = savePath;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }
    
    /*public void def() {
        isAdmin = false;
        theme = "Light";
        reminderTime = "20:00";
        localSavePath = "";//still figure out
        onlineSavePath = "";//still figure out
        notifications = true;
        maxCacheSize = 200;//Mb 
    }*/

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
import sebastianbouwer.backend.*;
import com.google.gson.*;
import java.util.*;
import java.io.*;
import java.time.*;

public class StrikerManager {
    private List<strike> strikes;
    private final String FILEPATH = "";//idk
    private final Gson gson;
    
    public StrikerManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        strikes = new ArrayList<>();
    }
    
    private void readStrikes() {
        try (Reader read = new FileReader(FILEPATH)) {
            strike[] arrStrikes = gson.fromJson(read, strike[].class);
            if (arrStrikes != null) {
                strikes = new ArrayList<>(Arrays.asList(arrStrikes));
            }
        }
        catch(FileNotFoundException e) {
            System.out.println("could not find strikes " + e.getMessage());
            try (Writer write = new FileWriter(FILEPATH)) {
                write.write("[]");
                System.out.println("made strikes file!!!!!");
            }
            catch(IOException er) {
                System.out.println("could not create strikes " + er.getMessage());
            }
        }
        catch (IOException e) {
            System.out.println("could not read strikes " + e.getMessage());
        }
    }
    
    public void saveStrike() {
        try (Writer write = new FileWriter(FILEPATH)) {
           gson.toJson(strikes, write);
        }
        catch (IOException e) {
            System.out.println("could not save strikes " + e.getMessage());
        }
    }
    
    public void issueStrike(String userID, String reason, int punishmentID, QueueManager qM) {
        strike newStrike = new strike(userID, reason);
        strikes.add(newStrike);
        saveStrike();
        qM.addPunishment(userID, punishmentID);
    }
    
    public void dailyCheck(QueueManager qM, int punishmentID) {
        Map<String, List<QueueItem>> all = qM.getAllQueues();
        String today = LocalDate.now().toString();
        List<String> userID = new ArrayList<>(all.keySet());
        for (int i = 0; i < userID.size(); i++) {
            String user = userID.get(i);
            List<QueueItem> list = all.get(user);
            for (int k = 0; k < list.size(); k++) {
                QueueItem item = list.get(k);
                if (item.getStatus().equalsIgnoreCase("watching")) {
                    if (!item.getLastLogDate().equals(today)) {
                        issueStrike(user, "Missed logging progress", punishmentID, qM);
                        item.setLastLogDate(today);
                    } 
                    
                }
            }
            
        }
        qM.saveQueue();
    }
    
    public static class strike {
        private String userID, dateIssued, reason;
        private boolean isVoided;
        
        public strike(String inUID, String inR) {
            userID = inUID;
            reason = inR;
            dateIssued = LocalDate.now().toString();
            isVoided = false;//doesnt auto void punishment
        }
        public String getUserID() {
            return userID;
        }
        public String getDateIssued() {
            return dateIssued;
        }
        public String getReason() {
            return reason;
        }
        public boolean getVoided() {
            return isVoided;
        }
        public void setVoided(boolean voided) {
            isVoided = voided;
        }
    } 
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
import com.google.gson.*;
import sebastianbouwer.backend.*;
import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class RollManager {
    private final Random RANDOM;
    private List<RollRecord> history;
    private final Gson gson;//creates gson object so i cant accidentally overwrite it and ruin everything
    private final String historyFilePath = "src/roll_history.json";
    
    public RollManager() {
        RANDOM = new Random();
        gson = new GsonBuilder().setPrettyPrinting().create();
        history = new ArrayList<>();
        readHistory();
    }
    
    public List<show> roll(String targetUserID, int batchCount, String mode, boolean isRandomRoll,
            List<show> masterList, QueueManager qM, AniListAPI api) {
        List<show> rolled = new ArrayList<>();
        int batch = Math.abs(batchCount);//how limit min/max; abs ensures no negative i think
            if (isRandomRoll) {
                for (int i = 0; i < batch; i++) {
                    show random = api.getRandomAnime();
                    if (random != null){
                        qM.addToQueue(targetUserID, random.getAnilistID());
                        addHistoryRecord(new RollRecord(targetUserID, random.getAnilistID(), random.getEngTitle(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()),
                                "Random"));
                        rolled.add(random);
                    }
                }
                return rolled;
            }
            List<show> pool = new ArrayList<>();
            List<QueueItem> queue = qM.getUserQueue(targetUserID);
            for (int i = 0; i < masterList.size(); i++) {//make sure no dupe
                show show = masterList.get(i);
                boolean inQ = false;
                for (int k = 0; k < queue.size(); k++) {
                    if (queue.get(k).getAnilistID() == show.getAnilistID()) {
                        inQ = true;
                        break;
                    }
                }
                if (!inQ) {
                    pool.add(show);
                }
            }
            //roll from pool list
            for (int i = 0; i < batch; i++) {
                double weight = 0.00;
                for (int k = 0; k < pool.size(); k++) {
                    weight += weight(pool.get(k), mode);
                }
                double random = RANDOM.nextDouble() * weight;
                double currentWeight = 0.00;
                for (int k = 0; k < pool.size(); k++) {
                    show show = pool.get(k);
                    currentWeight += weight(show, mode);
                    if (currentWeight > random) {
                        rolled.add(show);
                        pool.remove(k);//no duplicate in batch because we steal this away from batch
                        qM.addToQueue(targetUserID, show.getAnilistID());
                        addHistoryRecord(new RollRecord(targetUserID, show.getAnilistID(), show.getEngTitle(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()),
                                "Master List"));
                        break;
                }
            }
        }
            return rolled;
    }
    
    private void readHistory() {
        try (Reader read = new FileReader(historyFilePath)) {
            RollRecord[] loaded = gson.fromJson(read, RollRecord[].class);
            if (loaded != null) {
                history = new ArrayList<>(Arrays.asList(loaded));
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("creating writer " + e.getMessage());
        }
        catch(IOException e) {
            System.out.println("couldnt read history " + e.getMessage());
        }
    }
    
    private void addHistoryRecord(RollRecord record) {
        history.add(record);
        try (Writer write = new FileWriter(historyFilePath)) {
            gson.toJson(history, write);
        }
        catch(IOException e) {
            System.out.println("error adding to history " + e.getMessage());
        }
    }
    
    public List<RollRecord> getUserHistory(String userID) {
        List<RollRecord> history = new ArrayList<>();
        for (int i = 0; i < this.history.size(); i++) {
            if (this.history.get(i).getUserID().equals(userID)) {
                history.add(this.history.get(i));
            } 
       }
        return history;
    }
    
    public class RollRecord {//cool nested class ngl
        private String userID, engTitle, dateRolled, rollType;
        private int anilistID;
        
        public RollRecord(String sUI, int sAID, String sET, String sDR, String sRT) {
            userID = sUI;
            anilistID = sAID;
            engTitle = sET;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateRolled = dtf.format(LocalDateTime.now());
            rollType = sRT;
        }
        public String getUserID() {
            return userID;
        }
        public int getAnilistID() {
            return anilistID;
        }
        public String getEngTitle() {
            return engTitle;
        }
        public String getDateRolled() {
            return dateRolled;
        }
        public String getRollType() {
            return rollType;
        }
        
    }
    
    public double weight(show show, String mode) {
        double rating = show.getRating();
        if (rating <= 0) {
            rating = 1.0;
        }
        switch (mode) {
            case "higher": return Math.pow(rating, 2);//higher rating = higher weighting
            case "lower": return Math.pow(11-rating, 2);//lower rating = higher weighting
            case "unweighted": return 1.00;
            default: return 1.0;
        }
    }
}
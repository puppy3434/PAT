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
    
    public List<RollResult> roll(String targetUserID, int batchCount, String mode, boolean isRandomRoll,
            List<show> masterList, QueueManager qM, AniListAPI api) {
        List<RollResult> rolled = new ArrayList<>();
        int batch = Math.abs(batchCount);//how limit min/max; abs ensures no negative i think
            if (isRandomRoll) {
                for (int i = 0; i < batch; i++) {
                    show random = api.getRandomAnime();
                    if (random != null){
                        List<show> franchise = addFranchiseToQueue(random, masterList, api, targetUserID, qM);
                        rolled.add(new RollResult(random, franchise));
                        addHistoryRecord(new RollRecord(targetUserID, random.getAnilistID(), random.getEngTitle(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()),
                                "Random"));
                    }
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        
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
                        pool.remove(k);//no duplicate in batch because we steal this away from batch
                        List<show> franchise = addFranchiseToQueue(show, masterList, api, targetUserID, 
                                qM);
                        rolled.add(new RollResult(show, franchise));
                        addHistoryRecord(new RollRecord(targetUserID, show.getAnilistID(), show.getEngTitle(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()),
                                "Master List"));
                        break;
                }
            }
        }
            return rolled;
    }
    
    private List<show> addFranchiseToQueue(show base, List<show> masterList, AniListAPI api, 
            String userID, QueueManager qM){
        List<show> franchise = new ArrayList<>();
        franchise.add(base); // Add the rolled show first
        java.util.Map<String, List<show>> relations = api.getRelatedShows(base.getAnilistID());
        String[] valid = {"SEQUEL", "PREQUEL", "SIDE_STORY", "SPIN_OFF", "PARENT"};
        for (int i = 0; i < valid.length; i++) {
            String type = valid[i];
            if (relations.containsKey(type)) {
                List<show> shows = relations.get(type);
                for (int k = 0; k < shows.size(); k++) {
                    show relatedShow = shows.get(k);
                    for (int j = 0; j < masterList.size(); j++) {
                        if (masterList.get(j).getAnilistID() == relatedShow.getAnilistID()) {
                            boolean alreadyInList = false;
                            for (int f = 0; f < franchise.size(); f++) {
                                if (franchise.get(f).getAnilistID() == relatedShow.getAnilistID()) {
                                    alreadyInList = true;
                                    break;
                                }
                            }
                            
                            if (!alreadyInList) {
                                franchise.add(relatedShow);
                            }
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < franchise.size() - 1; i++) {
            for (int k = 0; k < franchise.size() - i - 1; k++) {
                if (franchise.get(k).getReleaseYear() > franchise.get(k + 1).getReleaseYear()) {
                    show temp = franchise.get(k);
                    franchise.set(k, franchise.get(k + 1));
                    franchise.set(k + 1, temp);
                }
            }
        }
        for (int i = 0; i < franchise.size(); i++) {
            show fShow = franchise.get(i);
            qM.addToQueue(userID, fShow.getAnilistID());
            if (fShow.getAnilistID() != base.getAnilistID()) {
                addHistoryRecord(new RollRecord(userID, fShow.getAnilistID(), fShow.getEngTitle(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()),
                        "Franchise Chain"));
            }
        }
        return franchise;
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
            try (Writer write = new FileWriter(historyFilePath)) {
                write.write("[]");
                System.out.println("Made roll_history.json");
            }
            catch (IOException er) {
                System.out.println("could not make history " + er.getMessage());
            }
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
    
    public static class RollRecord {//cool nested class ngl
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
    
    public static class RollResult {
        public show baseShow;
        public List<show> franchiseShows;
        
        public RollResult(show base, List<show> franchise) {
            baseShow = base;
            franchiseShows = franchise;
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
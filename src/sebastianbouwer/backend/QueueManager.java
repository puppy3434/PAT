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
import com.google.gson.reflect.TypeToken;
import sebastianbouwer.backend.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.time.LocalDate;
public class QueueManager {
    private Map<String, List<QueueItem>> queue;
    private final String filePath = "src/queus.json";
    private final Gson gson;
    
    public QueueManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        queue = new HashMap<>();
        readQueue();
    }
    
    private void readQueue() {
        try (Reader read = new FileReader(filePath)) {
            Type mapType = new TypeToken<HashMap<String, List<QueueItem>>>(){}.getType();
            Map<String, List<QueueItem>> load = gson.fromJson(read, mapType);
            if (load != null) {
                queue = load;
            }
        }
        catch (FileNotFoundException e) {
            try (Writer write = new FileWriter(filePath)) {
                write.write("{}");
            }
            catch (IOException er) {
                System.out.println("error making queue.json" + er.getMessage());
            }
        }
        catch (IOException e) {
            System.out.println("error reading queue");
        }
    }
    
    public void saveQueue() {
        try (Writer write = new FileWriter(filePath)) {
            gson.toJson(queue, write);
        }
        catch (IOException e) {
            System.out.println("error saving queue.json " + e.getMessage());
        }
    }
    public void addToQueue(String userID, int anilistID) {
        List<QueueItem> list = getUserQueue(userID);
        for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getAnilistID() ==  anilistID) {
                    return;
                }
            }
            
            list.add(new QueueItem(userID, anilistID));
            saveQueue();
        }
    
    public void logProgress(String userID, int anilistID, int episodesAdded) {
        List<QueueItem> list = getUserQueue(userID);
        for (int i = 0; i < list.size(); i++) {
            QueueItem item = list.get(i);
            if (item.getAnilistID() == anilistID) {
                String today = LocalDate.now().toString();
                if (!item.getLastLogDate().equals(today)) {
                    item.setEpisodesWatchedToday(0);
                    item.setLastLogDate(today);
                }
                item.setEpisodesWatchedToday(item.getEpisodesWatchedToday() + episodesAdded);
                item.setCurrentEpisode(item.getCurrentEpisode() + episodesAdded);
                item.setStatus("watching");
                saveQueue();
                return;
            }
        }
    }
    
    public void removeFromQueue(String userID, int anilistID) {
        List<QueueItem> list = getUserQueue(userID);
        for(int i = 0; i < list.size(); i++) {
            QueueItem item = list.get(i);
            if (item.getUserID().equals(userID) && item.getAnilistID() == anilistID) {
                list.remove(i);
                saveQueue();
                return; 
            }
        }
        
    }
    
    public void addPunishment(String userID, int anilistID) {
        List<QueueItem> list = getUserQueue(userID);
        QueueItem punishment = new QueueItem(userID, anilistID);
        punishment.setStatus("pending");
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getStatus().equals("watching")) {//adds punishemt after the show you are busy watching
                index = i + 1;
                break;
            }
        }
        list.add(index, punishment);
        saveQueue();
    }
    
    public List<QueueItem> getUserQueue(String userID) {
        if (!queue.containsKey(userID)) {
            queue.put(userID, new ArrayList<>());
        }
        return queue.get(userID);
    }
    
    public Map<String, List<QueueItem>> getAllQueues() {
        return queue;
    }
}

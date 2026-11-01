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
    private final String filePath = "src/queues.json";
    private final Gson gson;

    public QueueManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        queue = new HashMap<>();
        readQueue();
    }

    private void readQueue() {
        try (Reader read = new FileReader(filePath)) {
            Type mapType = new TypeToken<HashMap<String, List<QueueItem>>>() {
            }.getType();
            Map<String, List<QueueItem>> load = gson.fromJson(read, mapType);
            if (load != null) {
                queue = load;
            }
        } catch (FileNotFoundException e) {
            try (Writer write = new FileWriter(filePath)) {
                write.write("{}");
            } catch (IOException er) {
                System.out.println("error making queue.json" + er.getMessage());
            }
        } catch (IOException e) {
            System.out.println("error reading queue");
        }
    }

    public void saveQueue() {
        try (Writer write = new FileWriter(filePath)) {
            gson.toJson(queue, write);
        } catch (IOException e) {
            System.out.println("error saving queue.json " + e.getMessage());
        }
    }
/**
     * Adds a show to a user's queue. If they have no "watching" show, it automatically becomes "watching".
     * 
     * @param userID The ID of the user.
     * @param anilistID The ID of the show to add.
     */
    public void addToQueue(String userID, int anilistID) {
        List<QueueItem> list = getUserQueue(userID);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAnilistID() == anilistID) {
                return;
            }
        }
        QueueItem item = new QueueItem(userID, anilistID);
        boolean watching = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getStatus().equals("watching")) {
                watching = true;
                break;
            }
        }
        if (!watching) {
            item.setStatus("watching");
        }

        list.add(item);
        saveQueue();
    }
/**
     * Logs watched episodes for a specific show. Handles daily resets, auto-completing shows,
     * and automatically promoting the next "pending" show to "watching".
     * 
     * @param userID The ID of the user.
     * @param anilistID The ID of the show.
     * @param episodesAdded The number of episodes watched.
     * @param totalEps The total number of episodes in the show.
     */
    public void logProgress(String userID, int anilistID, int episodesAdded, int totalEps) {
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
                if (item.getCurrentEpisode() >= totalEps && totalEps > 0) {
                    item.setStatus("complete");
                    for (int k = 0; k < list.size(); k++) {
                        QueueItem nextItem = list.get(k);
                        if (nextItem.getStatus().equals("pending")) {
                            nextItem.setStatus("watching");
                            break;
                        }
                    }
                } else {
                    item.setStatus("watching");
                }
                saveQueue();
                return;
            }
        }
    }

    public void removeFromQueue(String userID, int anilistID) {
        List<QueueItem> list = getUserQueue(userID);
        for (int i = 0; i < list.size(); i++) {
            QueueItem item = list.get(i);
            if (item.getUserID().equals(userID) && item.getAnilistID() == anilistID) {
                list.remove(i);
                saveQueue();
                return;
            }
        }

    }
  /**
     * Adds a punishment show to the queue right after their currently watching show.
     * 
     * @param userID The ID of the user.
     * @param anilistID The ID of the punishment show.
     */
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

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sebastianbouwer.backend.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShowManager {
    private List<show> shows;
    private List<episode> episodeCache;
    private final String sFilePath = "src/shows.json";
    private final String eFilePath = "src/episodes_cache.json";
    private final Gson gson;
    
    public ShowManager() {
      gson = new GsonBuilder().setPrettyPrinting().create();
      shows = new ArrayList<>();
      episodeCache = new ArrayList<>();
    }
    
    private void readShow() {
       File file = new File(sFilePath);
        try (Reader reader = new FileReader(sFilePath)){//gson only accepts a reader and not scanner :(
            show[] arrShow = gson.fromJson(reader, show[].class);
            if (arrShow != null) {
                shows = new ArrayList<>(java.util.Arrays.asList(arrShow));//populates shows list
            }
        }
        catch(FileNotFoundException e) {
            try (Writer write = new FileWriter(sFilePath)) {
                write.write("[]");
                System.out.println("shows.json was missing so i made another one");
            }
            catch(IOException d) {
                System.out.println("error idk " + d.getMessage());
            }
        }
        catch(IOException e) {//reader wants an IOException for some reason and i dont want to throw it
            System.out.println("completele failure (to read) " + e.getMessage());
        } 
    }
    
    public void saveShow() {
        try (Writer writer = new FileWriter(sFilePath)) {
            gson.toJson(shows, writer);
        }
        catch (IOException e) {
            System.out.println("Error Saving " + e.getMessage());
        }
    }
    
    public void addShow(show newShow) {
        shows.add(newShow);
        saveShow();
    }
    
    public boolean showExists(int aniListID, int malID, String engName, String forName) {
        for (int i = 0; i < shows.size(); i++) {
            if (shows.get(i).getAnilistID() == aniListID
                    || shows.get(i).getMalID() == malID
                    || shows.get(i).getEngTitle().equalsIgnoreCase(engName)
                    || shows.get(i).getForTitle().equalsIgnoreCase(forName)) {
                return true;
            }
        }
        return false;
    }
    
    private void readEpisodes() {
        try (Reader read = new FileReader(eFilePath)) {
            episode[] arrEpisodes = gson.fromJson(read, episode[].class);
            if (arrEpisodes != null) {
                episodeCache = new ArrayList<>(Arrays.asList(arrEpisodes));
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("creating cache " + e.getMessage());
            try (Writer write = new FileWriter(eFilePath)) {
                write.write("[]");
                System.out.println("created ep cache!");
            }
            catch(IOException er) {
                System.out.println("error creating cache " + e.getMessage());
            }
        }
        catch(IOException e) {
            System.out.println("error reading ep cache " + e.getMessage());
        }
    }
    
    private void saveEpisodes() {
        try (Writer write = new FileWriter(eFilePath)) {
            gson.toJson(episodeCache, write);
        }
        catch (IOException e) {
            System.out.println("could not save episodes " + e.getMessage());
        }
    }
    
    public void cacheEpisodes(List<episode> newEpisodes) {
        episodeCache.addAll(newEpisodes);
        saveEpisodes();
    }
    
    public List<episode> getCachedShowEps(int aniListID, int malID, String engName, String forName) {
        List<episode> arrEpisodes = new ArrayList<>();
        for (int i = 0; i < episodeCache.size(); i++) {
            if (shows.get(i).getAnilistID() == aniListID
                    || shows.get(i).getMalID() == malID
                    || shows.get(i).getEngTitle().equalsIgnoreCase(engName)
                    || shows.get(i).getForTitle().equalsIgnoreCase(forName)) {
                arrEpisodes.add(episodeCache.get(i));
            }
        }
        return arrEpisodes;
    }
    
    public boolean hasCache(int aniListID, int malID, String engName, String forName) {
        for (int i = 0; i < episodeCache.size(); i++) {
            if (shows.get(i).getAnilistID() == aniListID
                    || shows.get(i).getMalID() == malID
                    || shows.get(i).getEngTitle().equalsIgnoreCase(engName)//if null maybe crash idk
                    || shows.get(i).getForTitle().equalsIgnoreCase(forName)) {
                return true;
            }
        }
        return false;
    }
}

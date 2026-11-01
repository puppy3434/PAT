/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
import java.time.LocalDate;

public class QueueItem {

    private String userID, lastLogDate, status;
    private int anilistID, currentEpisode, episodesWatchedToday;

    public QueueItem(String sUID, int iAID) {
        userID = sUID;
        anilistID = iAID;
        currentEpisode = 0;
        episodesWatchedToday = 0;
        lastLogDate = LocalDate.now().toString();
        status = "pending";
    }

    public String getUserID() {
        return userID;
    }

    public String getLastLogDate() {
        return lastLogDate;
    }

    public String getStatus() {
        return status;
    }

    public int getAnilistID() {
        return anilistID;
    }

    public int getCurrentEpisode() {
        return currentEpisode;
    }

    public int getEpisodesWatchedToday() {
        return episodesWatchedToday;
    }

    public void setLastLogDate(String lastLogDate) {
        this.lastLogDate = lastLogDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCurrentEpisode(int currentEpisode) {
        this.currentEpisode = currentEpisode;
    }

    public void setEpisodesWatchedToday(int episodesWatchedToday) {
        this.episodesWatchedToday = episodesWatchedToday;
    }

}

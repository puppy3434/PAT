/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
public class episode {
    private int episodeNum, seasonNum, anilistID;
    private String title, thumbnail, description;
    
    public episode(int iAID, int iEN, int iSN, String sT, String sTN, String sD) {
        anilistID = iAID;
        episodeNum = iEN;
        seasonNum = iSN;
        title = sT;
        thumbnail = sTN;
        description = sD;  
    }

    public int getEpisodeNum() {
        return episodeNum;
    }

    public int getSeasonNum() {
        return seasonNum;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getDescription() {
        return description;
    }
    
    public int getAnilistID() {
        return anilistID;
    }
}

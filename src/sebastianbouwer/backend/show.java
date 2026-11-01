/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
import java.util.List;
public class show {
    private int anilistID, malID, totalEpisodes, seasons, epSeason, releaseYear;
    private String engTitle, forTitle/*foreign lang title*/, format/*TV, Movie, OVA etc*/, 
            description, picture/*url of image as a string*/, status, addedBy; //userid
    private List<String> genres;
    private double rating;
    private List<Integer> seasonEpisodes; //stores num episodes per season, size is season count
    
    public show (int iAID, int iMID, String sET, String sFT, String sD, String sP, int iTE, List<Integer> iSE, 
            double dR, List<String> sG, String sF, String sS, String sAB, int rY) {
        
        anilistID = iAID;
        malID = iMID;
        engTitle = sET;
        forTitle = sFT;
        description = sD;
        picture = sP;
        totalEpisodes = iTE;
        seasonEpisodes = iSE;
        rating = dR;
        genres = sG;
        format = sF;
        addedBy = sAB;  
        status = sS;
        releaseYear = rY;
    }
    
    public int getAnilistID() {
        return anilistID;
    }
    public int getMalID() {
        return malID;
    }
    public String getEngTitle() {
        if (engTitle == null || engTitle.trim().isEmpty()) {
            return forTitle;
        }
        return engTitle;
    }
    public String getForTitle() {
        return forTitle;
    }
    public String getDescription() {
        return description;
    }
    public String getPicture() {
        return picture;
    }
    public int getTotalEpisodes() {
        return totalEpisodes;
    }
    public List<Integer> getSeasonEpisodes() {
        return seasonEpisodes;
    }
    public double getRating() {
        return rating;
    }
    public List<String> getGenres() {
        return genres;
    }
    public String getFormat() {
        return format;
    }
    public String getAddedBy() {
        return addedBy;
    }
    public String getStatus() {
        return status;
    }
    public int getReleaseYear() {
        return releaseYear;
    }
}

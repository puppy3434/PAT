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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*; //to access internet and use API from AniList, free and 90 requests per minute, normally but as of 30th oct its 30 per min because of stability issues
import java.nio.charset.StandardCharsets;
import java.util.*;
import static javax.swing.UIManager.get;
public class AniListAPI {//you get rate limit remaining after requests, and a 1 min timeout for exceeding, Retry-After is in seconds
    private final String API = "https://graphql.anilist.co";
    
public List<show> fetch(String search, Integer anilistID, Integer malID, String genre, 
                            Integer lowYear, Integer highYear, Double minRating, Double maxRating, 
                            Boolean isAdult, String format) {
        
        List<show> results = new ArrayList<>();

        // FIXED: Changed to startDate_greater and startDate_lesser
        String query = "query ($search: String, $id: Int, $idMal: Int, $genre: String, " +
                       "$startGreater: FuzzyDateInt, $startLesser: FuzzyDateInt, $minScore: Int, $maxScore: Int, " +
                       "$isAdult: Boolean, $format: MediaFormat) { " +
                       "  Page(page: 1, perPage: 30) { " +
                       "    media(search: $search, id: $id, idMal: $idMal, genre: $genre, " +
                       "          startDate_greater: $startGreater, startDate_lesser: $startLesser, " +
                       "          averageScore_greater: $minScore, averageScore_lesser: $maxScore, " +
                       "          isAdult: $isAdult, format: $format, type: ANIME) { " +
                       "      id idMal title { english romaji } description coverImage { large } " +
                       "      episodes averageScore genres format status " +
                       "    } " +
                       "  } " +
                       "}";

        JsonObject variables = new JsonObject();
        if (search != null && !search.isEmpty()) variables.addProperty("search", search);
        if (anilistID != null) variables.addProperty("id", anilistID);
        if (malID != null) variables.addProperty("idMal", malID);
        if (genre != null && !genre.isEmpty()) variables.addProperty("genre", genre);
        
        // FIXED: Convert years to FuzzyDateInt (YYYYMMDD)
        if (lowYear != null) variables.addProperty("startGreater", (lowYear * 10000) + 101); // e.g., 2020 -> 20200101
        if (highYear != null) variables.addProperty("startLesser", (highYear * 10000) + 1231); // e.g., 2024 -> 20241231
        
        if (minRating != null) variables.addProperty("minScore", (int)(minRating * 10));
        if (maxRating != null) variables.addProperty("maxScore", (int)(maxRating * 10));
        if (isAdult != null) variables.addProperty("isAdult", isAdult);
        if (format != null && !format.isEmpty()) variables.addProperty("format", format);

        String responseBody = sendGraphQLRequest(query, variables);

        if (responseBody != null) {
            try {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                    JsonArray mediaArray = jsonObject.getAsJsonObject("data")
                                                     .getAsJsonObject("Page")
                                                     .getAsJsonArray("media");

                    for (int i = 0; i < mediaArray.size(); i++) {
                        results.add(parseShowObject(mediaArray.get(i).getAsJsonObject()));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing JSON: " + e.getMessage());
            }
        }
        return results;
    }

    // NEW METHOD: Get related shows (Sequels, Prequels, etc.)
    public java.util.Map<String, List<show>> getRelatedShows(int anilistID) {
        java.util.Map<String, List<show>> relatedMap = new java.util.HashMap<>();
        
        String query = "query ($id: Int) { " +
                       "  Media(id: $id) { " +
                       "    relations { " +
                       "      edges { " +
                       "        relationType " +
                       "        node { " +
                       "          id idMal title { english romaji } description coverImage { large } " +
                       "          episodes averageScore genres format status type " +
                       "        } " +
                       "      } " +
                       "    } " +
                       "  } " +
                       "}";

        JsonObject variables = new JsonObject();
        variables.addProperty("id", anilistID);

        String responseBody = sendGraphQLRequest(query, variables);

        if (responseBody != null) {
            try {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                    JsonObject media = jsonObject.getAsJsonObject("data").getAsJsonObject("Media");
                    
                    if (media.has("relations") && !media.get("relations").isJsonNull()) {
                        JsonArray edges = media.getAsJsonObject("relations").getAsJsonArray("edges");
                        
                        for (int i = 0; i < edges.size(); i++) {
                            JsonObject edge = edges.get(i).getAsJsonObject();
                            String relType = edge.get("relationType").getAsString();
                            JsonObject node = edge.getAsJsonObject("node");
                            
                            // 1. Ensure it's an Anime (not a Manga adaptation)
                            if (!node.has("type") || !node.get("type").getAsString().equals("ANIME")) {
                                continue; 
                            }

                            show related = parseShowObject(node);
                            
                            // 2. Filter out MUSIC videos and SPECIALS
                            String format = related.getFormat();
                            if (format.equals("MUSIC")) {
                                continue;
                            }
                            
                            // 3. Add to the Map
                            if (!relatedMap.containsKey(relType)) {
                                relatedMap.put(relType, new ArrayList<>());
                            }
                            relatedMap.get(relType).add(related);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing Relations JSON: " + e.getMessage());
            }
        }
        return relatedMap;
    }

    public List<episode> getEpisodes(int anilistID) {
        List<episode> episodes = new ArrayList<>();
        
        String query = "query ($id: Int) { Media(id: $id) { streamingEpisodes { title thumbnail } } }";

        JsonObject variables = new JsonObject();
        variables.addProperty("id", anilistID);

        String responseBody = sendGraphQLRequest(query, variables);

        if (responseBody != null) {
            try {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                    JsonObject media = jsonObject.getAsJsonObject("data").getAsJsonObject("Media");
                    
                    if (media.has("streamingEpisodes") && !media.get("streamingEpisodes").isJsonNull()) {
                        JsonArray epArray = media.getAsJsonArray("streamingEpisodes");
                        for (int i = 0; i < epArray.size(); i++) {
                            JsonObject ep = epArray.get(i).getAsJsonObject();
                            String title = ep.has("title") && !ep.get("title").isJsonNull() 
                                    ? ep.get("title").getAsString() : "Episode " + (i + 1);
                            String thumb = ep.has("thumbnail") && !ep.get("thumbnail").isJsonNull() 
                                    ? ep.get("thumbnail").getAsString() : null;
                            
                            episode episode = new episode(anilistID, 1, i + 1, title, thumb, 
                                    "No description.");
                            episodes.add(episode);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing Episodes JSON: " + e.getMessage());
            }
        }
        return episodes;
    }
    
    public show getShowById(int anilistID) {
        String query = "query ($id: Int) { " +
                       "  Media(id: $id) { " +
                       "    id idMal title { english romaji } description coverImage { large } " +
                       "    episodes averageScore genres format status " +
                       "  } " +
                       "}";

        JsonObject variables = new JsonObject();
        variables.addProperty("id", anilistID);

        String responseBody = sendGraphQLRequest(query, variables);

        if (responseBody != null) {
            try {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                    JsonObject media = jsonObject.getAsJsonObject("data").getAsJsonObject("Media");
                    // Reuse our helper method!
                    return parseShowObject(media);
                }
            } catch (Exception e) {
                System.out.println("Error getting show by ID: " + e.getMessage());
            }
        }
        return null;
    }

    public List<show> getTrendingShows() {
        List<show> results = new ArrayList<>();
        
        // sort: TRENDING_DESC gets the hottest shows right now
        String query = "query { " +
                       "  Page(page: 1, perPage: 10) { " +
                       "    media(sort: TRENDING_DESC, type: ANIME) { " +
                       "      id idMal title { english romaji } description coverImage { large } " +
                       "      episodes averageScore genres format status " +
                       "    } " +
                       "  } " +
                       "}";

        JsonObject variables = new JsonObject(); // No variables needed here

        String responseBody = sendGraphQLRequest(query, variables);

        if (responseBody != null) {
            try {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                    JsonArray mediaArray = jsonObject.getAsJsonObject("data")
                                                     .getAsJsonObject("Page")
                                                     .getAsJsonArray("media");
                    for (int i = 0; i < mediaArray.size(); i++) {
                        results.add(parseShowObject(mediaArray.get(i).getAsJsonObject()));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error getting trending shows: " + e.getMessage());
            }
        }
        return results;
    }

    public List<String> getGenres() {
        List<String> genres = new ArrayList<>();
        String query = "query { GenreCollection }";

        JsonObject variables = new JsonObject();
        String responseBody = sendGraphQLRequest(query, variables);

        if (responseBody != null) {
            try {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                    JsonArray genreArray = jsonObject.getAsJsonObject("data").getAsJsonArray("GenreCollection");
                    for (int i = 0; i < genreArray.size(); i++) {
                        genres.add(genreArray.get(i).getAsString());
                    }
                }
            } catch (Exception e) {
                System.out.println("Error getting genres: " + e.getMessage());
            }
        }
        return genres;
    }

    private show parseShowObject(JsonObject media) {
        int id = media.get("id").getAsInt();
        int malId = media.has("idMal") && !media.get("idMal").isJsonNull() 
                ? media.get("idMal").getAsInt() : 0;
        
        JsonObject titleObj = media.getAsJsonObject("title");
        String engTitle = titleObj.has("english") && !titleObj.get("english").isJsonNull() 
                ? titleObj.get("english").getAsString() : null;
        String forTitle = titleObj.has("romaji") && !titleObj.get("romaji").isJsonNull() 
                ? titleObj.get("romaji").getAsString() : null;
        
        String desc = media.has("description") && !media.get("description").isJsonNull() 
                ? media.get("description").getAsString() : "No description.";
        String pic = media.getAsJsonObject("coverImage").get("large").getAsString();
        int eps = media.has("episodes") && !media.get("episodes").isJsonNull() 
                ? media.get("episodes").getAsInt() : 0;
        double rating = media.has("averageScore") && !media.get("averageScore").isJsonNull() 
                ? media.get("averageScore").getAsDouble() / 10.0 : 0.0;
        String formatStr = media.has("format") && !media.get("format").isJsonNull() 
                ? media.get("format").getAsString() : "UNKNOWN";
        String statusStr = media.has("status") && !media.get("status").isJsonNull() 
                ? media.get("status").getAsString() : "UNKNOWN";


        return new show(id, malId, engTitle, forTitle, desc, pic, eps, null,
                rating, new ArrayList<>(), formatStr, statusStr, "API");
    }

    private String sendGraphQLRequest(String query, JsonObject variables) {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("query", query);
            payload.add("variables", variables);

            URL url = java.net.URI.create(API).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), 
                        StandardCharsets.UTF_8));
                StringBuilder responseBody = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    responseBody.append(inputLine);
                }
                in.close();
                return responseBody.toString();
            } else {
                System.out.println("API Error: HTTP " + responseCode);
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    BufferedReader errReader = new BufferedReader(new InputStreamReader(errorStream, 
                            StandardCharsets.UTF_8));
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) {
                        System.out.println("Error details: " + errLine);
                    }
                    errReader.close();
                }
                return null;
            }
        } catch (Exception e) {
            System.out.println("Network Error: " + e.getMessage());
            return null;
        }
    }
    
    public show getRandomAnime() {
        int maxID = 200000; //placeholder
        String maxIDQuery = "query {Page(page: 1, perPage: 1) {media(sort: ID_DESC){ id })}}";
        String maxIDResponse = sendGraphQLRequest(maxIDQuery, new JsonObject());
        if (maxIDResponse != null) {
            try {
                JsonObject json = JsonParser.parseString(maxIDResponse).getAsJsonObject();
                maxID = json.getAsJsonObject("data").getAsJsonObject("Page").getAsJsonArray("media").get(0)
                        .getAsJsonObject().get("id").getAsInt();
            }
            catch (Exception e) {
                System.out.println("error rolling " + e.getMessage());
            }
            
            java.util.Random random = new java.util.Random();
            for (int attempts = 0; attempts < 50; attempts++) {//limits attempts to 50 for rate limiting
                int randomID = random.nextInt(maxID)+1;
                String query = "query ($id: Int) {Media(id: $id) {"
                        + "id idMal title { english romaji } description coverImage"
                        + "{ Large } episodes averageScore genres format status type } }";
                JsonObject variables = new JsonObject();
                variables.addProperty("id", randomID);
                String response = sendGraphQLRequest(query, variables);
                if (response != null) {
                    try {
                        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                        if (json.has("data") && !json.get("data").isJsonNull()) {
                            JsonObject media = json.getAsJsonObject("data").getAsJsonObject("Media");
                            if (media.has("type") && media.get("type").getAsString().equals("ANIME")) {//checks that its actually an anime
                                return parseShowObject(media);
                            }
                        }
                    }
                    catch (Exception e) {
                        System.out.println("error random " + e.getMessage());
                    }
                }
            }
        }
        return null;
    }
}


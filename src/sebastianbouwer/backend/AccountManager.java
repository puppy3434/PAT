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
import sebastianbouwer.backend.Account;
import java.io.*;
import java.util.*;
import java.lang.reflect.Type;

public class AccountManager {
    private List<Account> accounts;//dynamic array it seems accoring to google?
    private final Gson gson;
    private String filePath = "src/users.json";
    
    public AccountManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        accounts = new ArrayList<>();
        readAcc();
    }
    
    private void readAcc(){//reads account list and makes json
        File file = new File(filePath);
        try (Reader reader = new FileReader("src/users.json")){//gson only accepts a reader and not scanner :(
            Account[] arrAcc = gson.fromJson(reader, Account[].class);
            if (arrAcc != null) {
                accounts = new ArrayList<>(java.util.Arrays.asList(arrAcc));//populates accounts list
            }
        }
        catch(FileNotFoundException e) {
            try (Writer write = new FileWriter(filePath)) {
                write.write("[]");
                System.out.println("users.json was missing so i made another one");
            }
            catch(IOException d) {
                System.out.println("error idk " + d.getMessage());
            }
        }
        catch(IOException e) {//reader wants an IOException for some reason and i dont want to throw it
            System.out.println("completele failure (to read) " + e.getMessage());
        }
    }
    
    public void saveAcc() {//saves account list
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(accounts, writer);//saves accounts using writer to writers path (filePath)
        }
        catch(IOException e) {
            System.out.println("save error " + e.getMessage());
        }
    }
    
    public void addAcc(Account newAcc) {
        accounts.add(newAcc);
        saveAcc();
    }
    
    public Account searchAccID(String id) {//google says use a : but i dont trust that
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            if (acc.getUserID().equals(id)) {
                return acc;
            }
        }
        return null;
    }
    
    public Account searchAccUser(String username) {
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            if (acc.getUsername().equalsIgnoreCase(username)) {
                return acc;
            }
        }
        return null;
    }
    
    public List<Account> getAccounts() {
        return accounts;
    }
    
    public boolean changePassword(String userID, String oldPass, String newPass) {
        Account acc = searchAccID(userID);
        if (acc != null && acc.getPassword().equals(oldPass)) {
            acc.setPassword(newPass);
            saveAcc();
            return true;
        }
        return false;
    }
    
    public boolean changeUsername(String userID, String pass, String newUser) {
        Account acc = searchAccID(userID);
        if (acc != null && acc.getPassword().equals(pass)) {
            acc.setUsername(newUser);
            saveAcc();
            return true;
        }
        return false;  
    }
    
}

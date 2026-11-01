/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
import java.io.*;
import sebastianbouwer.backend.*;

public class GitManager {

    private String runCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File("."));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            System.out.println("error running git " + e.getMessage());
        }
        return "";
    }

    public boolean pullChanges() {
        System.out.println("Pulling");
        String result = runCommand("git", "pull", "origin", "main");
        System.out.println(result);
        return !result.contains("CONFLICT") && !result.contains("error")
                && !result.contains("fatal");
    }

    public boolean pushChanges(String commitMessage) {
        pullChanges();
        System.out.println("Pushing");
        runCommand("git", "add", "src/");
        String commitResult = runCommand("git", "commit", "-m", commitMessage);
        if (commitResult.contains("nothing to commit") || commitResult.contains("working tree clean")
                || commitResult.contains("No local changes to push")) {
            System.out.println("no local changes to push");
            return true;
        }
        String pushResult = runCommand("git", "push", "origin", "main");
        System.out.println(pushResult);
        if (pushResult.contains("Everything up-to-date")) {
            return true;
        }
        return !pushResult.contains("error") && !pushResult.contains("rejected") && !pushResult.contains("fatal");
    }

    public boolean isGitInit() {
        String result = runCommand("git", "status");
        return !result.contains("not a git repository");
    }
//no more test mm needed
}

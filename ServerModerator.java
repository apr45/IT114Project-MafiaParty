import java.net.*;
import java.io.*;
import java.util.*;

public class ServerModerator {
    // input and output streams
    static BufferedReader incomingStream = null;
    static PrintWriter outgoingStream = null;
    static String incomingText, outgoingText;

    // lists to store player information
    static ArrayList<String> usernames = new ArrayList<String>();
    static ArrayList<String> roles = new ArrayList<String>();
    static ArrayList<String> status = new ArrayList<String>();

    // variable to keep track of votes
    static int voteCount = 0;

    public static void main(String[] args) {
        // connection variables
        int port = 2005;
        ServerSocket listener;
        Socket connection = null;

        // list of roles
        String[] roleList = {"Mafia", "Civilian"};

        // random number generator for role assignment
        Random rand = new Random();
        int randomIndex;

        // wait for connection from client and set up streams
            // code to handle multiple clients will later be implemented here in a while loop
        try {
            listener = new ServerSocket(port);
            connection = listener.accept();
            listener.close();

            outgoingStream = new PrintWriter(connection.getOutputStream(), true);
            incomingStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get username from client; assign role and status
        try{
            incomingText = incomingStream.readLine();
            usernames.add(incomingText);
            randomIndex = rand.nextInt(roleList.length);
            // code will be updated to prevent duplicate Mafia roles
                roles.add(roleList[randomIndex]);
            status.add("Alive");
            outgoingStream.println(roles.get(roles.size()-1));
        } catch(Exception e) {
            e.printStackTrace();
        }

        // gets clients out of waiting room and starts the game
            // code will later wait for more players to join before starting the game
        outgoingStream.println("NIGHTPHASE");

        // night and day phases
            // code will be updated to loop through night and day phases until end game conditions
        try {
            incomingText = incomingStream.readLine();
            nightPhase(incomingText);

            incomingText = incomingStream.readLine();
            dayPhase(incomingText);

            // code to check for end game conditions
            int countAlive = Collections.frequency(status, "Alive");
            if (countAlive == 1 && roles.contains("Mafia")) {
                endGame(0);
                // code to exit loop will be implemented here
            } else if (roles.contains("Mafia")) {
                endGame(1);
                // code to exit loop will be implemented here
            } else{
                outgoingStream.println("CONTINUE");
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try{
                connection.close();
                incomingStream.close();
                outgoingStream.close();
            } catch(Exception e){

            }
    }

    }
    
     // night phase where Mafia will choose a victim and Civilians will wait
    public static void nightPhase(String role) {
        try {
            // code will be updated to handle multiple clients
            outgoingStream.println("Night has fallen.");

            if (incomingText.equals("Mafia")) {
                outgoingStream.println("Choose a victim: ");
                // code to display list of players and pick victim
                    // time limit for picking victim will also be implemented here
                
                // after victim is chosen, code to update victim's status and role to "Unalived" and "Ghost" respectively
                    // if no victim is chosen within time limit, no one will be eliminated that night
            } else if (incomingText.equals("Ghost")){
                // code to switch ghost client to spectator mode will be implemented here
                outgoingStream.println("You have been eliminated. You are now a spectator.");
            } else{
                outgoingStream.println("Wait for Mafia to choose a victim...");
            }

            outgoingStream.println("Night phase has ended.");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    // day phase where players will discuss and vote on who they think the Mafia is
        // time limit for discussion and voting will also be implemented here
    public static void dayPhase(String role) {
        try {
            if (role.equals("Ghost")) {
                // ghost clients can only watch the discussion and cannot participate in voting
                outgoingStream.println("You are a spectator. You can watch the discussion but cannot participate.");
             } else {   
                // chatroom functionality for discussion among players
                    // server will receive messages from clients and broadcast to all clients
                outgoingStream.println("Day has dawned. Discuss who is the Mafia.");

                // after discussion, display list of players and perform voting
                    // use synchronized method to handle multiple clients voting at the same time
                outgoingStream.println("Time for discussion is over. Vote for who you think the Mafia is.");

                // after voting, code to tally votes and update eliminated player's status and role to "Unalived" and "Ghost" respectively
                    // checks for tie votes and handles accordingly by eliminating no one
                outgoingStream.println("Votes have been tallied. The player with the most votes has been eliminated.");
                voteCount = 0;
             }
            outgoingStream.println("Day phase has ended.");
        } catch(Exception e) {
            e.printStackTrace();
         }
    }

    // end game method to determine winning team and display appropriate message to clients
    public static void endGame(int winningTeam) {
        try {
            outgoingStream.println("EXIT");
            outgoingStream.println("Game over. The winning team is...");
            if (winningTeam == 0) {
                outgoingStream.println("Mafia wins!");
            } else if (winningTeam == 1) {
                outgoingStream.println("Civilians win!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
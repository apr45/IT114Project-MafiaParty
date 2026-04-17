import java.net.*;
import java.io.*;
import java.util.*;

public class ServerModerator{
    // variable manages state of game
    public static final Object gameLock = new Object();

    // variables to keep track of number of players
    static private int maxPlayers = 2;
    static private int playersCount = 0;

    // variable to keep track if clients left the waiting room
    static private boolean exitWaitingState = false;
    
    // input and output streams
    static BufferedReader incomingStream = null;
    static PrintWriter outgoingStream = null;
    static String incomingText, outgoingText;

    // lists to store player information
    static public ArrayList<String> usernames = new ArrayList<String>();
    static public ArrayList<String> roles = new ArrayList<String>();
    static public ArrayList<String> status = new ArrayList<String>();

    // list of available roles
    static final private String[] ROLELIST = {"Mafia", "Civilian"};
    static private boolean mafiaAssigned = false;

    // variable to keep track of votes
    static int voteCount = 0;

    public static void main(String[] args) throws IOException{
        // connection variables
        final int PORT = 2005;
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket = null;

        // connects client to server until max number of players is reached
        while (playersCount < maxPlayers) {
            try {
                // waits for client connection
                System.out.println("Waiting connection...");
                clientSocket = serverSocket.accept();

                // creates new thread to handle client connection
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                playersCount += 1;
                System.out.println("Client " + playersCount + " connected.");

                clientThread.start(); // runs client thread
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // closes server socket once max number of players is reached
        serverSocket.close();
        System.out.println("Max number of players reached. No longer accepting connections.");


        // ensures all info per client is setup before server executes night state
        while (!exitWaitingState){}
        exitWaitingState = false;

        // displays list of players, roles, and statuses for testing purposes
        System.out.println("\nList of players: " + usernames);
        System.out.println("List of roles: " + roles);
        System.out.println("List of statuses: " + status);
    }

/*      // gets clients out of waiting room and starts the game
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
                clientSocket.close();
                incomingStream.close();
                outgoingStream.close();
            } catch(Exception e){

            }
        }

    }
*/
    // synchronized method to check for duplicate usernames
    public synchronized static boolean doubleUsername(String username){
        // checks if client's username is already in use
        if (usernames.contains(username)){
            return true;
        } else{
            usernames.add(username); // adds client's username to list of usernames if not already in use
            return false;
        }        
    }

    // synchronized method to set up client's role and status 
    public static synchronized String setup(String username){
        // role assignment logic
            // random number generator
            Random rand = new Random();
            int roleIndex;

            //ensures Mafia role is assigned to one client
            if (usernames.size() == maxPlayers && mafiaAssigned == false){
                roles.add("Mafia");
            } else {
                // checks if Mafia role has already been assigned
                if (roles.size() != 0 && !mafiaAssigned){
                    if (roles.contains("Mafia")){
                        mafiaAssigned = true;
                    }
                }

                // assigns a random role or Civilian role given whether Mafia role has already been assigned or not
                if (mafiaAssigned){
                    roleIndex = 1;
                } else {
                    roleIndex = rand.nextInt(ROLELIST.length);
                }

                roles.add(ROLELIST[roleIndex]); // stores client's role in list of roles
            }

        status.add("Alive"); // stores client's status in list of statuses

        return roles.get(roles.size()-1); // returns client's assigned role
    }


    // method to place clients into waiting rooms
    public static void clientWaitingRoom() throws InterruptedException {
        synchronized (gameLock) {
            while (playersCount < maxPlayers){
                gameLock.wait();
            }
            gameLock.notifyAll();
            exitWaitingState = true;
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
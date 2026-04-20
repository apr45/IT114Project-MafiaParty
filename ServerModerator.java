import java.net.*;
import java.io.*;
import java.util.*;

public class ServerModerator{
    // variable to use as lock for synchronized methods
    static final public Object GAMELOCK = new Object();

    // variable to keep track of game state
    static volatile public String gameState = "";

    // variable to keep track of timer
    static volatile public int timer = 1;

    // variables to keep track of number of players
    static private int maxPlayers = 2;
    static public int playersCount = 0;

    // variable to keep track of whether clients exited waiting room
    static private boolean exitWaitingState = false;

    // lists to store player information
    static private ArrayList<String> usernames = new ArrayList<String>();
    static private ArrayList<String> roles = new ArrayList<String>();
    static private ArrayList<String> status = new ArrayList<String>();

    // lists to store client input and output streams for communication with clients
    static private ArrayList<BufferedReader> incomingStreams = new ArrayList<BufferedReader>();
    static private ArrayList<PrintWriter> outgoingStreams = new ArrayList<PrintWriter>();

    // list of available roles
    static final private String[] ROLELIST = {"Mafia", "Civilian"};
    static private boolean mafiaAssigned = false;

    // variable to keep track of votes
    static private int voteCount = 0;

    // variable to keep track of incoming text from clients
    static private String incomingText;

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

        try {
            // waiting state until all clients set up information and enter waiting room
            while (exitWaitingState == false){
                Thread.sleep(1000);
            }

            // displays game information regarding players, roles, and statuses
            System.out.println("\n--Game Information--");
            System.out.println("List of players: " + usernames);
            System.out.println("List of roles: " + roles);
            System.out.println("List of statuses: " + status);

            // gets clients out of waiting room
            synchronized (GAMELOCK) {
                GAMELOCK.notifyAll();
            }

            // waiting state until all clients leave waiting room
            while (playersCount > 0){
                Thread.sleep(1000);
            }

            exitWaitingState = false; // resets waiting room
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // night and day phases
        serverDayPhase();
            /*
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
*/
    }

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

    // synchronized method to set up client information
    public static synchronized String setup(BufferedReader incomingStream, PrintWriter outgoingStream){
        // adds client's input and output streams to list of streams
        incomingStreams.add(incomingStream);
        outgoingStreams.add(outgoingStream);

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


    // method to manage clients in waiting room
    public static void clientWaitingRoom() throws InterruptedException {
        synchronized (GAMELOCK) {
            while (exitWaitingState == false){
                // last player to join waiting room will notify server to exit waiting state
                if (playersCount == maxPlayers){
                    exitWaitingState = true;
                }

                GAMELOCK.wait(); // clients will wait until server signals all to exit waiting room
            }
        }
    }

    // night phase where Mafia will choose a victim and Civilians will wait
  /*   public static void nightPhase(String role) {
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
*/
    public static void serverDayPhase(){
        System.out.println("\nDay Phase has begun for alive players. Starting time limit for discussion...");
        gameState = "DAYPHASE";
        try{
            Thread.sleep(10000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Time reached zero. Moving on to voting");
        timer = 0;
    }

    // day phase where players will discuss and vote on who they think the Mafia is
        // time limit for discussion and voting will also be implemented here
    public synchronized static void broadcast(String username, String message){
        try {
            for (int i = 0; i < usernames.size(); i++){
                if (!usernames.get(i).equals(username) && status.get(i).equals("Alive")){
                    outgoingStreams.get(i).println(username + ": " + message);
                } 
            }


            /* voting implementation will be added later
            // after discussion, display list of players and perform voting
                    // use synchronized method to handle multiple clients voting at the same time
                System.out.println("Time for discussion is over. Vote for who you think the Mafia is.");

                // after voting, code to tally votes and update eliminated player's status and role to "Unalived" and "Ghost" respectively
                    // checks for tie votes and handles accordingly by eliminating no one
                System.out.println("Votes have been tallied. The player with the most votes has been eliminated.");
                voteCount = 0; 
            */
        } catch(Exception e) {
            e.printStackTrace();
         }
    }

    // end game method to determine winning team and display appropriate message to clients
/*    public static void endGame(int winningTeam) {
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
    }*/
}
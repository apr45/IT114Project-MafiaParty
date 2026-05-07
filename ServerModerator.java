import java.net.*;
import java.io.*;
import java.util.*;

public class ServerModerator{
    // variable to use as lock for synchronized methods
    public static final Object GAME_LOCK = new Object();

    // variables to track data
    public static volatile String gameState = "";
    public static volatile boolean targetChoosen = false;
    private static boolean exitWaitingState = false;

    // variables to keep track of number of players
    public static final int MAX_PLAYERS = 2; // TODO: change max players to 5
    public static volatile int playersCount = 0;

    // lists to store player information
    private static ArrayList<String> usernames = new ArrayList<String>();
    private static ArrayList<String> roles = new ArrayList<String>();
    private static ArrayList<String> statuses = new ArrayList<String>();

    // lists to store client input and output streams for communication with clients
    private static ArrayList<BufferedReader> incomingStreams = new ArrayList<BufferedReader>();
    private static ArrayList<PrintWriter> outgoingStreams = new ArrayList<PrintWriter>();

    // list of available roles
    private static final String[] ROLELIST = {"Mafia", "Civilian"};

    // tracks votes
    private static int[] votes;
    private static int highestVoteIndex;
    private static boolean tieVote;

    public static void main(String[] args) throws IOException{
        // connection variables
        final int PORT = 2005;
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket;

        // connects client to server until max number of players is reached
        while (playersCount < MAX_PLAYERS) {
            try {
                // waits for client connection
                System.out.println("Waiting connection...");
                clientSocket = serverSocket.accept();

                // creates new thread to handle client connection
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                playersCount += 1;
                System.out.println("Client " + playersCount + " connected.");

                // runs client thread
                clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // closes server socket once max number of players is reached
        serverSocket.close();
        System.out.println("Max number of players reached. No longer accepting connections.");

        // temp code to stop server execution if a client disconnected; fix later as code gets blocked by readLine()
        /*for (BufferedReader clientStream : incomingStreams){
            try {
                String line = clientStream.readLine();
            } catch (SocketException e){
                System.out.println("One or more disconnections. Closing game...");
                System.exit(0);
            }
        }*/
        
        try {
            // waiting state until all clients set up information and enter waiting room
            while (exitWaitingState == false){
                Thread.sleep(1000);
            }

            // displays game information regarding players, roles, and statuses
            System.out.println("\n--Game Information--");
            System.out.println("List of players: " + usernames);
            System.out.println("List of roles: " + roles);
            System.out.println("List of statuses: " + statuses);

            // gets clients out of waiting room
            synchronized (GAME_LOCK) {
                GAME_LOCK.notifyAll();
            }

            // waiting state until all clients leave waiting room
            while (playersCount > 0){
                Thread.sleep(1000);
            }

            // resets waiting room
            exitWaitingState = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // enters server into a night and day cycle
            // TODO: update to loop until a win condition
        while(true){
            serverNightState();
            serverDayState();

            gameState = "ENDSTATE";
            break; //temp break
        
            
        /*  // temp code to check for end game conditions
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
            } catch(Exception e){
            }
        }*/
       }

       try{
        while (playersCount < MAX_PLAYERS){
                Thread.sleep(1000);
            }

        System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();    
        }
}

    // check for duplicate usernames
    public synchronized static boolean doubleUsername(String username){
        // checks if client's username is already in use
        if (usernames.contains(username)){
            return true;
        } else{
            // adds client's username to list of usernames if approved
            usernames.add(username);
            return false;
        }        
    }

    // set up client information
    public static synchronized String setup(BufferedReader incomingStream, PrintWriter outgoingStream){
        // adds client's input and output streams to list of streams
        incomingStreams.add(incomingStream);
        outgoingStreams.add(outgoingStream);

        // role assignment logic
            // random number generator
            Random rand = new Random();
            int roleIndex = rand.nextInt(ROLELIST.length);;

            // ensures Mafia role is assigned to one client
            if (usernames.size() == MAX_PLAYERS && !roles.contains("Mafia")){
                roles.add("Mafia");
            } else {
                if (roleIndex == 0 && !roles.contains("Mafia")){
                    roles.add(ROLELIST[roleIndex]);
                } else {
                    roles.add(ROLELIST[1]);
                }
            }

        // stores client's status in list of statuses
        statuses.add("Alive");

        // returns client's assigned role
        return roles.get(roles.size()-1);
    }

    // manage clients in waiting room
    public static void clientWaitingRoom() throws InterruptedException {
        synchronized (GAME_LOCK) {
            while (exitWaitingState == false){
                // last player to join waiting room will notify server to exit waiting state
                if (playersCount == MAX_PLAYERS){
                    exitWaitingState = true;
                }

                // clients will wait until server signals all to exit waiting room
                GAME_LOCK.wait();
            }
        }
    }

    // getters for username and status arraylists
        public static ArrayList<String> usernamesArrayList(){
            return usernames;
        }

        // displays a list of players that are alive to Mafia only; update later to implement GUI 
        public static ArrayList<String> alivePlayersList(){
            ArrayList<String> alivePlayers = new ArrayList<String>();

            for (int i = 0; i < MAX_PLAYERS; i++){
                if (statuses.get(i).equals("Alive") && roles.get(i).equals("Civilian") && gameState.equals("NIGHTSTATE")){
                    alivePlayers.add(usernames.get(i));
                } else if (statuses.get(i).equals("Alive") && gameState.equals("DAYSTATE")){
                    alivePlayers.add(usernames.get(i));
                }
            }
        
            return alivePlayers;
        }

        public static ArrayList<String> statusArrayList(){
            return statuses;
        }

    // night state for server side
    public static void serverNightState() {
        try {
            // signals server that night time has begun
            System.out.println("\nNight State has begun for players. Starting time limit for Mafia to elimate player...");
            gameState = "NIGHTSTATE";

            // initates a timer for server to wait during player elimination
            try{
                Thread.sleep(10000); // TODO: change timer to 30 - 60 seconds
            } catch (InterruptedException e){
            e.printStackTrace();
            }

            // once timer reaches, ends night time
            System.out.println("Timer reached zero. Night State has ended.");

            // displays updated game information regarding players, roles, and statuses
            System.out.println("\n--Game Information--");
            System.out.println("List of players: " + usernames);
            System.out.println("List of roles: " + roles);
            System.out.println("List of statuses: " + statuses);

            gameState = "";

            // removes all clients from waiting room
            synchronized (GAME_LOCK) {
                GAME_LOCK.notifyAll();
            }

            // waiting state until all clients leave waiting room
            while (playersCount > 0){
                Thread.sleep(1000);
            }

            // resets waiting room
            exitWaitingState = false;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // updates client's information based on Mafia's choice or highest vote count
    public static void eliminatedPlayer(String username){
        for (int i = 0; i < MAX_PLAYERS; i++){
            if (usernames.get(i).equals(username)){
                statuses.set(i, "Dead");
            }
        }
    }

    // day state for server side
    public static void serverDayState(){
        // signals server to initate timer for chat discussion
        System.out.println("\nDay State has begun for players. Starting time limit for discussion...");
        gameState = "DAYSTATE";

        // initates a timer for server to wait during client discussion
        try{
            Thread.sleep(10000); // TODO: change timer to 5 minutes
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        // signals server timer hits 0 and initate for player voting
        System.out.println("Time reached zero. Move on to voting..");
        votes = new int[MAX_PLAYERS];

        // initiates a timer for server to wait during voting
         try{
            Thread.sleep(11000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        // calculating which player was the highest votes
        highestVoteIndex = 0;
        tieVote = false;
        int highestVoteCount = 0;
        System.out.println("Voting is over. Counting votes..");
        for (int i = 0; i < MAX_PLAYERS; i++){
            if (votes[i] > highestVoteCount){
                highestVoteCount = votes[i];
                highestVoteIndex = i;
            }
        }

        // checks if more than two players tied in votes
        int duplicateVote = 0;
        for (int vote : votes){
            if (vote == highestVoteCount){
                duplicateVote ++;
            }
        }
        if (duplicateVote > 1){
            tieVote = true;
        }

        // changes highest voted player's status and role if no tied votes
        if (tieVote == false){
            eliminatedPlayer(usernames.get(highestVoteIndex));
        }

        // displays updated information
        System.out.println("\n--Game Information--");
        System.out.println("List of players: " + usernames);
        System.out.println("List of roles: " + roles);
        System.out.println("List of statuses: " + statuses);

        gameState = "";

        // removes all clients from waiting room
        synchronized (GAME_LOCK) {
            GAME_LOCK.notifyAll();
        }

        // waiting state until all clients leave waiting room
        try{
            while (playersCount > 0){
                Thread.sleep(1000);
            }
        } catch (InterruptedException e){

        }
    }

    // displays client message to other clients
    public synchronized static void broadcast(String username, String message){
        try {
            for (int i = 0; i < MAX_PLAYERS; i++){
                // prevent the original sender to recieve message
                if (!usernames.get(i).equals(username) /*&& statuses.get(i).equals("Alive")*/){
                    outgoingStreams.get(i).println(username + ": " + message);
                } 
            }
        } catch(Exception e) {
            e.printStackTrace();
         }
    }

    // adds player votes
    public synchronized static void addVote(String username){
        int index = usernames.indexOf(username);
        if (index != -1){
            votes[index] ++;
        }
    }

    // displays the results after voting
    public synchronized static void votingResults(){
        // informs player based if there is a tie or not
         for (int i = 0; i < MAX_PLAYERS; i ++){
            if (tieVote == false){
                // displays player with highest counts on votes
                outgoingStreams.get(i).println("Most Voted Player: " + usernames.get(highestVoteIndex));

                // reveals if player is Mafia or not
                if (roles.get(highestVoteIndex).equals("Mafia")){
                    outgoingStreams.get(i).println(usernames.get(highestVoteIndex) + " is the Mafia.");
                } else {
                    outgoingStreams.get(i).println(usernames.get(highestVoteIndex) + " is not the Mafia.");
                }
            } else {
                outgoingStreams.get(i).println("Votes were tied.");
                outgoingStreams.get(i).println("No player will be eliminated.");
            }
        }
    }

/*  // temp end game method to determine winning team and display appropriate message to clients
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
    }*/
}
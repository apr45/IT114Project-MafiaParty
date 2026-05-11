import java.net.*;
import java.io.*;
import java.util.*;

public class ServerModerator{
    // variable to use as lock for synchronized methods
    public static final Object GAME_LOCK = new Object();

    // variables to track data
    public static volatile String gameState = "";
    public static volatile boolean targetChoosen = false;

    // variables to keep track of number of players
    public static final int MAX_PLAYERS = 4;
    public static volatile int playerCount = 0;

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

    // sockets
    private static Socket clientSocket;
    private static ServerSocket serverSocket;

    // tracks for disconnections
    public static boolean clientDisconnected = false;

    // tracks winning team
    public static volatile String winner;

    public static void main(String[] args){
        try{
            // connection variables
            final int PORT = 2005;
            serverSocket = new ServerSocket(PORT);
        } catch (BindException e){
            System.out.println("Port already in use.");
            System.exit(0);
        } catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
        
        // connects client to server until max number of players is reached
        while (playerCount < MAX_PLAYERS) {
            try {
                // waits for client connection
                System.out.println("Waiting connection...");
                clientSocket = serverSocket.accept();

                // creates new thread to handle client connection
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                playerCount += 1;
                System.out.println("Client " + playerCount + " connected.");

                // runs client thread
                clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try{
            // closes server socket once max number of players is reached
            serverSocket.close();
            System.out.println("Max number of players reached. No longer accepting connections.");
        } catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
        
        try {
            // waiting state until all clients set up information and enter waiting room
            while (usernames.size() != MAX_PLAYERS){
                Thread.sleep(1000);
            }

            // checks for any disconnections
            cilentConnectionsCheck();
            if (clientDisconnected){
                throw new SocketException();
            } else if (MAX_PLAYERS == 1){
                throw new Exception("Can't start game with one player.");
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
            while (playerCount > 0){
                Thread.sleep(1000);
            }
        } catch (SocketException e){
            System.out.println("One or more disconnections occured during set up. Closing game...");
            System.exit(0);
        } catch (InterruptedException e) {
            System.out.println("Interrupted occurred during set up. Closing game...");
            System.exit(0);
        } catch (Exception e){
            if (MAX_PLAYERS == 1){
                System.out.println(e.getMessage());
            } else {
                System.out.println("An error occurred during set up. Closing game...");
            }
            System.exit(0);
        }

        // enters server into a night and day cycle
        while(true){
            try {
                // night and day cycle
                serverNightState();
                serverDayState();
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.exit(0);
            }
            
            // end game conditions
                // checks if 2 or less players are alive
                int countAlive = Collections.frequency(statuses, "Alive");
                if (countAlive <= 2 || statuses.get(roles.indexOf("Mafia")).equals("Dead")){
                    // indicates the end of the game
                    gameState = "ENDSTATE";

                    // checks if Mafia is still alive
                    if (statuses.get(roles.indexOf("Mafia")).equals("Alive")) {
                        endGame(0);
                        break;
                    } else {
                        endGame(1);
                        break;
                    }
                }
        }

        // exits game
        System.out.println("Closing game...");
        System.exit(0);
    }

    // getters 
        // gets an array of current players
        public static ArrayList<String> usernamesArrayList(){
            return usernames;
        }

        // gets a list of players that are alive
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

        // gets an array of current players' statuses
        public static ArrayList<String> statusArrayList(){
            return statuses;
        }

        // gets the status of a specific player
        public static String getStatus(String username){
            int index = usernames.indexOf(username);
            return statuses.get(index);
        }

    // setters
        // updates the player count
            public synchronized static void updatePlayerCount(String operation){
                if (operation.equals("add")){
                    playerCount++;
                } else if (operation.equals("subtract")){
                    playerCount--;
                }
            }

    // checks for client connection
    private static void cilentConnectionsCheck(){
        // stop server execution if a client disconnected
        for (PrintWriter clientStream : outgoingStreams){
            clientStream.println("Testing connection");
            if (clientStream.checkError()){
                clientDisconnected = true;
            }
        }
    }

    // night state for server side
    private static void serverNightState() throws Exception {
        try {
            // signals server that night time has begun
            System.out.println("\nNight State has begun for players. Starting time limit for Mafia to elimate player...");
            gameState = "NIGHTSTATE";

            // initates a timer for server to wait during player elimination
            int seconds = 0;
            for (int i = 0; i < MAX_PLAYERS; i++){
                seconds += 1000;
            }
            Thread.sleep(9000 + seconds);
            
            // checks for disconnections
            cilentConnectionsCheck();
            if (clientDisconnected){
                throw new SocketException();
            }

            // once timer reaches, ends night time
            System.out.println("Timer reached zero. Night State has ended.");

            // displays updated game information regarding players, roles, and statuses
            System.out.println("\n--Game Information--");
            System.out.println("List of players: " + usernames);
            System.out.println("List of roles: " + roles);
            System.out.println("List of statuses: " + statuses);

            // resets game state
            gameState = "";

            // removes all clients from waiting room
            synchronized (GAME_LOCK) {
                GAME_LOCK.notifyAll();
            }
            
            // waiting state until all clients leave waiting room
            while (playerCount > 0){
                Thread.sleep(1000);
            }
        } catch (SocketException e){
            throw new Exception("One or more disconnections occured during player elimination. Closing game...");
        } catch (InterruptedException e){
            throw new Exception("Interrupted occurred during player elimination. Closing game...");
        } catch (Exception e){
            throw new Exception("An error occurred during player elimination. Closing game...");
        }
    }

    // day state for server side
    private static void serverDayState() throws Exception{
        try {
            // global chat
                // signals server to initate timer for chat discussion
                System.out.println("\nDay State has begun for players. Starting time limit for discussion...");
                gameState = "DAYSTATE";

                // initates a timer for server to wait during client discussion
                int seconds = 0;
                for (int i = 0; i < MAX_PLAYERS; i++){
                    seconds += 1000;
                }
                Thread.sleep(20000 + seconds);

                // checks for disconnections
                if (clientDisconnected){
                    throw new SocketException();
                }
        } catch (SocketException e){
            throw new Exception("One or more disconnections occured during global discussion. Closing game...");
        } catch (InterruptedException e){
            throw new Exception("Interrupted occurred during server global discussion. Closing game...");
        } catch (Exception e){
            throw new Exception("An error occurred during global discussion. Closing game...");
        }

        try{
            // global voting
                // signals server timer hits 0 and initate for player voting
                System.out.println("Timer reached zero. Move on to voting..");
                votes = new int[MAX_PLAYERS];

                // initiates a timer for server to wait during voting
                int seconds = 0;
                for (int i = 0; i < MAX_PLAYERS; i++){
                    seconds += 1000;
                }
                Thread.sleep(16000 + seconds);

                // checks for disconnections
                cilentConnectionsCheck();
                if (clientDisconnected){
                    throw new SocketException();
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

                //resets game state
                gameState = "";

                // removes all clients from waiting room
                synchronized (GAME_LOCK) {
                    GAME_LOCK.notifyAll();
                }

                // waiting state until all clients leave waiting room
                while (playerCount > 0){
                    Thread.sleep(1000);
                }

                // checks for disconnections
                cilentConnectionsCheck();  
        } catch (SocketException e){
            throw new Exception("One or more disconnections occured during voting. Closing game...");
        } catch (InterruptedException e){
            throw new Exception("Interrupted occurred during voting. Closing game...");
        } catch (Exception e){
            throw new Exception("An error occurred during voting. Closing game...");
        }
    }

    // determines winning team and display appropriate message to clients
    private static void endGame(int winningTeam) {
        try {
            // informs server that game is coming to an end
            System.out.println("\nWinning condition has been met. Calculating winning team...");
            playerCount = MAX_PLAYERS;

            // calculates winning team
            if (winningTeam == 0) {
                winner = "Mafia";
                System.out.println("Mafia wins!");
            } else if (winningTeam == 1) {
                winner = "Civilians";
                System.out.println("Civilians win!");
            }

            // informs clients the game has ended and winning team
            synchronized (GAME_LOCK) {
                GAME_LOCK.notifyAll();
            }

             // waiting state until all clients leave waiting room
             while (playerCount > 0){
                Thread.sleep(1000);
            }
        } catch(Exception e) {
            System.out.println("An error occurred during end game. Closing game...");
        }
    }

    // displays the results after voting
    public static void votingResults(String username){
        // informs player based if there is a tie or not
        if (tieVote == false){
            // displays player with highest counts on votes
            outgoingStreams.get(usernames.indexOf(username)).println("Most Voted Player: " + usernames.get(highestVoteIndex));
            // reveals if player is Mafia or not
            if (roles.get(highestVoteIndex).equals("Mafia")){
                outgoingStreams.get(usernames.indexOf(username)).println(usernames.get(highestVoteIndex) + " is the Mafia.");
            } else {
                outgoingStreams.get(usernames.indexOf(username)).println(usernames.get(highestVoteIndex) + " is not the Mafia.");
            }
        } else {
            outgoingStreams.get(usernames.indexOf(username)).println("Votes were tied.");
            outgoingStreams.get(usernames.indexOf(username)).println("No player will be eliminated.");
        }
    }

    // check for duplicate usernames
    public synchronized static boolean doubleUsername(String username){
        // creates temp usernames in lowercase
        ArrayList<String> lowercaseNames = new ArrayList<String>();
        for (String name : usernames){
            lowercaseNames.add(name.toLowerCase());
        }

        // checks if client's username is already in use
        if (lowercaseNames.contains(username.toLowerCase())){
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
                // clients will wait until server signals all to exit waiting room
                GAME_LOCK.wait();
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

    // displays client message to other clients
    public synchronized static void broadcast(String username, String message){
        try {
            for (int i = 0; i < MAX_PLAYERS; i++){
                // prevent the original sender to recieve message
                if (!usernames.get(i).equals(username)){
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
}
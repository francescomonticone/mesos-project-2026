package it.polimi.ingsw.Client.View.TUI;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Client.View.View;
import it.polimi.ingsw.Network.DTO.EventResultDTO;
import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Network.DTO.PlayerDTO;
import it.polimi.ingsw.Network.DTO.PlayerDeltaDTO;
import it.polimi.ingsw.Network.DTO.TribeDTO;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.Terminal.Signal;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Text User Interface (TUI) implementation of the {@link View} interface.
 * <p>
 * This class handles the command-line interaction with the player. It uses
 * synchronization to ensure that asynchronous network messages from the server
 * are printed cleanly without corrupting the console output.
 * </p>
 */
public class TuiView implements View {

    private final Terminal terminal;
    private int termWidth;

    private final Scanner scanner;
    private boolean showingTribes;
    private boolean showingHelp;

    /**
     * We use a single PrintStream (System.out) for both info and errors.
     * Using System.Err concurrently with System.Out often causes visual glitches
     * in the terminal due to OS-level stream flushing differences.
     */
    private final PrintStream out;

    /**
     * Lock object used to synchronize console prints.
     * Prevents overlapping text when the server sends a message exactly
     * while the user is typing something.
     */
    private final Object printLock = new Object();

    /** Monitor object used to synchronize the main thread during the login phase. */
    private final Object loginMonitor = new Object();

    /**
     * Stores the result of the login attempt.
     * A value of {@code null} means no response has been received yet.
     */
    private Boolean loginSuccess = null;

    /**
     * Flag indicating whether the client is currently waiting for user input.
     * Used to determine if the prompt marker ("> ") needs to be redrawn
     * after an asynchronous message is printed.
     */
    private boolean isReadingInput = false;

    private String myNickname;

    private String currentTurnOwner;
    private MatchDTO latestMatchState;

    //this is used to display the disconnection timer with a dedicated thread
    private Thread tuiTimerThread = null;


    //these are cached values after the MatchResultDTO to enable DB leaderboard view
    private List<MatchResultDTO> GlobalLeaderboard = null;
    private int MatchSize = 0;

    /**
     * Stores the player's nickname locally in the view.
     * <p>
     * This is called after a successful login so that the TUI can dynamically
     * highlight the player's own components (e.g., green prompts, own tribe header).
     * </p>
     *
     * @param nickname the confirmed nickname of the player
     */
    public void setNickname(String nickname) {
        this.myNickname = nickname;
    }

    // ANSI Color Constants
    public static final String ANSI_RESET  = "\u001B[0m"; //reset ansi color
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN   = "\u001B[36m";
    public static final String ANSI_WHITE  = "\u001B[37m";


    /**
     * Constructs a new TuiView, initializing the standard input and output streams
     * and acquiring the terminal's width.
     */
    public TuiView() {
        this.scanner = new Scanner(System.in);

        Terminal t;
        int width = 80;

        try{
            t = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)
                    .build();

            width = t.getWidth();
            if (width <= 0) width = 80;

        }catch(Exception e){
            // Fallback if JLine doesn't succeed
            System.err.println("[WARN] JLine terminal not available (" + e.getMessage()
                    + "), falling back to System.out");
            t = null;
        }

        this.terminal = t;

        if(terminal != null){
            terminal.handle(Signal.WINCH, signal -> {
                int newWidth = terminal.getWidth();
                if (newWidth > 0) {
                    termWidth = newWidth;
                    if (latestMatchState != null && !showingTribes && !showingHelp) {
                        synchronized (printLock) {
                            clearScreen();
                            updateModel(latestMatchState);
                        }
                    }
                }
            });
        }

        this.out = new PrintStream(System.out);
        this.termWidth = width;
    }

    // --- JLINE UTILITY METHODS ---
    /**
     * Return terminal's actual width, if it fails
     * set width to fallback size
     *
     * @return actual terminal window's width
     */
    public int getTermWidth() {
        if (terminal != null) {
            int w = terminal.getWidth();
            return w > 0 ? w : termWidth;
        }
        return termWidth;
    }

    /**
     * Prints and horizontal line of a given char across the full terminal width
     */
    private String hLine(char c) {
        return String.valueOf(c).repeat(Math.max(0, getTermWidth()));
    }

    /**
     * Center a text ignoring ANSI codes and using the terminal's width
     */
    private String center(String text) {
        int visibleLength = text.replaceAll("\u001B\\[[0-9;]*m", "").length();
        int w = getTermWidth();
        if (visibleLength >= w) return text;
        int padding = (w - visibleLength) / 2;
        return " ".repeat(padding) + text;
    }

    /**
     * Truncates a string if exceed the terminal width
     */
    private String truncate(String text, int maxWidth) {
        if (text.length() <= maxWidth) return text;
        return text.substring(0, maxWidth - 3) + "...";
    }

    /**
     * Method used to resize the terminal's width, if it changed
     */
    private void CheckResize(){
        terminal.flush();
        this.termWidth = (terminal.getWidth() <= 0)? 80: terminal.getWidth();

    }

    // --- OVERRIDDEN INTERFACE METHODS ---

    /**
     * Updates the local representation of the game using the latest snapshot.
     *
     * @param snapshot the updated state of the match received from the server
     */
    @Override
    public void updateModel(MatchDTO snapshot) {

        // Save the current turn owner for the dynamic prompt
        this.currentTurnOwner = snapshot.currentPlayer();
        synchronized (printLock) {
            showingTribes = false;
            showingHelp = false;

            CheckResize();
            clearScreen();

            int w = getTermWidth();

            // adaptive banner
            out.println("\n" + hLine('='));
            out.println(center("GAME STATE UPDATED"));
            out.println(hLine('='));

            printBoard(snapshot);

            out.println("\n");

            // Display the current phase with a colored banner
            out.println("\n" + ANSI_RED);
            String title = ">>> " + snapshot.currentPhaseName() + " <<<";
            String subtitle = "[ ERA " + snapshot.currentEra() + " - ROUND " + snapshot.currentRound() + " ]";
            out.println(center(subtitle) + "\n");
            out.println(center(title) + ANSI_RESET + "\n");


            if (snapshot.currentPlayer() != null) {
                if (snapshot.currentPlayer().equals(myNickname)) {
                    out.println(ANSI_GREEN + center(">>> IT'S YOUR TURN! <<<") + ANSI_RESET);
                } else {
                    out.println(ANSI_YELLOW + center(">>> CURRENT TURN: " + snapshot.currentPlayer() + " <<<") + ANSI_RESET);
                    out.println("Waiting for them to make a move...");
                }
            } else {
                out.println(ANSI_CYAN + center(">>> RESOLVING AUTOMATIC ACTIONS... <<<") + ANSI_RESET);
            }

            out.println(hLine('=') + "\n");

            printPlayersDashboard(snapshot);

            //Event printing
            List<EventResultDTO> results = snapshot.eventResults();
            if(results != null && !results.isEmpty()){
                showEventResult(results); //handle event results if present
            }

            //count for active players and if there is only one player display the information message
            long activePlayers = snapshot.players().stream().filter(PlayerDTO::isConnected).count();

            if (tuiTimerThread != null) { //check if there is another old timer, in this case interrupt it
                tuiTimerThread.interrupt(); //if a new matchDTO will arrive from the network, we interrupt the thread (we catch the exception)
                tuiTimerThread = null; //initialize the thread
            }

            int startSeconds = snapshot.remainingDisconnectionTime(); //save the remaining disconnection time

            if (activePlayers == 1 && snapshot.players().size() > 1) {
                //adaptive box
                String warning = "⚠ WARNING: YOU ARE THE ONLY PLAYER LEFT CONNECTED!";
                String subWarning = "The disconnection timer has started. Waiting for opponents.";
                int boxWidth = Math.min(subWarning.length() + 4, w);

                out.println(ANSI_RED + "╔" + "═".repeat(boxWidth - 2) + "╗");
                out.println("║ " + truncate(warning, boxWidth - 4) +
                        " ".repeat(Math.max(boxWidth-4 - truncate(warning, boxWidth-4).length(), 0)) + " ║");
                out.println("║ " + truncate(subWarning, boxWidth - 4) + " ║");
                out.println("╚" + "═".repeat(boxWidth - 2) + "╝" + ANSI_RESET + "\n");
            }
            //create a local thread to
            tuiTimerThread = new Thread(() -> {
                int remaining = startSeconds;
                try {
                    while (remaining > 0 && !Thread.currentThread().isInterrupted()) {
                        synchronized (printLock) {
                            //print the remaining time on the same line
                            out.print("\r\033[K" + ANSI_RED + "⏳ Time remaining: " + remaining + " seconds... " + ANSI_RESET);
                            out.flush();
                        }
                        Thread.sleep(1000); // wait for 1 s
                        remaining--;
                    }
                } catch (InterruptedException e) {
                    // thread is interrupted, someone reconnected or the game ended, we just exit
                    //no operation
                }
            });
            tuiTimerThread.setDaemon(true); //if the app closes, this thread will die with it
            tuiTimerThread.start(); //start the thread

            restorePrompt();
        }
        this.latestMatchState = snapshot;
    }


    /**
     * Displays the end-game screen, showing the match winner.
     * Caches the global leaderboard for later viewing.
     */
    public void showEndGame(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) {
        //cache values for leaderboard view
        this.GlobalLeaderboard = globalLeaderboard;
        this.MatchSize = numPlayers;

        synchronized (printLock) {
            clearScreen();

            out.println("\n" + ANSI_YELLOW + center("MATCH COMPLETE") + ANSI_RESET + "\n");

            // leaderboard of the match
            out.println(ANSI_CYAN + center("=== 🏆 FINAL STANDINGS 🏆 ===") + ANSI_RESET);

            for (int i = 0; i < matchRanking.size(); i++) {
                MatchResultDTO p = matchRanking.get(i);

                String prefix = p.nickname().equals(myNickname) ? ANSI_GREEN + " ★ " : ANSI_WHITE + "   ";
                String rankStr = (i == 0) ? "🥇" : (i == 1) ? "🥈" : (i == 2) ? "🥉" : " " + (i + 1);

                int nameWidth = 16;
                String result = String.format("%s%s  %-" + nameWidth + "s | %3d PP | %3d Food",
                        prefix, rankStr, truncate(p.nickname(), nameWidth),
                        p.prestigePoints(), p.food());
                out.println(center(result) + ANSI_RESET);
            }

            out.println("\n" + ANSI_WHITE + "Type " + ANSI_CYAN + "'leaderboard'" + ANSI_WHITE + " to view the Global Leaderboard, or " + ANSI_GREEN + "'exit'" + ANSI_WHITE + " to close." + ANSI_RESET);

            // to avoid overlapping
            isReadingInput = false;
        }
    }

    /**
     * Called when the user types 'db' at the end of the game.
     */
    public void showGlobalLeaderboard() {
        synchronized (printLock) {
            clearScreen();
            getTermWidth();

            // find global leaderboard position
            int myGlobalRank = -1;
            for (int i = 0; i < GlobalLeaderboard.size(); i++) {
                if (GlobalLeaderboard.get(i).nickname().equals(myNickname)) {
                    myGlobalRank = i + 1;
                    break;
                }
            }

            // display global leaderboard
            out.println("\n" + ANSI_PURPLE + center("=== 🌍 GLOBAL TOP 20 (" + MatchSize + " PLAYERS) 🌍 ===") + ANSI_RESET);

            if (myGlobalRank != -1) {
                out.println(ANSI_GREEN + center("Your World Rank: #" + myGlobalRank) + ANSI_RESET + "\n");
            } else {
                out.println(ANSI_YELLOW + center("Your score hasn't been registered globally yet.") + "\n" + ANSI_RESET);
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy"); //formatting date
            int nameWidth = 16;

            int displayLimit = Math.min(20, GlobalLeaderboard.size());
            for (int i = 0; i < displayLimit; i++) {
                MatchResultDTO r = GlobalLeaderboard.get(i);
                String color = r.nickname().equals(myNickname) ? ANSI_GREEN : ANSI_WHITE; //dynamic coloured display name
                String dateStr = r.date() != null ? sdf.format(r.date()) : "Unknown";

                String entry = String.format("  %s#%-2d %-15s | %3d PP | %s",
                        color, (i + 1), truncate(r.nickname(), nameWidth), r.prestigePoints(), dateStr);
                out.println(center(entry) + ANSI_RESET);
            }

            if (GlobalLeaderboard.size() > 20) {
                out.println(ANSI_WHITE + center("  ...and " + (GlobalLeaderboard.size() - 10) + " more players.") + ANSI_RESET);
            }

            out.println("\n" + ANSI_WHITE + "Type " + ANSI_GREEN + "'exit'" + ANSI_WHITE + " to close the game." + ANSI_RESET);
            restorePrompt();
        }
    }

    /**
     * Generates a context-aware prompt string based on the game state.
     * <p>
     * If the match is running, it visually indicates whether the client
     * can play or must wait. Otherwise, it returns a default prompt.
     * </p>
     *
     * @return the formatted prompt string
     */
    private String getDynamicPrompt() {
        if (this.myNickname == null || this.currentTurnOwner == null) {
            return "> "; // Default prompt for Lobby or uninitialized state
        }

        if (this.myNickname.equals(this.currentTurnOwner)) {
            return ANSI_GREEN + "[YOUR TURN] > " + ANSI_RESET;
        } else {
            return ANSI_YELLOW + "[WAITING...] > " + ANSI_RESET;
        }
    }

    /**
     * Displays the list of available lobbies in a tabular format.
     *
     * @param lobbies The list of {@link LobbyDTO} objects provided by the server.
     */
    @Override
    public void showOpenLobbies(List<LobbyDTO> lobbies) {

        if (this.latestMatchState != null) { //if we have received already the matchDTO we skip the lobby phase (reconnection)
            return;
        }

        clearScreen();
        int w = getTermWidth();

        System.out.println("\n" + ANSI_PURPLE + hLine('=') + ANSI_RESET);
        System.out.println(ANSI_WHITE + center("--- AVAILABLE GAME LOBBIES ---") + ANSI_RESET);
        System.out.println(ANSI_PURPLE + hLine('=') + ANSI_RESET);

        if (lobbies.isEmpty()) {
            System.out.println(ANSI_YELLOW + "   No lobbies found. Type 'create <size>' to start one!" + ANSI_RESET);
        } else {
            // adaptive Header
            int idW = 5, creatorW = Math.max(10, (w - 30) / 3), playersW = 10, nicksW = w - idW - creatorW - playersW - 15;

            // Table Header
            System.out.printf(ANSI_YELLOW + "%-5s | %-15s | %-10s | %-20s\n" + ANSI_RESET, //left aligned columns with specific widths
                    "ID", "CREATOR", "PLAYERS", "CURRENT PLAYERS");
            System.out.println(hLine('-'));

            for (LobbyDTO lobby : lobbies) {
                // Logic to color the player count based on fullness
                String occupancyColor = ANSI_GREEN; // Default: plenty of space
                if (lobby.currentPlayers() >= lobby.maxPlayers() - 1) occupancyColor = ANSI_YELLOW;
                if (lobby.currentPlayers() == lobby.maxPlayers()) occupancyColor = ANSI_RED;

                String playersInfo = occupancyColor + lobby.currentPlayers() + "/" + lobby.maxPlayers() + ANSI_RESET;

                // Printing the row with specific colors for each column
                System.out.printf(ANSI_CYAN + "%-5d" + ANSI_RESET + " | %-15s | %-20s | %s\n",
                        lobby.lobbyId(),
                        truncate(lobby.creatorNickname(), creatorW),
                        playersInfo,
                        truncate(String.join(", ", lobby.connectedNicknames()), nicksW)
                );
            }
        }

        System.out.println(ANSI_PURPLE + hLine('=') + ANSI_RESET);
        System.out.println(ANSI_WHITE + "Commands: " + ANSI_GREEN + "join <ID>" + ANSI_RESET + " | " +
                ANSI_CYAN + "create <size>" + ANSI_RESET + " | " +
                ANSI_YELLOW + "refresh" + ANSI_RESET);
        System.out.print("> ");
    }

    /**
     * Displays a generic informational message to the player.
     *
     * @param message the message to display
     */
    @Override
    public void showMessage(String message) {
        synchronized (printLock) {
            // \r moves cursor to the start of the line, \033[K clears the line
            out.print("\r\033[K");
            out.println("[INFO] " + message);
            restorePrompt();
        }
    }

    /**
     * Displays an error message to the player.
     *
     * @param message the error message
     */
    @Override
    public void showError(String message) {
        synchronized (printLock) {
            // \r moves cursor to the start of the line, \033[K clears the line
            out.print("\r\033[K");
            out.println("[ERROR] " + message);
            restorePrompt();
        }
    }

    /**
     * Notifies the view whether the login attempt was successful.
     *
     * @param success {@code true} if the nickname was accepted, {@code false} otherwise
     */
    @Override
    public void showLoginResult(boolean success) {
        synchronized (printLock) {
            out.print("\r\033[K");
            if (success) {
                if (this.latestMatchState != null) { //control if we are reconnecting (a matchDTO is arrived)
                    out.println(ANSI_GREEN + "[SUCCESS] Welcome back! Reconnecting to the match in progress..." + ANSI_RESET);
                } else {
                    out.println(ANSI_GREEN + "[SUCCESS] Login accepted! Welcome to the Lobby." + ANSI_RESET);
                    out.println(hLine('-'));
                    out.println("COMMANDS AVAILABLE:");
                    out.println(" - Type 'create <size>' to host a new match (e.g., 'create 4').");
                    out.println(" - Type 'join <tileId>' to join an open lobby (e.g., 'join 1001').");
                    out.println(hLine('-'));
                }
            } else {
                out.println(ANSI_RED + "[FAILED] Login failed. The nickname might be taken or the password is wrong." + ANSI_RESET);
            }
            restorePrompt();
        }

        // Wakes up the main thread to communicate the result
        synchronized (loginMonitor) {
            loginSuccess = success;
            loginMonitor.notifyAll();
        }
    }

    /**
     * Prompts the player to submit an action because it is their turn.
     * <p>
     * The client performs no logic to determine the phase. It simply prints
     * the exact instruction provided by the server's State Machine.
     * </p>
     *
     * @param messageTUI the specific instruction string for TUI from the server
     * @param messageGUI the specific instruction string for GUI from the server
     */
    @Override
    public void askAction(String messageTUI, String messageGUI) {
        synchronized (printLock) {
            out.print("\r\033[K");
            out.println(ANSI_GREEN + messageTUI + ANSI_RESET); //server sent directly the message to print
            restorePrompt();
        }
    }


    // --- SYNCHRONIZATION AND UTILITY METHODS ---

    /**
     * Resets the login state.
     * <p>
     * This must be called before a new login attempt to clear out any
     * delayed responses from previous timed-out attempts.
     * </p>
     */
    public void resetLoginState() {
        synchronized (loginMonitor) {
            loginSuccess = null;
        }
    }

    /**
     * Blocks the calling thread until the server responds to the login request,
     * or until the 5-second timeout expires.
     *
     * @return {@code true} if login was successful, {@code false} if it failed or timed out
     */
    public boolean waitForLoginResponse() {
        synchronized (loginMonitor) {
            if (loginSuccess == null) {
                try {
                    // Wait for a maximum of 5 seconds (5000 milliseconds)
                    loginMonitor.wait(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // If loginSuccess is still null after the wait, the server did not respond in time
            if (loginSuccess == null) {
                showError("Timeout: The server did not respond in time. Please try again.");
                return false; // Treat timeout as a failure to prevent blocking the application
            }

            // Consume the result and reset it for future use
            boolean result = loginSuccess;
            loginSuccess = null;
            return result;
        }
    }

    /**
     * Reads a line of input from the user safely, managing the prompt display.
     *
     * @param prompt the text to display before waiting for input
     * @return the string typed by the user (trimmed)
     */
    public String readInput(String prompt) {
        synchronized (printLock) {
            isReadingInput = true; // Signal that the app is waiting for user input

            out.print("\r\033[K"); // Clear the current line
            if (prompt != null && !prompt.isEmpty()) {
                out.print(prompt);
            } else {
                out.print(getDynamicPrompt()); // Default prompt marker
            }
            out.flush();
        }

        // Wait for the user to type and press Enter (blocking call)
        String input = "";
        if (scanner.hasNextLine()) {
            input = scanner.nextLine().trim();
        }

        synchronized (printLock) {
            isReadingInput = false; // Signal that input reading has finished
            // pendingNewline is removed entirely
        }

        return input;
    }

    /**
     * Prints the current state of the game board to the console.
     * <p>
     * This method should be called whenever the board state is updated to provide
     * a visual representation of the game.
     * </p>
     */
    private void printBoard(MatchDTO snapshot) {
        printUpperRow(snapshot);
        out.println("\n");

        printOfferTrack(snapshot);
        out.println("\n");

        printLowerRow(snapshot);
    }

    /**
     * Renders the Offer Track (tiles and totems) to the terminal.
     * Delegates the actual drawing logic to the {@link TuiBoardRenderer}.
     *
     * @param snapshot the current state of the match containing the offer track data
     */
    private void printOfferTrack(MatchDTO snapshot) {
        TuiBoardRenderer renderer = new TuiBoardRenderer(snapshot,termWidth);
        renderer.printOfferTrack();
    }

    /**
     * Renders the upper row of cards (e.g., characters, buildings) to the terminal.
     * Delegates the actual drawing logic to the {@link TuiCardRenderer}.
     *
     * @param snapshot the current state of the match containing the upper row cards
     */
    private void printUpperRow(MatchDTO snapshot) {
        synchronized (printLock) {
            TuiCardRenderer renderer = new TuiCardRenderer(snapshot, termWidth);
            renderer.printUpperRow();
        }
    }

    /**
     * Renders the lower row of cards to the terminal.
     * Delegates the actual drawing logic to the {@link TuiCardRenderer}.
     *
     * @param snapshot the current state of the match containing the lower row cards
     */
    private void printLowerRow(MatchDTO snapshot) {
        synchronized (printLock) {
            TuiCardRenderer renderer = new TuiCardRenderer(snapshot, termWidth);
            renderer.printLowerRow();
        }
    }

    /**
     * Prints a quick dashboard showing the vital statistics of all players.
     */
    private void printPlayersDashboard(MatchDTO snapshot) {

        out.println(ANSI_CYAN + center("PLAYERS DASHBOARD") + ANSI_RESET);
        out.println(ANSI_CYAN + hLine('=') + ANSI_RESET);

        //Header adaptive to the terminal length
        int nameWidth = 16;
        String header = String.format("%-" + nameWidth + "s | 🍖 F | 🌟 P | 🛖 B | 🏹 H | 🌀 S | 🎨 A | 🔨 B | 🌿 G | 💡 I",
                "NICKNAME");
        out.println(ANSI_YELLOW + center(header) + ANSI_RESET);
        out.println(hLine('-'));

        for (PlayerDTO player : snapshot.players()) { //for each player print statistics
            String nickColor = player.nickname().equals(myNickname) ? ANSI_GREEN : ANSI_WHITE;
            if (!player.isConnected()) {
                nickColor = ANSI_RED; //red if the player is disconnected
            }
            TribeDTO t = player.tribeDTO();
            int buildingCount = player.ownedBuildingIds() != null ? player.ownedBuildingIds().size() : 0;

            int h = 0, s = 0, a = 0, b = 0, g = 0, i = 0;
            if (t != null) {
                h = t.hunterCount();
                s = t.shamanCount();
                a = t.artistCount();
                b = t.builderCount();
                g = t.gathererCount();
                i = t.inventorCount();
            }

            //alignment
            String info = String.format(nickColor + "%-" + nameWidth + "s" + ANSI_RESET +  "|  %2d  |  %2d  |  %2d  |  %2d  |  %2d  |  %2d  |  %2d  |  %2d  |  %2d",
                    truncate(player.nickname(), nameWidth),
                    player.foodTokens(),
                    player.prestigePoints(),
                    buildingCount,
                    h, s, a, b, g, i);
            System.out.println(center(info));

        }
        out.println(ANSI_CYAN + hLine('=') + "\n" + ANSI_RESET);
    }

    /**
     * Shared rendering logic for any player's tribe.
     */
    private void printTribeContent(PlayerDTO player) {
        TuiCardRenderer renderer = new TuiCardRenderer(latestMatchState, termWidth);

        var buildings  = player.ownedBuildingIds();
        var characters = player.ownedCharacterIds();

        int bSize = buildings  != null ? buildings.size()  : 0;
        int cSize = characters != null ? characters.size() : 0;

        out.println(ANSI_YELLOW + "\n--- 🛖 BUILDINGS (" + bSize + ") ---" + ANSI_RESET);
        if (bSize == 0) {
            out.println("  No buildings yet.");
        } else {
            renderer.printCardRow(buildings);
        }

        out.println(ANSI_CYAN + "\n--- 👤 CHARACTERS (" + cSize + ") ---" + ANSI_RESET);
        if (cSize == 0) {
            out.println("  No characters yet.");
        } else {
            renderer.printCardRow(characters);
        }
    }


    /**
     * Displays a detailed view of all players' tribes provided by the Controller.
     */

    @Override
    public void showAllTribes(List<PlayerDTO> players) {
        synchronized (printLock) {
            clearScreen();

            // Iterate over the list exactly as the Controller ordered it
            for (PlayerDTO p : players) {
                boolean isMe = p.nickname().equals(myNickname);
                String header = isMe ? "YOUR TRIBE (" + p.nickname() + ")" : "TRIBE OF: " + p.nickname();

                if (isMe) {
                    out.println(ANSI_GREEN + hLine('='));
                    out.println("  " + header);
                    out.println(hLine('=') + ANSI_RESET);
                } else {
                    out.println("\n" + ANSI_CYAN + hLine('='));
                    out.println("  " + header);
                    out.println(hLine('=') + ANSI_RESET);
                }

                int susDiscount = - p.tribeDTO().sustenanceDiscount();
                int buildDiscount = - p.tribeDTO().builderDiscount();
                int star =  p.tribeDTO().shamanStarsCount();
                int distinctInv = p.tribeDTO().distinctInvention();
                int invPair = p.tribeDTO().inventionPair();

                out.printf("%-50s%n", "Sustenance discount: "+ susDiscount+
                        "\nBuilder discount: "+buildDiscount+
                        "\nShaman stars: "+star+
                        "\nDistinct invention: "+distinctInv+
                        "\nSame invention pairs: "+invPair);

                printTribeContent(p);
            }

            // Prompt to return at the very bottom
            out.println("\n" + ANSI_PURPLE + hLine('=') + ANSI_RESET);
            out.println(ANSI_WHITE + "Type " + ANSI_GREEN + "'back'" + ANSI_WHITE + " to return to the game board." + ANSI_RESET);
            out.print("> ");
        }
    }

    /**
     * Displays the results of an event phase.
     * <p>
     * Translates the GUI EventResultController logic into a formatted ASCII table,
     * showing up/down arrows and colors based on the point/food deltas.
     * Players are displayed as columns, metrics (Points, Food) as rows.
     * </p>
     *
     * @param results list of events with their results
     */
    public void showEventResult(List<EventResultDTO> results) {
        synchronized (printLock) {
            // Move the cursor to the starting point and clear the current prompt line
            out.print("\r\033[K");

            out.println("\n" + ANSI_PURPLE + hLine('='));
            out.println(center("⚡ EVENT RESULTS ⚡"));
            out.println(hLine('=') + ANSI_RESET);

            for (EventResultDTO result : results) {
                out.println(ANSI_CYAN + center(">>> Event Card: " + result.eventCardId() + " <<<") + ANSI_RESET);

                Map<String, PlayerDeltaDTO> deltas = result.playerDeltas();

                // Store the player names in a list to maintain a consistent column order across all rows
                List<String> players = new java.util.ArrayList<>(deltas.keySet());

                int metricWidth = 15;
                int playerColWidth = 12;

                //Build the Header Row (Player names)
                StringBuilder header = new StringBuilder();
                header.append(ANSI_YELLOW)
                        .append(String.format("%-" + metricWidth + "s", "METRIC"))
                        .append(ANSI_RESET);

                for (String player : players) {
                    // Highlight the current player's nickname in green
                    String nickColor = player.equals(myNickname) ? ANSI_GREEN : ANSI_WHITE;
                    String playerPadded = String.format("%-" + playerColWidth + "s", truncate(player, playerColWidth));

                    header.append(ANSI_YELLOW).append(" | ").append(ANSI_RESET)
                            .append(nickColor).append(playerPadded).append(ANSI_RESET);
                }

                // Calculate the exact visible length to create a perfectly aligned separator line
                int totalVisibleLength = metricWidth + (players.size() * (3 + playerColWidth));
                String separator = "-".repeat(totalVisibleLength);

                //Build the Prestige Points Row
                StringBuilder pointsRow = new StringBuilder();
                pointsRow.append(String.format("%-" + metricWidth + "s", "PRESTIGE POINTS"));

                for (String player : players) {
                    int ppDelta = deltas.get(player).pointsChange();
                    pointsRow.append(" | ").append(formatDelta(ppDelta, playerColWidth));
                }

                //Build the Food Row
                StringBuilder foodRow = new StringBuilder();
                foodRow.append(String.format("%-" + metricWidth + "s", "FOOD"));

                for (String player : players) {
                    int foodDelta = deltas.get(player).foodChange();
                    foodRow.append(" | ").append(formatDelta(foodDelta, playerColWidth));
                }

                // Print everything perfectly centered
                out.println(center(header.toString()));
                out.println(center(separator));
                out.println(center(pointsRow.toString()));
                out.println(center(foodRow.toString()));
                out.println();
                out.println(); // Add two empty lines between different events
            }

            out.println(ANSI_PURPLE + hLine('=') + ANSI_RESET + "\n");
        }
    }

    /**
     * Formats the delta value with arrows and colors, padding it to maintain table alignment.
     * Replicates the labelFormat logic from the GUI.
     *
     * @param delta the change in value
     * @param columnWidth the width of the column to maintain alignment
     * @return the formatted string with ANSI colors
     */
    private String formatDelta(int delta, int columnWidth) {
        String arrowStr;
        String color;

        if (delta > 0) {
            // Positive value: Cyan arrow up
            arrowStr = "↑ " + delta;
            color = ANSI_CYAN;
        } else if (delta < 0) {
            // Negative value: Red arrow down
            arrowStr = "↓ " + Math.abs(delta);
            color = ANSI_RED;
        } else {
            // Zero value: White, no arrow
            arrowStr = String.valueOf(delta);
            color = ANSI_WHITE;
        }

        // Compute the padding needed to fill the column width
        int paddingLength = Math.max(0, columnWidth - arrowStr.length());
        String padding = " ".repeat(paddingLength);

        // Return the colored string followed by the neutral padding spaces
        return color + arrowStr + ANSI_RESET + padding;
    }


    /**
     * Starts the infinite loop that processes user input from the console.
     * <p>
     * This method reads raw strings from the standard input, parses the commands,
     * and delegates the execution to the appropriate {@link ClientController} method.
     * This is a blocking call and should keep the CLI alive.
     * </p>
     *
     * @param controller the main client controller to handle application logic
     */
    public void startInputLoop(ClientController controller) {
        while (true) {
            CheckResize();
            String input = this.readInput("");

            if (input == null || input.isBlank()) {
                continue;
            }

            String command = input.toLowerCase();

            if (showingTribes) {
                if (command.equals("back")) {
                    showingTribes = false;
                    showingHelp = false;
                    if (this.latestMatchState != null) {
                        this.updateModel(this.latestMatchState);
                    }
                } else if (command.equals("exit")) {
                    this.showMessage("Closing application...");
                    System.exit(0);
                } else {
                    this.showError("Invalid command. You are viewing tribes. Type 'back' to return.");
                }
                continue;
            }

            if(showingHelp){
                if(command.equals("back")){
                    if (this.latestMatchState != null) {
                        this.updateModel(this.latestMatchState);
                    }
                    continue;
                }
                showingHelp = false;
                if(this.latestMatchState != null){
                    this.updateModel(this.latestMatchState);
                }
            }

            if (command.equals("exit")) {
                this.showMessage("Closing application...");
                System.exit(0);
            }
            else if (command.equals("help")) {
                showingHelp = true;
                this.printHelp();
            }
            else if (command.startsWith("create ")) {
                try {
                    int size = Integer.parseInt(input.split(" ")[1]);
                    controller.handleCreateLobby(size);
                } catch (Exception e) {
                    this.showError("Invalid format. Usage: 'create <number>'");
                }
            }
            else if (command.startsWith("join ")) {
                try {
                    int lobbyId = Integer.parseInt(input.split(" ")[1]);
                    controller.handleJoinLobby(lobbyId);
                } catch (Exception e) {
                    this.showError("Invalid format. Usage: 'join <lobby_id>'");
                }
            }
            else if (command.equals("refresh")) {
                controller.handleRequestOpenLobbies();
            }
            else if (command.startsWith("place ")) {
                try {
                    String arg = input.split(" ")[1].toUpperCase();
                    char tileId = arg.charAt(0);
                    controller.handlePlaceTotem(tileId);
                } catch (Exception e) {
                    this.showError("Invalid format. Usage: 'place <Letter>'");
                }
            }
            else if (command.startsWith("pick")) {
                try {
                    // Split the input carefully
                    String[] parts = input.split("\\s+");

                    List<String> requestedCards = new java.util.ArrayList<>();

                    // If the user typed "pick" and nothing else, parts.length will be 1.
                    // This means they want to pick 0 cards.
                    if (parts.length > 1) {
                        // Extract all arguments after the "pick" command
                        for (int i = 1; i < parts.length; i++) {
                            requestedCards.add(parts[i].toUpperCase());
                        }
                    }

                    // Send the action to the controller. An empty list means "I pick nothing".
                    controller.handleResolveOffer(requestedCards);

                } catch (Exception e) {
                    this.showError("Invalid format. Usage: 'pick <Card1> <Card2>...' or just 'pick' to take nothing.");
                }
            }

            else if (command.equals("skip")) {
                controller.handleSkipExtra();
            }
            else if (command.startsWith("extra ")) {
                try {
                    String[] parts = input.split("\\s+");
                    if (parts.length != 2) {
                        this.showError("Invalid format. Usage: 'extra <CardID>'");
                        continue;
                    }
                    String cardId = parts[1].toUpperCase();

                    // Find if the card is in upper or lower row
                    boolean isUpper = false;
                    boolean found = false;

                    if (this.latestMatchState != null) {
                        if (this.latestMatchState.board().upperRowCardIds().contains(cardId)) {
                            isUpper = true;
                            found = true;
                        } else if (this.latestMatchState.board().lowerRowCardIds().contains(cardId)) {
                            found = true;
                        }
                    }

                    if (!found) {
                        this.showError("Card '" + cardId + "' not found on the board.");
                        continue;
                    }

                    controller.handleExtraPick(cardId, isUpper);

                } catch (Exception e) {
                    this.showError("Invalid format. Usage: 'extra <CardID>'");
                }
            }
            else if (command.equals("tribe")) {
                showingTribes = true;
                controller.handleShowTribe(); //controller handles tribe
            }
            else if (command.equals("leaderboard")) {
                if (GlobalLeaderboard != null) {
                    this.showGlobalLeaderboard();
                } else {
                    this.showError("The global leaderboard is only available at the end of the match.");
                }
            }
            else {
                this.showError("Unknown command. Type 'help' to see the list of available commands.");
            }
        }
    }


    /**
     * Clears the console screen using ANSI escape codes.
     * Note: This works on most Unix-like terminals and modern Windows terminals.
     */
    public void clearScreen() {
        synchronized (printLock) {
            out.print("\033[H\033[2J\033[3J");   // first cancel all then the cursor goes in home position
            out.flush();
        }
    }

    /**
     * Restores the input cursor (prompt marker) after an asynchronous print
     */

    private void restorePrompt() {
        if (isReadingInput) {
            out.print(getDynamicPrompt());
            out.flush();
        }
    }


    /**
     * Prints a guide of all tui commands
     */
    private void printHelp() {
        synchronized (printLock) {
            out.println("\n" + ANSI_CYAN + hLine('='));
            out.println(center("📜 MESOS COMMAND GUIDE 📜"));
            out.println(hLine('=') + ANSI_RESET);

            out.println(ANSI_YELLOW + "LOBBY COMMANDS:" + ANSI_RESET);
            out.println("  " + ANSI_GREEN + "create <size>" + ANSI_RESET + " : Creates a new match (size 2-5).");
            out.println("  " + ANSI_GREEN + "join <id>" + ANSI_RESET + "     : Joins an existing lobby by ID.");
            out.println("  " + ANSI_GREEN + "refresh" + ANSI_RESET + "       : Refreshes the list of open lobbies.");

            out.println("\n" + ANSI_YELLOW + "IN-GAME COMMANDS:" + ANSI_RESET);
            out.println("  " + ANSI_GREEN + "place <letter>"+ ANSI_RESET + " : Places your Totem on an Offer Tile (e.g., 'place A').");
            out.println("  " + ANSI_GREEN + "pick <cards>" + ANSI_RESET + "   : Takes cards from the board (e.g., 'pick CH_01 BL_02').");
            out.println("  " + ANSI_GREEN + "pick" + ANSI_RESET + "           : Skips the pick action (takes 0 cards).");
            out.println("  " + ANSI_GREEN + "extra <card>" + ANSI_RESET + "   : Takes an extra card (e.g., 'extra BL_01').");
            out.println("  " + ANSI_GREEN + "skip" + ANSI_RESET + "           : Skips the extra card pick.");
            out.println("  " + ANSI_GREEN + "tribe" + ANSI_RESET + "          : Opens the tribes dashboard (yours and opponents').");

            out.println("\n" + ANSI_YELLOW + "GENERAL COMMANDS:" + ANSI_RESET);
            out.println("  " + ANSI_GREEN + "help" + ANSI_RESET + "           : Shows this command guide.");
            out.println("  " + ANSI_GREEN + "leaderboard" + ANSI_RESET + "    : Shows the global top 20 (only at the end of a match).");
            out.println("  " + ANSI_GREEN + "exit" + ANSI_RESET + "           : Closes the application.");

            out.println(ANSI_CYAN + hLine('=') + ANSI_RESET + "\n");

            restorePrompt();
        }
    }
}
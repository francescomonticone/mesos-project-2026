package it.polimi.ingsw.Client;

import java.util.Scanner;


public class Launcher {
    /**
     * Launches either the TUI or GUI client based on the user's choice.
     *
     * @param args the command-line arguments
     */
    static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String choice;
        do {
            System.out.println("Choose interface:");
            System.out.println("  1. TUI");
            System.out.println("  2. GUI");
            System.out.print("---> ");
            choice = scanner.nextLine().trim();
        } while (!"1".equals(choice) && !"2".equals(choice));

        if ("1".equals(choice)) {
            MainClient.main(args);
        } else {
            MainClientGUI.main(args);
        }
    }
}
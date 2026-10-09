package it.polimi.ingsw.Client.View.GUI;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

/**
 * Manages the background audio playback for the JavaFX Graphical User Interface.
 * <p>
 * This class handles loading audio files, seamless looping, and global mute states.
 * It ensures that only one audio track plays at a time and intelligently prevents
 * restarting a track that is already currently playing.
 * </p>
 */
public class SoundManager {

    private MediaPlayer currentMediaPlayer;
    private boolean isMuted = true; // Default mute state

    /** Keeps track of the currently playing audio file to prevent redundant restarts. */
    private String currentTrackPath = null;

    /**
     * Plays the background music designed for the pre-match phase
     * (Start Screen, Server Connection, Login, and Lobby).
     * If this track is already playing, the method does nothing.
     */
    public void playPreMatchMusic() {
        playMusic("/music/mesos_audio_1.wav");
    }

    /**
     * Plays the background music designed for the active game phase.
     * If this track is already playing, the method does nothing.
     */
    public void playGameMusic() {
        playMusic("/music/mesos_audio_2.wav");
    }

    /**
     * Internal helper method to transition to a new audio track.
     * Stops and disposes of the previous media player before loading the new one.
     *
     * @param path the internal resource path to the .wav file
     */
    private void playMusic(String path) {
        // Check if the requested track is already playing, or it is another track
        if (currentMediaPlayer != null && path.equals(currentTrackPath)) {
            return;
        }

        // if there is already a music running, stop and clear it
        if (currentMediaPlayer != null) {
            currentMediaPlayer.stop();
            currentMediaPlayer.dispose(); // free the memory
        }

        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                System.err.println("[AUDIO] music not found: " + path);
                return;
            }

            Media media = new Media(resource.toString());
            currentMediaPlayer = new MediaPlayer(media);

            currentTrackPath = path; //save current track

            currentMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // infinite loop
            currentMediaPlayer.setMute(isMuted); // apply the current mute state

            currentMediaPlayer.play();

        } catch (Exception e) {
            System.err.println("[AUDIO] Error on reproducing music: " + e.getMessage());
        }
    }

    /**
     * Toggles the global mute state for the application and applies it
     * immediately to the currently playing media.
     *
     * @return the new mute state ({@code true} if sound is now muted, {@code false} otherwise)
     */
    public boolean toggleMute() {
        isMuted = !isMuted;
        if (currentMediaPlayer != null) {
            currentMediaPlayer.setMute(isMuted);
        }
        return isMuted;
    }

    /**
     * Checks the current global mute configuration.
     *
     * @return {@code true} if the audio is currently muted, {@code false} otherwise
     */
    public boolean isMuted() {
        return isMuted;
    }
}
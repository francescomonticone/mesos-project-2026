package it.polimi.ingsw.Client.View.GUI;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

/**
 * Utility class for dynamically loading and rendering emoji images as JavaFX {@link ImageView} components.
 * <p>
 * It resolves image files from the predefined resources directory and automatically
 * handles sizing, aspect ratio preservation, and basic error logging if an icon is missing.
 * </p>
 */
public class EmojiIcon {

    private static final String BASE_PATH = "/emoji/";
    private static final double DEFAULT_SIZE = 28;

    /**
     * Creates an {@link ImageView} containing the requested emoji icon at the default size (28x28).
     *
     * @param id the unique identifier (filename without extension) of the emoji (e.g., "smile")
     * @return a configured {@link ImageView} containing the emoji, or an empty view if the image is not found
     */
    public static ImageView of(String id) {
        return of(id, DEFAULT_SIZE);
    }

    /**
     * Creates an {@link ImageView} containing the requested emoji icon at a specific size.
     * <p>
     * The image is automatically scaled to fit the requested dimensions while strictly
     * preserving its original aspect ratio and applying smooth filtering.
     * </p>
     *
     * @param id   the unique identifier (filename without extension) of the emoji
     * @param size the desired width and height of the icon
     * @return a configured {@link ImageView} containing the emoji, or an empty view if the image is not found
     */
    public static ImageView of(String id, double size) {
        ImageView iv = new ImageView();
        try {
            URL url = EmojiIcon.class.getResource(BASE_PATH + id + ".png");
            if (url == null) {
                System.err.println("[EMOJI] Not found: " + id);
                return iv;
            }
            iv.setImage(new Image(url.toString()));
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
        } catch (Exception e) {
            System.err.println("[EMOJI] Error loading: " + id);
        }
        return iv;
    }
}
package it.polimi.ingsw.Client.View.GUI;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.net.URL;

/**
 * Utility class to resolve and load card images for the GUI.
 */
public final class CardImageResolver {

    /** Fallback image shown when no specific image is found. */
    private static final String FALLBACK = "/img/error.jpg";

    // Private constructor to prevent instantiation of utility class
    private CardImageResolver() {}

    /**
     * Returns a JavaFX {@link Image} for the given card identifier asynchronously.
     *
     * <p>The image is resolved by checking:
     * <pre>  /img/{cardId}.jpg</pre>
     * and then falling back to:
     * <pre>  /img/{cardId}.png</pre>
     * Falls back to a generic error image if the resource is not found.</p>
     *
     * @param cardId the card identifier from the server DTO
     * @return the resolved image, never {@code null}
     */
    public static Image resolve(String cardId) {
        return loadImage(cardId, true);
    }

    /**
     * Same as {@link #resolve(String)} but loads the image synchronously.
     * Use this when you need width/height immediately (e.g., for layout math).
     * * @param cardId the card identifier from the server DTO
     * @return the resolved image, never {@code null}
     */
    public static Image resolveSync(String cardId) {
        return loadImage(cardId, false);
    }

    /**
     * Internal helper to find the URL and load the image.
     */
    private static Image loadImage(String cardId, boolean backgroundLoading) {
        if (cardId == null) {
            return loadFallback();
        }

        URL url = getImageUrl(cardId);
        return (url != null) ? new Image(url.toExternalForm(), backgroundLoading) : loadFallback();
    }

    /**
     * Helper method to centralize the path lookup logic.
     */
    private static URL getImageUrl(String cardId) {
        // Try JPG first
        String pathJpg = "/img/" + cardId.toUpperCase() + ".jpg";
        URL url = CardImageResolver.class.getResource(pathJpg);

        // Try PNG if JPG fails
        if (url == null) {
            String pathPng = "/img/" + cardId + ".png";
            url = CardImageResolver.class.getResource(pathPng);
        }
        return url;
    }

    /**
     * Loads the generic fallback image, or a blank 1x1 image as a last resort to prevent crashes.
     */
    private static Image loadFallback() {
        URL url = CardImageResolver.class.getResource(FALLBACK);
        if (url != null) {
            return new Image(url.toExternalForm());
        }
        // Safest fallback: a 1x1 transparent WritableImage so the GUI doesn't crash
        return new WritableImage(1, 1);
    }
}
package ru.mihaly4.vkmdcli;

import org.json.JSONArray;
import org.json.JSONException;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VkHelper {
    @Nonnull
    public static String compileAudioId(@Nonnull String audioDataJson) {
        try {
            JSONArray audioData = new JSONArray(audioDataJson);

            String fullId = audioData.get(1) + "_" + audioData.get(0);
            String[] tokens = ((String) audioData.get(13)).split("/");
            String actionHash = tokens.length > 2 ? tokens[2] : "";
            String urlHash = tokens.length > 5 ? tokens[5] : "";

            return fullId + "_" + actionHash + "_" + urlHash;
        } catch (JSONException e) {
            return "";
        }
    }

    @Nonnull
    public static Target parseTarget(@Nonnull String url) throws TargetException {
        Pattern p = Pattern.compile("com/audios((-|)[\\d]+)");
        Matcher matches = p.matcher(url);
        if (matches.find()) {
            return new Target(matches.group(1), Target.Type.AUDIO);
        }

        p = Pattern.compile("com/(.+)");
        matches = p.matcher(url);
        if (matches.find()) {
            return new Target(matches.group(1), Target.Type.WALL);
        }

        throw new TargetException("Invalid VK.com URL.");
    }

    @Nonnull
    public static String decodeHtmlSpecialChars(@Nonnull String text) {
        return text.replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#039;", "'")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">");
    }

    @Nonnull
    public static String sanitizeFileName(@Nonnull String fileName) {
        // except any letters, numbers, space, and - _
        return fileName.replaceAll("[^\\p{L}\\p{N}\\040\\-_]", "_");
    }

    public final static class Target {
        public enum Type {
            AUDIO,
            WALL
        }

        @Nonnull
        private final String value;
        @Nonnull
        private final Type type;

        public Target(@Nonnull String value, @Nonnull Type type) {
            this.value = value;
            this.type = type;
        }

        @Nonnull
        public String getValue() {
            return value;
        }

        @Nonnull
        public Type getType() {
            return type;
        }
    }

    public final static class TargetException extends AppException {
        public TargetException(@Nonnull String message) {
            super(message);
        }
    }
}

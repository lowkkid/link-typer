package com.github.lowkkid.linktyper.core;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class MessageLoader {

    private static final String MULTI_MARKER  = "#multi";
    private static final Pattern VARIATION    = Pattern.compile("\\{\\{([^}]+)}}");
    private static final Random  RANDOM       = new Random();

    /**
     * Loads a file and returns a generated message.
     * - Plain file: returns content as-is.
     * - #multi file: picks a weighted random line, resolves {{a/b}} variations.
     * Throws MessageLoadException with a human-readable message on parse errors.
     */
    public static String load(Path file) throws IOException, MessageLoadException {
        String content = Files.readString(file).stripTrailing();

        if (!content.startsWith(MULTI_MARKER)) {
            return content;
        }

        List<String> lines = content.lines().toList();
        if (lines.size() < 2) {
            throw new MessageLoadException("File is marked #multi but contains no text lines.");
        }

        List<String> texts   = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;

            String text;
            int weight;

            int sep = line.lastIndexOf("||");
            if (sep != -1) {
                text = line.substring(0, sep).stripTrailing();
                String weightStr = line.substring(sep + 2).strip();
                try {
                    weight = Integer.parseInt(weightStr);
                    if (weight <= 0) throw new MessageLoadException(
                            "Line " + (i + 1) + ": weight must be a positive integer, got: " + weightStr);
                } catch (NumberFormatException e) {
                    throw new MessageLoadException(
                            "Line " + (i + 1) + ": invalid weight '" + weightStr + "' — must be a number.");
                }
            } else {
                text   = line;
                weight = 1;
            }

            if (text.isBlank()) {
                throw new MessageLoadException("Line " + (i + 1) + ": text is empty before '||'.");
            }

            texts.add(text);
            weights.add(weight);
        }

        if (texts.isEmpty()) {
            throw new MessageLoadException("No valid lines found in #multi file.");
        }

        String chosen = pickWeighted(texts, weights);
        return resolveVariations(chosen);
    }

    private static String pickWeighted(List<String> texts, List<Integer> weights) {
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int roll  = RANDOM.nextInt(total);
        int cum   = 0;
        for (int i = 0; i < texts.size(); i++) {
            cum += weights.get(i);
            if (roll < cum) return texts.get(i);
        }
        return texts.get(texts.size() - 1); // fallback, should never reach
    }

    private static String resolveVariations(String text) {
        Matcher m = VARIATION.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String[] options = m.group(1).split("/", -1);
            if (options.length < 2) {
                // malformed {{x}} with no slash — leave as-is
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
            } else {
                String picked = options[RANDOM.nextInt(options.length)];
                m.appendReplacement(sb, Matcher.quoteReplacement(picked));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static class MessageLoadException extends Exception {
        public MessageLoadException(String message) { super(message); }
    }
}
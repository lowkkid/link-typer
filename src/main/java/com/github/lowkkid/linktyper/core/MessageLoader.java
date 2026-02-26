package com.github.lowkkid.linktyper.core;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class MessageLoader {

    private static final String MULTI_MARKER = "#multi";
    private static final String PART_MARKER  = "[part]";
    private static final Pattern VARIATION   = Pattern.compile("\\{\\{([^}]+)}}");
    private static final Random  RANDOM      = new Random();

    public static String load(Path file) throws IOException, MessageLoadException {
        String content = Files.readString(file).stripTrailing();

        if (!content.startsWith(MULTI_MARKER)) {
            return content;
        }

        List<String> lines = content.lines().toList();
        if (lines.size() < 2) {
            throw new MessageLoadException("File is marked #multi but contains no text lines.");
        }

        // проверяем есть ли [part] секции
        boolean hasParts = lines.stream().anyMatch(l -> l.strip().equalsIgnoreCase(PART_MARKER));

        if (hasParts) {
            return generateFromParts(lines);
        } else {
            return generateFromLines(lines.subList(1, lines.size()), "file");
        }
    }

    // ── multi без parts — старое поведение ────────────────────────────────

    private static String generateFromLines(List<String> lines, String context)
            throws MessageLoadException {
        List<String>  texts   = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.strip().equalsIgnoreCase(PART_MARKER)) continue;

            parseLine(line, i + 1, texts, weights, context);
        }

        if (texts.isEmpty()) {
            throw new MessageLoadException("No valid lines found in " + context + ".");
        }

        return resolveVariations(pickWeighted(texts, weights));
    }

    // ── multi с parts ──────────────────────────────────────────────────────

    private static String generateFromParts(List<String> lines) throws MessageLoadException {
        // разбиваем на секции по [part]
        List<List<String>> parts = new ArrayList<>();
        List<String> current = null;

        for (String line : lines) {
            if (line.strip().equalsIgnoreCase(PART_MARKER)) {
                current = new ArrayList<>();
                parts.add(current);
            } else if (current != null && !line.isBlank()) {
                current.add(line);
            }
        }

        if (parts.isEmpty()) {
            throw new MessageLoadException("No [part] sections found after #multi.");
        }

        List<String> results = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            String partLabel = "[part] #" + (i + 1);
            results.add(generateFromLines(parts.get(i), partLabel));
        }

        return String.join(" ", results);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static void parseLine(String line, int lineNum,
                                  List<String> texts, List<Integer> weights,
                                  String context) throws MessageLoadException {
        String text;
        int weight;

        int sep = line.lastIndexOf("||");
        if (sep != -1) {
            text = line.substring(0, sep).stripTrailing();
            String weightStr = line.substring(sep + 2).strip();
            try {
                weight = Integer.parseInt(weightStr);
                if (weight <= 0) throw new MessageLoadException(
                        context + ", line " + lineNum + ": weight must be positive, got: " + weightStr);
            } catch (NumberFormatException e) {
                throw new MessageLoadException(
                        context + ", line " + lineNum + ": invalid weight '" + weightStr + "'.");
            }
        } else {
            text   = line;
            weight = 1;
        }

        if (text.isBlank()) {
            throw new MessageLoadException(
                    context + ", line " + lineNum + ": text is empty before '||'.");
        }

        texts.add(text);
        weights.add(weight);
    }

    private static String pickWeighted(List<String> texts, List<Integer> weights) {
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int roll  = RANDOM.nextInt(total);
        int cum   = 0;
        for (int i = 0; i < texts.size(); i++) {
            cum += weights.get(i);
            if (roll < cum) return texts.get(i);
        }
        return texts.get(texts.size() - 1);
    }

    private static String resolveVariations(String text) {
        Matcher m = VARIATION.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String[] options = m.group(1).split("/", -1);
            if (options.length < 2) {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(options[RANDOM.nextInt(options.length)]));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static class MessageLoadException extends Exception {
        public MessageLoadException(String message) { super(message); }
    }
}
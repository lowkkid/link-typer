package com.github.lowkkid.linktyper.core;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class MessageLoader {

    private static final String MULTI_MARKER = "#multi";
    private static final Pattern PART_HEADER = Pattern.compile(
            "\\[part(?:\\s+(\\d*\\.?\\d+))?]", Pattern.CASE_INSENSITIVE);
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
        boolean hasParts = lines.stream().anyMatch(l -> PART_HEADER.matcher(l.strip()).matches());

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
            if (line.isBlank() || PART_HEADER.matcher(line.strip()).matches()) continue;

            parseLine(line, i + 1, texts, weights, context);
        }

        if (texts.isEmpty()) {
            throw new MessageLoadException("No valid lines found in " + context + ".");
        }

        return resolveVariations(pickWeighted(texts, weights));
    }

    private static String generateFromParts(List<String> lines) throws MessageLoadException {
        List<List<String>>  partLines   = new ArrayList<>();
        List<Double>        partChances = new ArrayList<>();
        List<String>        current     = null;

        for (String line : lines) {
            Matcher m = PART_HEADER.matcher(line.strip());
            if (m.matches()) {
                current = new ArrayList<>();
                partLines.add(current);

                String chanceStr = m.group(1);
                if (chanceStr != null) {
                    double chance = Double.parseDouble(chanceStr);
                    if (chance < 0 || chance > 1) throw new MessageLoadException(
                            "[part] chance must be between 0 and 1, got: " + chanceStr);
                    partChances.add(chance);
                } else {
                    partChances.add(1.0); // всегда включается
                }
            } else if (current != null && !line.isBlank()) {
                current.add(line);
            }
        }

        if (partLines.isEmpty()) {
            throw new MessageLoadException("No [part] sections found after #multi.");
        }

        List<String> results = new ArrayList<>();
        for (int i = 0; i < partLines.size(); i++) {
            double chance = partChances.get(i);
            if (chance >= 1.0 || RANDOM.nextDouble() < chance) {
                String partLabel = "[part] #" + (i + 1);
                results.add(generateFromLines(partLines.get(i), partLabel));
            }
        }

        if (results.isEmpty()) {
            throw new MessageLoadException(
                    "All parts were skipped by chance — try again or increase probabilities.");
        }

        return String.join(" ", results);
    }

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
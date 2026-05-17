package com.codtech.recsys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Simple AI-based recommendation system demo using pure Java.
 *
 * We implement a basic user-based collaborative filtering algorithm:
 * - Compute similarity between users using Pearson correlation.
 * - Predict scores for items the target user has not rated.
 * - Recommend the top-N items with the highest predicted scores.
 *
 * Data file: data/user_preferences.csv
 * Format: userID,itemID,rating
 *
 * Example:
 * 1,101,4.5
 * 1,102,3.0
 * 2,101,2.0
 */
public class RecommendationApp {

    private static final String DATA_FILE_PATH = "data/user_preferences.csv";

    // userId -> (itemId -> rating)
    private static final Map<Long, Map<Long, Double>> USER_RATINGS = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("=== CODTECH Internship: Task-4 ===");
        System.out.println("AI-Based Recommendation System (User-Based Collaborative Filtering)");
        System.out.println("---------------------------------------------------------------");

        try (Scanner scanner = new Scanner(System.in)) {
            loadRatings();

            while (true) {
                System.out.print("\nEnter a user ID to get recommendations (or 'exit' to quit): ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                try {
                    long userId = Long.parseLong(input);
                    List<ItemScore> recs = recommendForUser(userId, 3);

                    if (recs.isEmpty()) {
                        System.out.println("No recommendations found for user " + userId + ". Try another user ID.");
                    } else {
                        System.out.println("Top " + recs.size() + " recommendations for user " + userId + ":");
                        for (ItemScore score : recs) {
                            System.out.printf("  Item %d (predicted score: %.3f)%n",
                                    score.itemId, score.score);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a numeric user ID or 'exit'.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading data file: " + e.getMessage());
            System.err.println("Make sure the file exists at: " + Paths.get(DATA_FILE_PATH).toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Unexpected error while generating recommendations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadRatings() throws IOException {
        Path path = Paths.get(DATA_FILE_PATH);
        System.out.println("Loading data from: " + path.toAbsolutePath());

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // skip empty or commented lines
            }

            String[] parts = line.split(",");
            if (parts.length != 3) {
                System.err.println("Skipping invalid line: " + line);
                continue;
            }

            long userId = Long.parseLong(parts[0].trim());
            long itemId = Long.parseLong(parts[1].trim());
            double rating = Double.parseDouble(parts[2].trim());

            USER_RATINGS
                    .computeIfAbsent(userId, k -> new HashMap<>())
                    .put(itemId, rating);
        }

        System.out.println("Loaded ratings for " + USER_RATINGS.size() + " users.");
    }

    private static List<ItemScore> recommendForUser(long targetUserId, int howMany) {
        Map<Long, Double> targetRatings = USER_RATINGS.get(targetUserId);
        if (targetRatings == null || targetRatings.isEmpty()) {
            System.out.println("No ratings found for user " + targetUserId + ".");
            return Collections.emptyList();
        }

        Map<Long, Double> numerators = new HashMap<>();
        Map<Long, Double> denominators = new HashMap<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : USER_RATINGS.entrySet()) {
            long otherUserId = entry.getKey();
            if (otherUserId == targetUserId) {
                continue;
            }

            double similarity = pearsonSimilarity(targetRatings, entry.getValue());
            if (similarity <= 0.0) {
                continue; // only use positively correlated users
            }

            for (Map.Entry<Long, Double> itemRating : entry.getValue().entrySet()) {
                long itemId = itemRating.getKey();
                if (targetRatings.containsKey(itemId)) {
                    continue; // already rated by target user
                }

                double weighted = similarity * itemRating.getValue();
                numerators.merge(itemId, weighted, Double::sum);
                denominators.merge(itemId, Math.abs(similarity), Double::sum);
            }
        }

        List<ItemScore> results = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : numerators.entrySet()) {
            long itemId = entry.getKey();
            double num = entry.getValue();
            double den = denominators.getOrDefault(itemId, 0.0);
            if (den == 0.0) {
                continue;
            }
            double predicted = num / den;
            results.add(new ItemScore(itemId, predicted));
        }

        results.sort((a, b) -> Double.compare(b.score, a.score));

        if (results.size() > howMany) {
            return new ArrayList<>(results.subList(0, howMany));
        }
        return results;
    }

    private static double pearsonSimilarity(Map<Long, Double> a, Map<Long, Double> b) {
        // Find common items
        Set<Long> common = new HashSet<>(a.keySet());
        common.retainAll(b.keySet());
        if (common.isEmpty()) {
            return 0.0;
        }

        int n = common.size();
        double sumA = 0.0;
        double sumB = 0.0;
        double sumA2 = 0.0;
        double sumB2 = 0.0;
        double sumProd = 0.0;

        for (Long itemId : common) {
            double ra = a.get(itemId);
            double rb = b.get(itemId);

            sumA += ra;
            sumB += rb;
            sumA2 += ra * ra;
            sumB2 += rb * rb;
            sumProd += ra * rb;
        }

        double numerator = sumProd - (sumA * sumB / n);
        double denominator = Math.sqrt(sumA2 - (sumA * sumA / n)) *
                             Math.sqrt(sumB2 - (sumB * sumB / n));

        if (denominator == 0.0) {
            return 0.0;
        }
        return numerator / denominator;
    }

    private static class ItemScore {
        final long itemId;
        final double score;

        ItemScore(long itemId, double score) {
            this.itemId = itemId;
            this.score = score;
        }
    }
}


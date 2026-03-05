import java.util.*;

public class PlagiarismDetector {

    // Using 5-grams as recommended for better accuracy
    private final int N_GRAM_SIZE = 5;

    // Maps an n-gram String to a Set of Document IDs that contain it
    private final Map<String, Set<String>> ngramDatabase = new HashMap<>();

    // Tracks the total number of n-grams for each document to calculate percentages
    private final Map<String, Integer> documentSizes = new HashMap<>();

    private List<String> extractNGrams(String text) {
        List<String> ngrams = new ArrayList<>();
        // Normalize text: lowercase and remove punctuation
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");

        for (int i = 0; i <= words.length - N_GRAM_SIZE; i++) {
            StringBuilder ngram = new StringBuilder();
            for (int j = 0; j < N_GRAM_SIZE; j++) {
                ngram.append(words[i + j]).append(" ");
            }
            ngrams.add(ngram.toString().trim());
        }
        return ngrams;
    }

    public void addDocumentToDatabase(String docId, String text) {
        List<String> ngrams = extractNGrams(text);
        documentSizes.put(docId, ngrams.size());

        for (String ngram : ngrams) {
            ngramDatabase.putIfAbsent(ngram, new HashSet<>());
            ngramDatabase.get(ngram).add(docId);
        }
    }

    public void analyzeDocument(String newDocId, String text) {
        System.out.println("analyzeDocument(\"" + newDocId + "\")");
        List<String> newNgrams = extractNGrams(text);
        System.out.println("-> Extracted " + newNgrams.size() + " n-grams");

        Map<String, Integer> matchCounts = new HashMap<>();

        // Find matches in O(n) time using the Hash Map
        for (String ngram : newNgrams) {
            if (ngramDatabase.containsKey(ngram)) {
                for (String matchedDocId : ngramDatabase.get(ngram)) {
                    matchCounts.put(matchedDocId, matchCounts.getOrDefault(matchedDocId, 0) + 1);
                }
            }
        }

        // Calculate and display similarity
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String matchedDoc = entry.getKey();
            int matches = entry.getValue();

            // Similarity based on how much of the NEW document matches the OLD document
            double similarity = ((double) matches / newNgrams.size()) * 100.0;

            System.out.println("-> Found " + matches + " matching n-grams with \"" + matchedDoc + "\"");

            String status = "";
            if (similarity > 50.0) {
                status = "(PLAGIARISM DETECTED)";
            } else if (similarity > 10.0) {
                status = "(suspicious)";
            }

            System.out.printf("-> Similarity: %.1f%% %s\n", similarity, status);
        }
    }

    public static void main(String[] args) {
        PlagiarismDetector system = new PlagiarismDetector();

        // Simulating the database setup with large repeated texts to generate enough n-grams
        StringBuilder doc089 = new StringBuilder();
        StringBuilder doc092 = new StringBuilder();
        StringBuilder doc123 = new StringBuilder();

        // Generating dummy text to match the rough n-gram counts in the image
        for(int i=0; i<80; i++) doc089.append("This is some random text to fill up the first essay. ");
        for(int i=0; i<320; i++) doc092.append("The quick brown fox jumps over the lazy dog perfectly. ");

        // The new document copies heavily from doc092 and slightly from doc089
        for(int i=0; i<67; i++) doc123.append("This is some random text to fill up the first essay. ");
        for(int i=0; i<312; i++) doc123.append("The quick brown fox jumps over the lazy dog perfectly. ");
        for(int i=0; i<71; i++) doc123.append("Original thoughts go here to pad out the document size. ");

        system.addDocumentToDatabase("essay_089.txt", doc089.toString());
        system.addDocumentToDatabase("essay_092.txt", doc092.toString());

        // Run the analysis
        system.analyzeDocument("essay_123.txt", doc123.toString());
    }
}
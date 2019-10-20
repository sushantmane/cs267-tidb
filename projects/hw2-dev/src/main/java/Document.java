import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document {

    // punctuation normalization
    private final boolean pNormalization = true;
    // case normalization
    private final boolean cNormalization = true;
    // map of token:within-document-positions
    private Map<String, List<Long>> positions = new HashMap<>();
    // ld - document length (measured in tokens)
    private long length;
    // unique document id for this doc
    private int id;

    void setId(int id) {
        this.id = id;
    }

    void getId(int id) {
        this.id = id;
    }

    Document(List<String> lines) {
        long position = 0;
        for (String line : lines) {
            String[] tokens = line.trim().split(" ");
            for (String token : tokens) {
                position++;
                token = Utils.normalizeToken(token);
                List<Long> postings = positions.get(token);
                if (postings == null) {
                    postings = new ArrayList<>();
                    positions.put(token, postings);
                }
                postings.add(position);
            }
        }
        length = position;
    }

    public Map<String, List<Long>> getPostings() {
        return this.positions;
    }

    long getLength() {
        return length;
    }
}
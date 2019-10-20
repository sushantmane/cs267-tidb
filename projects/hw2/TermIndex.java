import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TermIndex {

    // map of docId:freq -- F(t,d) - the number of times term appears in each document
    private Map<Integer, Long> ftd = new HashMap<>();
    // posting list: mapping of docId:within-document-positions
    private Map<Integer, List<Long>> postings = new TreeMap<>();
    // Nt - the number of documents in the collection containing the term
    private int numDocs = 0;
    // token
    private String term;
    // total number of occurrences across all the documents
    private long freq = 0;

    int getNumDocs() {
        return numDocs;
    }

    List<Long> getDocPostings(int docId) {
        return postings.get(docId);
    }

    Map getPostings() {
        return postings;
    }

    long getFreqInDoc(int docId) {
        if (!ftd.containsKey(docId)) {
            return 0;
        }
        return ftd.get(docId);
    }

    public TermIndex(String term) {
        this.term = term;
    }

    // adds position info for current
    void add(int docId, long position) {
        List<Long> offsets = postings.get(docId);
        if (offsets == null) {
            offsets = new ArrayList<>();
            postings.put(docId, offsets);
            numDocs++;
        }
        offsets.add(position);
        ftd.merge(docId, 1L, Long::sum);
        freq++;
    }

    void add(int docId, List<Long> positions) {
        if (!postings.containsKey(docId)) {
            numDocs++;
        }
        postings.computeIfAbsent(docId, k -> new ArrayList<>()).addAll(positions);
        ftd.merge(docId, (long) positions.size(), Long::sum);
        freq += positions.size();
    }

    long removeDoc(int docId) {
        if (!postings.containsKey(docId)) {
            return 0;
        }
        long termFreq = ftd.get(docId);
        ftd.remove(docId);
        postings.remove(docId);
        freq = freq - termFreq;
        numDocs--;
        return termFreq;
    }

    long getFreq() {
        return freq;
    }
}

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

public class InvertedIndex {

    private Map<String, TermIndex> index = new TreeMap<>();
    // punctuation normalization
    private final boolean pNormalization = true;
    // N (document count): number of documents in the collection
    private int numDocs = 0;
    // total number of tokens across all documents
    private long numTokens = 0;
    // docid index
    private Map<String, List<Integer>> docidIndex = new TreeMap<>();
    // docIds of all docs in this index
    private Set<Integer> docIds = new HashSet<>();

    Map<String, TermIndex> getIndex() {
        return index;
    }

    public void printIndex() {
        printStats();
        String termRecord;
        TermIndex termIndex;
        for (String term : index.keySet()) {
            termIndex = index.get(term);
            termRecord = term + " --> " + termIndex.getNumDocs() + ":" + termIndex.getFreq() + ":";
            Map<Integer, List<Long>> postings = termIndex.getPostings();
            int i = 1;
            for (int docId : postings.keySet()) {
                termRecord += "(" + docId + ", " + termIndex.getFreqInDoc(docId) + " ,"
                        + postings.get(docId).toString() + ")";
                if (i++ < termIndex.getNumDocs()) {
                    termRecord += ", ";
                }
            }
            System.out.println(termRecord);
        }
    }

    public void addDoc(Document doc) {
        Map<String, List<Long>> postings = doc.getPostings();
        // if doc is empty we won't add it inverted index
        // also this doc will not be considered while generating
        // document ids
        if (postings.size() == 0) {
            return;
        }
        numDocs++;
        int docId = numDocs;
        for (String term : postings.keySet()) {
            TermIndex termIndex = index.computeIfAbsent(term, k -> new TermIndex(term));
            termIndex.add(docId, postings.get(term));

            // update docidIndex
            docidIndex.computeIfAbsent(term, k -> new ArrayList<>()).add(docId);
        }
        numTokens += doc.getLength();
        docIds.add(docId);
    }

    void removeDocs(Set<Integer> docs) {
        for (int docId : docs) {
            removeDoc(docId);
        }
    }

    void removeDoc(int docId) {
        TermIndex ti;
        long tokensRemoved = 0;
        for (String term : index.keySet()) {
            ti = index.get(term);
            tokensRemoved = ti.removeDoc(docId);
        }
        numDocs--;
        numTokens -= tokensRemoved;
        docIds.remove(docId);
        removeZeroRefTerms();
        // update docidIndex as well
    }

    Set<Integer> getDocIds() {
        return docIds;
    }

    int getNumDocs() {
        return numDocs;
    }

    // Lavg (average length): average document length across the collection
    long getAvgLenOfDoc() {
        return Math.round((float) numTokens / numDocs);
    }

    long getFtd(String term, int docId) {
        return index.get(term).getFreqInDoc(docId);
    }

    // Nt - the number of documents in the collection containing the term
    int numDocsWithTerm(String term) {
        return index.get(term).getNumDocs();
    }

    // following methods operate on docidIndex
    int firstDoc(String term) {
        List<Integer> pt = docidIndex.get(term);
        if (pt != null && pt.size() > 0) {
            return pt.get(0);
        }
        return -1;
    }

    int lastDoc(String term) {
        List<Integer> pt = docidIndex.get(term);
        if (pt != null && pt.size() > 0) {
            return pt.get(pt.size() - 1);
        }
        return -1;
    }

    private Map<String, Integer> ndCache = new HashMap<>();

    int nextDoc(String term, int current) {
        List<Integer> pt = docidIndex.get(term);
        ndCache.putIfAbsent(term, 0);
        int ct = ndCache.get(term);

        // posting list does not exists or is empty for this term
        if (pt == null || pt.size() == 0) {
            return -1;
        }
        // next(t, +infinity): +infinity
        // current is greater than or equal to the largest docId in posting
        if (pt.get(pt.size() - 1) <= current) {
            return -1;
        }
        // next(t, -infinity): first(t)
        // if first docId in posting is greater than current then return first docId
        if (pt.get(0) > current) {
            ct = 0;
            ndCache.put(term, ct);
            return pt.get(ct);
        }
        // if cached docId is less than or equal to current
        // set lower bound to cache
        int low = 0;
        if (ct > 0 && pt.get(ct - 1) <= current) {
            low = ct - 1;
        } else {
            low = 0;
        }
        // get upper bound
        int jump = 1;
        int high = low + jump;
        while (high < pt.size() && pt.get(high) <= current) {
            low = high;
            jump = 2 * jump;
            high = low + jump;
        }
        if (high >= pt.size()) {
            high = pt.size() - 1;
        }
        ct = binarySearch(term, low, high, current);
        ndCache.put(term, ct);
        return pt.get(ct);
    }

    // this impl assumes that Pt[low] <= current and Pt[high] > current
    int binarySearch(String term, int low, int high, int current) {
        List<Integer> pt = docidIndex.get(term);
        while (high - low > 1) {
            int mid = (low + high) / 2;
            if (pt.get(mid) <= current) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return high;
    }

    private Map<String, Integer> pdCache = new HashMap<>();

    int prevDoc(String term, int current) {
        List<Integer> pt = docidIndex.get(term);
        pdCache.putIfAbsent(term, 0);
        int ct = pdCache.get(term);

        // posting list does not exists or is empty for this term
        if (pt == null || pt.size() == 0) {
            return -1;
        }
        // prev(t, -infinity): should return -infinity
        if (pt.get(0) >= current) {
            return -1;
        }
        // prev(t, +infinity): should return last element
        if (pt.get(pt.size() - 1) < current) {
            ct = pt.size() - 1;
            pdCache.put(term, ct);
            return pt.get(ct);
        }

        int jump = 1;
        int low = 0;
        int high = low + jump;
        while (high < pt.size() && pt.get(high) < current) {
            low = high;
            jump = 2 * jump;
            high = low + jump;
        }
        if (high >= pt.size()) {
            high = pt.size() - 1;
        }
        ct = binarySearchPrev(term, low, high, current);
        pdCache.put(term, ct);
        return pt.get(ct);
    }

    int binarySearchPrev(String term, int low, int high, int current) {
        List<Integer> pt = docidIndex.get(term);
        while (high - low > 1) {
            int mid = (low + high) / 2;
            if (pt.get(mid) < current) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return low;
    }

    Set<Integer> getFilteredDocs(String query) {
        Map<String, Set<Integer>> res = new HashMap<>();
        String[] qt = query.trim().split(" ");
        Stack<String> stack = new Stack<>();
        for (int i = qt.length - 1; i >= 0; i--) {
            String token = qt[i];
            if (token.startsWith("_") && (token.equals("_OR") || token.equals("_AND"))) {
                Set<Integer> s1;
                String t1 = stack.pop();
                if (t1.startsWith("___REST___")) {
                    s1 = res.get(t1);
                } else {
                    List<Integer> s1p = docidIndex.get(t1);
                    if (s1p == null) {
                        s1 = new HashSet<>();
                    } else {
                        s1 = new HashSet<>(s1p);
                    }
                }

                Set<Integer> s2;
                String t2 = stack.pop();
                if (t2.contains("___REST___")) {
                    s2 = res.get(t2);
                } else {
                    List<Integer> s2p = docidIndex.get(t2);
                    if (s2p == null) {
                        s2 = new HashSet<>();
                    } else {
                        s2 = new HashSet<>(s2p);
                    }
                }
                if (token.equals("_OR")) {
                    s1.addAll(s2);
                } else {
                    s1.retainAll(s2);
                }
                String key = "___REST___" + i;
                res.put(key, s1);
                stack.push(key);
            } else {
                stack.push(normalizeToken(token));
            }
        }
        if (stack.size() != 1 ) {
            throw new InvalidParameterException("Invalid query");
        }
        return res.get(stack.pop());
    }

    private String normalizeToken(String token) {
        token = token.toLowerCase();
        if (pNormalization) {
            token = token.replaceAll("[-|.|,|?|'|\"|(|)|;|:|!]", "");
        }
        return token;
    }

    void removeZeroRefTerms() {
        // remove tokens with zero doc refs
        Set<String> terms = new HashSet<>(index.keySet());
        for (String term : terms) {
            if (index.get(term).getNumDocs() == 0) {
                index.remove(term);
            }
        }
    }
}

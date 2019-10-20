import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Vectors {

    private InvertedIndex invIndex;

    Vectors(InvertedIndex invIndex) {
        this.invIndex = invIndex;
    }

    Map<Integer, Double> computRanks(Set<Integer> docs, String query) {
        Map<Integer, Double> rankedList = new TreeMap<>();
        double[] qv = getNormalizedQv(query);
        for (int docId : docs) {
            double[] dv = getNormalizedDv(docId);
            rankedList.put(docId, (getCosSim(qv, dv)));
        }
        return rankedList;
    }

    double[] computeQv(String query) {
        Map<String, Integer> tfq = new TreeMap<>();
        for (String token : query.split(" ")) {
            if (token.startsWith("_") && (token.equals("_OR") || token.equals("_AND"))) {
                continue;
            }
            token = Utils.normalizeToken(token);
            tfq.merge(token, 1, Integer::sum);
        }
        double[] qv = new double[invIndex.getIndex().size()];
        int i = 0;
        for (String term : invIndex.getIndex().keySet()) {
            double tf = getTF(tfq.computeIfAbsent(term, k -> 0));
            double idf = getIDF(term);
            double wt = tf * idf;
            qv[i] = wt; // round numbers to 2 decimal places
            i++;
        }
        return qv;
    }

    void printVector(int id, double[] dv) {
        System.out.println("docID:" + id);
        for (double wt : dv) {
            System.out.print(Utils.round2dp(wt) + " ");
        }
        System.out.println();
    }

    double[] getNormalizedDv(int docId) {
        double[] dv = computDocVector(docId);
        return normalizeVec(dv);
    }

    double[] getNormalizedQv(String query) {
        return normalizeVec(computeQv(query));
    }

    double getCosSim(double[] qv, double[] dv) {
        double sum = 0;
        for (int i = 0; i < qv.length; i++) {
            sum += qv[i] * dv[i];
        }
        return sum;
    }

    double[] computDocVector(int docId) {
        double[] dv = new double[invIndex.getIndex().size()];
        int i = 0;
        for (String term : invIndex.getIndex().keySet()) {
            double tf = getTF(term, docId);
            double idf = getIDF(term);
            double wt = tf * idf;
            dv[i] = wt; // round numbers to 2 decimal places
            i++;
        }
        return dv;
    }

    double[] normalizeVec(double[] dv) {
        double ln = getVecLength(dv);
        for (int i = 0; i < dv.length; i++) {
            dv[i] = dv[i] / ln;
        }
        return dv;
    }

    double getVecLength(double[] vec) {
        double sum = 0;
        for (double wt : vec) {
            sum += Math.pow(wt, 2);
        }
        return Math.sqrt(sum);
    }

    // compute TF (term frequency) for term:docID pair
    double getTF(String term, int docId) {
        long ftd = invIndex.getFtd(term, docId);
        return getTF(ftd);
    }

    double getTF(double ft) {
        if (ft > 0) {
            return Utils.log2(ft) + 1;
        }
        return 0;
    }

    // compute IDF (inverse document frequency)
    double getIDF(String term) {
        int numDocs = invIndex.getNumDocs();
        int numDocsWithTerm = invIndex.numDocsWithTerm(term);
        return Utils.log2((double) numDocs / numDocsWithTerm);
    }
}

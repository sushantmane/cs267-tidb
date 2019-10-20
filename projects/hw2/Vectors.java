import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Vectors {

    private InvertedIndex invIndex;

    Vectors(InvertedIndex invIndex) {
        this.invIndex = invIndex;
    }

    void computRanks(String query, int k) {
        final Comparator<Map.Entry<Integer, Double>> comparator = new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                Double value1 = o1.getValue();
                Double value2 = o2.getValue();
                return value1.compareTo(value2);
            }
        };

        Map<Integer, Double> rankedList = new TreeMap<>();

        double[] qv = getNormalizedQv(query);
        Set<Integer> docs = invIndex.getDocIds();
        for (int docId : docs) {
            double[] dv = getNormalizedDv(docId);
            rankedList.put(docId, (getCosSim(qv, dv)));
        }

        List<Map.Entry<Integer, Double>> entryList = new ArrayList<Map.Entry<Integer, Double>>(rankedList.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        System.out.println("DocId Score");
        for (int i = entryList.size() - 1; i >= 0 && k > 0; i--, k--) {
            Map.Entry<Integer, Double> entry = entryList.get(i);
            System.out.println(entry.getKey() + " " + round2dp(entry.getValue()));
        }
    }

    double[] computeQv(String query) {
        Map<String, Integer> tfq = new TreeMap<>();
        for (String token : query.split(" ")) {
            if (token.startsWith("_") && (token.equals("_OR") || token.equals("_AND"))) {
                continue;
            }
            token = normalizeToken(token);
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
            System.out.print(round2dp(wt) + " ");
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

    static double log2(double d) {
        //return (Math.log(d) / Math.log(2));
        return (Math.log(d) / Math.log(2) + 1e-10);
    }

    static double round2dp(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    // compute TF (term frequency) for term:docID pair
    double getTF(String term, int docId) {
        long ftd = invIndex.getFtd(term, docId);
        return getTF(ftd);
    }

    double getTF(double ft) {
        if (ft > 0) {
            return log2(ft) + 1;
        }
        return 0;
    }

    // compute IDF (inverse document frequency)
    double getIDF(String term) {
        int numDocs = invIndex.getNumDocs();
        int numDocsWithTerm = invIndex.numDocsWithTerm(term);
        return log2((double) numDocs / numDocsWithTerm);
    }

    private String normalizeToken(String token) {
        boolean pNormalization = true;
        token = token.toLowerCase();
        if (pNormalization) {
            token = token.replaceAll("[-|.|,|?|'|\"|(|)|;|:|!]", "");
        }
        return token;
    }
}

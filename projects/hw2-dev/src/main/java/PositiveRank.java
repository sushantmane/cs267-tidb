import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PositiveRank {

    private InvertedIndex index = new InvertedIndex();

    public void buildIndex(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            List<String> docLines = new ArrayList<>();
            String line = br.readLine();
            while (line != null) {
                if (line.length() == 0) {
                    Document document = new Document(docLines);
                    index.addDoc(document);
                    docLines.clear();
                } else {
                    docLines.add(line);
                }
                line = br.readLine();
            }
            if (line == null) {
                Document document = new Document(docLines);
                index.addDoc(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void printIndex() {
        index.printIndex();
    }

    int nextSolution(String query, int position) {
        int v = index.docRight(query, position);
        if (v == -1) {
            return -1;
        }
        int u = index.docLeft(query, v + 1);
        if (u == v) {
            return u;
        }
        return nextSolution(query, v);
    }

    // returns set of docs after applying boolean filter
    Set<Integer> applyBooleanFilter(String query) {
        int current = 0; //
        Set<Integer> res = new HashSet<>();
        while (current != -1) {
            current = nextSolution(query, current);
            System.out.println(current);
            if (current != -1) {
                res.add(current);
            }
        }
        return res;
    }

    public void displayRankedList(Map<Integer, Double> rankedList, int numOfRes) {
        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(rankedList.entrySet());
        Collections.sort(entryList, Comparator.comparing(Map.Entry::getValue));
        System.out.println("DocId Score");
        for (int i = entryList.size() - 1; i >= 0 && numOfRes > 0; i--, numOfRes--) {
            Map.Entry<Integer, Double> entry = entryList.get(i);
            System.out.println(entry.getKey() + " " + Utils.round2dp(entry.getValue()));
        }
    }

    public static void main(String[] args) {
        // parse cmd args
        if (args.length < 3) {
            System.err.println("Error: Insufficient Arguments");
            return;
        }
        String inFile = args[0];
        String query = args[2];
        int numRes = 0; // top k score
        try {
            numRes = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid Input");
            return;
        }

        // build index
        PositiveRank pr = new PositiveRank();
        pr.buildIndex(inFile);

        // boolean retrieval
        Set<Integer> brDocs = pr.applyBooleanFilter(query);

        // update index
        // not needed::after discussion with Professor
//        Set<Integer> docs = new HashSet<>(pr.index.getDocIds());
//        docs.removeAll(brDocs);  // removal candidates
//        pr.index.removeDocs(docs);

        // VSM RANK
        Vectors vec = new Vectors(pr.index);
        pr.displayRankedList(vec.computRanks(brDocs, query), numRes);
    }
}

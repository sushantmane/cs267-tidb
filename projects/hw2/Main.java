import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

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

    public static void main(String[] args) {
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
        Main main = new Main();
        main.buildIndex(inFile);
        Set<Integer> fltDocs;
        try {
            fltDocs = main.index.getFilteredDocs(query);
        } catch (InvalidParameterException | EmptyStackException e) {
            System.err.println("Error: Invalid boolean query");
            return;
        }
        Set<Integer> docs = new HashSet<>(main.index.getDocIds());
        docs.removeAll(fltDocs);  // removal candidates
        main.index.removeDocs(docs);
        // VSM RANK
        Vectors vec = new Vectors(main.index);
        vec.computRanks(query, numRes);
    }
}

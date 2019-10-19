import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

class Corpus {
  private File corpus;
  private String docBreak = "---";
  private BufferedReader br;

  public Corpus(File corpus) throws FileNotFoundException {
    this.corpus = corpus;
    this.br = new BufferedReader(new FileReader(corpus));
  }

  public boolean hasNextDoc() {
    boolean result = false;
    try {
      br.mark(1024);
      if (br.readLine() != null) {
        result = true;
      }
      br.reset();
    } catch (IOException e) {
      // no-action
    }
    return result;
  }

  public String getNextDoc() throws IOException {
    String doc = null;
    String line = br.readLine();
    while (line != null) {
      if (line.length() >= 3 && docBreak.equals(line.substring(0, 3))) {
        break;
      }
      doc = doc == null ? line : doc + " " + line;
      line = br.readLine();
    }
    return doc;
  }

  public List<String> getNextDocAsTermsList() throws IOException {
    List<String> terms = Arrays.asList(getNextDoc().trim().split(" "));
    return terms.stream().map(String::toLowerCase).collect(Collectors.toList());
  }
}

class IndexPrinter {

  private Map<String, Map<Integer, List<Integer>>> index = new TreeMap<>();

  private void buildInvertedIndex(File corpusFile, Set<String> excludeTerms) {
    try {
      Corpus corpus = new Corpus(corpusFile);
      int docId = 0;
      while (corpus.hasNextDoc()) {
        docId++;
        List<String> terms = corpus.getNextDocAsTermsList();
        boolean skipDoc = false;
        for (String exTerm : excludeTerms) {
          if (terms.contains(exTerm)) {
            skipDoc = true;
            break;
          }
        }
	      // doc contains exclude terms, skip it
        if (skipDoc) {
          continue;
        }

        int pos = 0;
        for (String term : terms) {
          pos++;
	        // remove punctuations
          term = term.replaceAll("[-|.|,|?|'|\"|(|)|;|:|!]", "");
	  if (term.length() == 0) {
	  	continue;
	  }
          Map<Integer, List<Integer>> posMap = index.get(term);
          if (posMap == null) {
            posMap = new TreeMap<>();
            index.put(term, posMap);
          }
          List<Integer> positions = posMap.get(docId);
          if (positions == null) {
            positions = new ArrayList<>();
            posMap.put(docId, positions);
          }
          positions.add(pos);
        }
      }
    } catch (IOException e) {
	    // no action
    }
  }

  public int getNumOfDocs(String term) {
    Map<Integer, List<Integer>> posMap = index.get(term);
    return posMap == null ? -1 : posMap.size();
  }

  public int getFrequency(String term) {
    Map<Integer, List<Integer>> posMap = index.get(term);
    int freq = 0;
    for (Integer docId : posMap.keySet()) {
      freq += posMap.get(docId).size();
    }
    return freq;
  }

  public int[][] getDocIdPosTuples(String term) {
    Map<Integer, List<Integer>> posMap = index.get(term);
    int[][] posTuple = new int[getFrequency(term)][2];
    int i = 0;
    for (int docId : posMap.keySet()) {
      List<Integer> positions = getPosInDoc(term, docId);
      for (int position : positions) {
          posTuple[i][0] = docId;
          posTuple[i][1] = position;
          i++;
      }
    }
    return posTuple;
  }

  public List<Integer> getPosInDoc(String term, int docId) {
    return index.get(term).get(docId);
  }

  private void printInvertedIndex() {
    for (String term : index.keySet()) {
      int numOfDocs = getNumOfDocs(term);
      int freq = getFrequency(term);
      String str = "";
      int[][] tuple = getDocIdPosTuples(term);
      for (int[] arr : tuple) {
        str += ",(" + arr[0] + "," + arr[1] + ")";
      }
      System.out.println(term + "\n" + numOfDocs + "," + freq + str);
    }
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Please provide a corpus file");
      System.out.println("Usage: java IndexPrinter corpus_file [exclude_term1 exclude_term2 ...]");
      return;
    }
    File corpus = new File(args[0]);
    if (!corpus.exists()) {
      System.out.println("Error: Can not find a corpus file." + args[0]);
      return;
    }
    List<String> etl = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
    Set<String> exTerms = etl.stream().map(String::toLowerCase).collect(Collectors.toSet());
    IndexPrinter ip = new IndexPrinter();
    ip.buildInvertedIndex(corpus, exTerms);
    ip.printInvertedIndex();
  }
}

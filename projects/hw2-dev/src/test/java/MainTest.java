import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void mainTest_bookCorpus_IdeaSet() {
        String[] args = {"book_corpus.txt", "5", "_OR quarrel sir"};
        PositiveRank.main(args);
    }

    @Test
    void mainTest() {
        String[] args = {"my_corpus.txt", "5", "_OR _AND choose should _OR querty determiNe"};
        PositiveRank.main(args);
    }

    @Test
    void mainTest_bookCorpus() {
//        String[] args = {"book_corpus.txt", "5", "_AND _OR quarrel sir you"};
        String[] args = {"book_corpus.txt", "5", "_AND _OR _AND you sir quarrel MAN"};
        PositiveRank.main(args);
    }

    @Test
    void mainTest_BooleanRetrieval() {
        String[] args = {"book_corpus.txt", "5", "_OR _AND good dog _AND bad cat"};
        String[] bq = args[2].trim().split(" ");
        Stack<String> stack = new Stack<>();
        String op = new String();
        for (String token : bq) {
            if (token.startsWith("_") && (token.equals("_OR") || token.equals("_AND"))) {
                stack.push(token);
                continue;
            } else {
                op += token;
                if (!stack.isEmpty()) {
                    op += stack.pop();
                }
            }
            System.out.println(op);

//            System.out.println(token);
        }
    }
}
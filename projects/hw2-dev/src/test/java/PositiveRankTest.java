import org.junit.jupiter.api.Test;

class PositiveRankTest {

    @Test
    void mainTest_bookCorpus_IdeaSet() {
        String[] args = {"book_corpus.txt", "5", "_AND _OR quarrel sir you"};
        PositiveRank.main(args);
    }

    @Test
    void mainTest_bookCorpus_IdeaSet1() {
        String[] args = {"book_corpus.txt", "5", "quarrel sir"};
        PositiveRank.main(args);
    }

    @Test
    void mainTest_myCorpus() {
        String[] args = {"my_corpus.txt", "5", "_OR _AND choose should _OR querty determiNe"};
        PositiveRank.main(args);
    }

    @Test
    void mainTest_hw2_1() {
        String[] args = {"hw2.txt", "5", "_AND _AND San Jose Climate"};
        PositiveRank.main(args);
    }


    @Test
    void mainTest_hw2_2() {
        String[] args = {"hw2.txt", "5", "_OR _OR San Jose Climate"};
        PositiveRank.main(args);
    }
}
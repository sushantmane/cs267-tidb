Step-1: Compile
```
  javac IndexPrinter.java
```

Step-2: Run
```
  java IndexPrinter corpus_file [exclude_term1 exclude_term2 ...]
```

eg:
```
  java IndexPrinter troy_corpus.data achilles hector
```


# Data structure used to maintain an inverted index is Map (nested Map with list as values):

```
{
    term1 : {
            docId1 : [pos1, pos2, ..., posN],
            docId5 : [pos11, pos24, ..., posN],
            .
            .
            docIdX : [posA, posB, ..., posZ]
    },
    term2 : {
              docId11 : [pos11, pos21, ..., posN1],
              docId51 : [pos111, pos124, ..., posN1],
              .
              .
              docIdX1 : [posA1, posB1, ..., posZ1]
              },
      .
      .
      .
    termN : {
                docId1N : [pos1, pos2, ..., posNN],
                docId5N : [pos11, pos24, ..., posNN],
                .
                .
                docIdX2 : [posAN, posB, ..., posZN]
                }
}
```

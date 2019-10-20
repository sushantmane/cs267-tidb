import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

class Node {

    String term;
    Node left;
    Node right;
    boolean operator;

    Node(String term, boolean op) {
        this.term = term;
        this.operator = op;
        this.left = null;
        this.right = null;
    }

    boolean isOperator() {
        return operator;
    }
}

enum OPERATION {
    LEFT,
    RIGHT,
}

public class BooleanQueryParser {

    private Node root = null;

    BooleanQueryParser(String query) {
        buildTree(query);
    }

    void buildTree(String query) {
        Stack<Node> stack = new Stack<>();
        List<String> list = Arrays.asList(query.trim().split(" "));
        Collections.reverse(list);
        for (String token : list) {
            boolean op = isOperator(token);
            if (!op) {
                token = Utils.normalizeToken(token);
            }

            Node newNode = new Node(token, op);
            if (op) {
                newNode.left = stack.pop();
                newNode.right = stack.pop();
                stack.push(newNode);
                continue;
            }
            stack.push(newNode);
        }
        root = stack.pop();
    }

    private boolean isOperator(String term) {
        return term.startsWith("_") && (term.equals("_OR") || term.equals("_AND"));
    }

    public int evaluate(InvertedIndex index, OPERATION op, int current) {
        return postfix(root, index, op, current);
    }

    private int postfix(Node root, InvertedIndex index, OPERATION op, int current) {
        if (root == null) {
            return 0;
        }
        int a = postfix(root.left, index, op, current);
        int b = postfix(root.right, index, op, current);
        if (root.isOperator() && root.term.equals("_AND")) {
            return Math.max(a , b);
        }
        if (root.isOperator() && root.term.equals("_OR")) {
            return Math.min(a , b);
        }
        if (op == OPERATION.LEFT) {
            return index.prevDoc(root.term, current);
        }
        return index.nextDoc(root.term, current);
    }
}
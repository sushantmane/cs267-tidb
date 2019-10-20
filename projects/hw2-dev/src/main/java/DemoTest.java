//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Stack;
//
//
//class Node {
//    String term;
//    boolean operator;
//    Node left;
//    Node right;
//
//    Node(String term, boolean op) {
//        this.term = term;
//        this.operator = op;
//        this.left = null;
//        this.right = null;
//    }
//
//    String getTerm() {
//        return term;
//    }
//
//    boolean isOperator() {
//        return operator;
//    }
//}
//
//class QueryParser {
//
//    Node root = null;
//
//    QueryParser(String query) {
//        buildTree(query);
//    }
//
//    void buildTree(String query) {
//        Stack<Node> stack = new Stack<>();
//        List<String> list = Arrays.asList(query.trim().split(" "));
//        Collections.reverse(list);
//        for (String token : list) {
//            boolean op = isOperator(token);
//            Node newNode = new Node(token, op);
//            if (op) {
//                newNode.left = stack.pop();
//                newNode.right = stack.pop();
//                stack.push(newNode);
//                continue;
//            }
//            stack.push(newNode);
//        }
//        root = stack.pop();
//    }
//
//    boolean isOperator(String term) {
//        if (term.startsWith("_") && (term.equals("_OR") || term.equals("_AND"))) {
//            return true;
//        }
//        return false;
//    }
//
//    static Map<String, Integer> map;
//
//    {
//        map = new HashMap<>();
//        map.put("quarrel",2);
//        map.put("sir",2);
//        map.put("you",3);
//        map.put("term",7);
//    }
//
//    public int infixTrav(Node root, int current) {
//        if (root == null) {
//            return 0;
//        }
//        int a = infixTrav(root.left, current);
//        int b = infixTrav(root.right, current);
//        if (root.isOperator() && root.term.equals("_AND")) {
//            return Math.max(a , b);
//        }
//        if (root.isOperator() && root.term.equals("_OR")) {
//            return Math.min(a , b);
//        }
//        return map.get(root.term);
//    }
//}
//
//public class DemoTest {
//
//    public static void main(String[] args) {
//        String query = "_AND _OR quarrel sir you";
//        QueryParser parser = new QueryParser(query);
//        int res = parser.infixTrav(parser.root, 3);
//        System.out.println("res: " + res);
//    }
//}

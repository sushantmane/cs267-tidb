import java.util.Stack;

public class BinarySearch {

    class Node {
        int data;
        Node left;
        Node right;

        Node(int data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }

    Node root = null;

    void insertNode(Node root, Node node) {
        if (this.root == null) {
            this.root = node;
            return;
        }

        Node ptr = this.root;
        Node ptrPrime = ptr;
        while (ptr != null) {
            ptrPrime = ptr;
            if (node.data < ptr.data) {
                ptr = ptr.left;
            } else {
                ptr = ptr.right;
            }
        }

        if (node.data < ptrPrime.data) {
            ptrPrime.left = node;
        } else {
            ptrPrime.right = node;
        }
    }

    void buildBST(int[] input) {
        for (int i : input) {
            Node node = new Node(i);
            insertNode(root, node);
        }
    }

    void printPreorder(Node root) {
        if (root == null) {
            return;
        }
        System.out.print(root.data + " ");
        printPreorder(root.left);
        printPreorder(root.right);
    }

    void printInorder(Node root) {
        if (root == null) {
            return;
        }
        printInorder(root.left);
        System.out.print(root.data + " ");
        printInorder(root.right);
    }

    void printPostorder(Node root) {
        if (root == null) {
            return;
        }
        printPostorder(root.left);
        printPostorder(root.right);
        System.out.print(root.data + " ");
    }

    void printInorderWoRec() {
        Stack<Node> stack = new Stack<>();

        if (root == null) {
            return;
        }

        Node ptr = root;
        while (true) {
            while (ptr != null) {
                stack.push(ptr);
                ptr = ptr.left;
            }
            if (stack.isEmpty()) {
                break;
            }
            Node curr = stack.peek();
            System.out.print(curr.data + " ");
            ptr = curr.right;
        }
    }

    public static void main(String[] args) {
        int[] input = {34, 14, 11, 67, 64, 98, 28, 59, 45, 95, 27, 68, 42};
        BinarySearch bs = new BinarySearch();
        bs.buildBST(input);
        bs.printPreorder(bs.root);
        System.out.println();
        bs.printInorder(bs.root);
        System.out.println();
        bs.printPostorder(bs.root);
        System.out.println();
        bs.printInorderWoRec();
    }
}

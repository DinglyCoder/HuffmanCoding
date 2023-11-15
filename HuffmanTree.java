/*  Student information for assignment:
 *
 *  On our honor, Akshat and Sankarsh, this programming assignment is our own work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: ak49553
 *  email address: akshat_kumar@utexas.edu
 *  Grader name: Casey
 *
 *  Student 2
 *  UTEID: skn636
 *  email address: kaushikskn@outlook.com
 *
 */

public class HuffmanTree {
    TreeNode root;
    
    public HuffmanTree(TreeNode root) {
        this.root = root;
    }

    // returns the root of the huffman tree
    public TreeNode getRoot() {
        return this.root;
    }

    //sets the root instance variable to the passed in TreeNode
    public void setRoot(TreeNode root) {
        this.root = root;
    }
    
}
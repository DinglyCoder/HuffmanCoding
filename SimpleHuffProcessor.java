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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private int headerConstant;
    private HuffmanTree huffTree;
    private int numNodes;
    private int numLeaves;
    private String[] codeTable;
    private int[] frequenciesTable;
    private int savedBits;
    private int compressedBits;
    private int ogBitCount;
    private boolean preprocessCalled;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * 
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind
     *                     of
     *                     header to use, standard count format, standard tree
     *                     format, or
     *                     possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     *         Note, to determine the number of
     *         bits saved, the number of bits written includes
     *         ALL bits that will be written including the
     *         magic number, the header format number, the header to
     *         reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        headerConstant = headerFormat;
        huffTree = null;
        ogBitCount = 0;
        numNodes = 0;
        numLeaves = 0;
        codeTable = null;
        frequenciesTable = null;
        savedBits = 0;
        compressedBits = 0;
        preprocessCalled = true;
        PriorityQueue<TreeNode> huffQueue = new PriorityQueue<>();

        // get frequencies
        frequenciesTable = getFrequencies(in);

        // fill priority queue
        for (int i = 0; i < frequenciesTable.length; i++) {
            if (frequenciesTable[i] != 0) {
                TreeNode node = new TreeNode(i, frequenciesTable[i]);
                huffQueue.enqueue(node);
            }
        }

        // build huffman tree
        numNodes = buildTree(huffQueue);
        huffTree = new HuffmanTree(huffQueue.dequeue());

        // build code table with tree
        codeTable = buildTable(huffTree.getRoot());

        compressedBits = calcSavedBits(codeTable, frequenciesTable, headerFormat, numNodes);
        savedBits = ogBitCount - compressedBits;
        return savedBits;
    }

    // helper method to test whether the root node of the priority queue works as
    // intended
    private void printTree(TreeNode root) {
        printTree(root, "");
    }

    // helper method that performs the recursive call for printTree
    private void printTree(TreeNode n, String spaces) {
        if (n != null) {
            printTree(n.getRight(), spaces + "  ");
            // myViewer.update(spaces + (char) n.getValue());
            printTree(n.getLeft(), spaces + "  ");
        }
    }

    // return array that stores frequency of each int (int is index and val is freq)
    private int[] getFrequencies(InputStream in) {
        try {
            int[] frequencies = new int[ALPH_SIZE + 1];
            BitInputStream bIn = new BitInputStream(in);
            int val = bIn.readBits(BITS_PER_WORD);
            while (val != -1) {
                ogBitCount += BITS_PER_WORD;
                frequencies[val]++;
                val = bIn.readBits(BITS_PER_WORD);
            }
            // add PEOF to freq table
            frequencies[ALPH_SIZE] = 1;
            bIn.close();
            return frequencies;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }

    // constructs the huffman tree from the huffman queue
    private int buildTree(PriorityQueue<TreeNode> huffQueue) {
        int numNodes = huffQueue.size();
        while (huffQueue.size() > 1) {
            TreeNode firstNode = huffQueue.dequeue();
            TreeNode secondNode = huffQueue.dequeue();

            TreeNode newNode = new TreeNode(firstNode, -1, secondNode);
            huffQueue.enqueue(newNode);
            numNodes++;
        }
        return numNodes;
    }

    // bulids an array table that stores tree paths for each element in tree
    private String[] buildTable(TreeNode root) {
        String[] codeTable = new String[ALPH_SIZE + 1];

        buildTableHelper(root, new StringBuilder(), codeTable);
        return codeTable;
    }

    // helper method that does the recursive call for buildTable
    private void buildTableHelper(TreeNode currNode, StringBuilder path, String[] codeTable) {
        // base case: if node is null, do nothing
        if (currNode != null) {
            // base case: leaf node
            if (currNode.isLeaf()) {
                codeTable[currNode.getValue()] = path.toString();
            } else {
                // traverse left
                path.append("0");
                buildTableHelper(currNode.getLeft(), path, codeTable);
                path.delete(path.length() - 1, path.length());

                // traverse right
                path.append("1");
                buildTableHelper(currNode.getRight(), path, codeTable);
                path.delete(path.length() - 1, path.length());
            }
        }
    }

    // calculates the number of bits in the compressed file
    private int calcSavedBits(String[] codeTable, int[] freqs, int headerConstant, int numNodes) {
        int compressedBitLength = 0;
        // number bits taken by data after header
        for (int i = 0; i < codeTable.length; i++) {
            if (freqs[i] != 0) {
                numLeaves++;
                compressedBitLength += freqs[i] * codeTable[i].length();
            }
        }
        compressedBitLength += BITS_PER_INT * 2; // magic no, header constant
        if (headerConstant == STORE_TREE) {
            compressedBitLength += BITS_PER_INT;    // size of tree int
            compressedBitLength += numNodes + numLeaves * (BITS_PER_WORD + 1); // bits for tree
        } else if (headerConstant == STORE_COUNTS) {
            compressedBitLength += BITS_PER_INT * ALPH_SIZE; // size of table
        }

        return compressedBitLength;
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br>
     * pre: <code>preprocessCompress</code> must be called before this method
     * 
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than
     *              the input file.
     *              If this is false do not create the output file if it is larger
     *              than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        // check precondition
        if (!preprocessCalled) {
            throw new IOException();
        }
        if (savedBits <= 0 && !force) {
            myViewer.showError("Compressed file has " + (-1 * savedBits) + " more bits than " +
                    "uncompressed file.\nSelect \"force compression\" option to compress.");
        }
        if (savedBits > 0 || force) {
            BitOutputStream bOut = new BitOutputStream(out);
            BitInputStream bIn = new BitInputStream(in);
            writeHeader(bOut);

            // write compressed data
            writeCompressedData(bIn, bOut);

            bOut.close();
            return compressedBits;
        }
        preprocessCalled = false;
        headerConstant = 0;
        return 0;
    }

    public void writeHeader(BitOutputStream bOut) {
        // write magic number
        bOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        // write header format
        bOut.writeBits(BITS_PER_INT, headerConstant);

        if (headerConstant == STORE_TREE) {
            // write tree length
            bOut.writeBits(BITS_PER_INT, numNodes + numLeaves * (BITS_PER_WORD + 1));
            makeTreeHeader(bOut, huffTree.getRoot());
        } else if (headerConstant == STORE_COUNTS) {
            makeStandardHeader(bOut);
        } else if (headerConstant == STORE_CUSTOM) {
            // do nothing, to be replaced when other format known
        }
    }

    // helper method to create the header for the Standard Tree Format
    private void makeTreeHeader(BitOutputStream bOut, TreeNode currNode) {
        if (currNode != null) {
            if (currNode.isLeaf()) {
                bOut.writeBits(1, 1);
                bOut.writeBits(BITS_PER_WORD + 1, currNode.getValue());
            } else {
                bOut.writeBits(1, 0);
                makeTreeHeader(bOut, currNode.getLeft());
                makeTreeHeader(bOut, currNode.getRight());
            }
        }
    }

    // helper method to create the header for Standard Tree Format
    private void makeStandardHeader(BitOutputStream bOut) {
        for (int i = 0; i < frequenciesTable.length - 1; i++) {
            bOut.writeBits(BITS_PER_INT, frequenciesTable[i]);
        }
    }

    // writes the main data from the file into the compressed file
    private void writeCompressedData(BitInputStream bIn, BitOutputStream bOut) throws IOException {
        int nextBits = bIn.read();
        while (nextBits != -1) {
            String codedBitString = codeTable[nextBits];
            // write each bit into file
            for (char bit : codedBitString.toCharArray()) {
                bOut.writeBits(1, bit);
            }
            nextBits = bIn.read();
        }

        // write PSEUDO_EOF
        String peofCode = codeTable[ALPH_SIZE];
        for (char bit : peofCode.toCharArray()) {
            bOut.writeBits(1, bit);
        }
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * 
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        BitInputStream bIn = new BitInputStream(in);
        BitOutputStream bOut = new BitOutputStream(out);

        // check magic number
        if (bIn.readBits(BITS_PER_INT) == MAGIC_NUMBER) {
            // creates tree to be used while uncompressing based on header format
            TreeNode rootNode = new TreeNode(-1, -1);
            HuffmanTree dehuffTree = new HuffmanTree(rootNode);
            int tableFormat = bIn.readBits(BITS_PER_INT);
            if (tableFormat == STORE_COUNTS) {
                dehuffTree.setRoot(createTreeFromCounts(bIn));
            } else if (tableFormat == STORE_TREE) {
                int[] treeSize = { bIn.readBits(BITS_PER_INT) };
                dehuffTree.setRoot(createTreeFromBitTree(bIn, treeSize));
            } else {
                bIn.close();
                bOut.close();
                throw new IOException();
            }

            // uses created tree to create un uncompressed file
            int fileSize = createUncompressed(bIn, bOut, dehuffTree.getRoot());
            bIn.close();
            bOut.close();
            return fileSize;
        } else {
            myViewer.showError("Error reading compressed file.\nFile did not start with " +
                    "magic number.");
        }
        bIn.close();
        bOut.close();
        return 0;
    }

    // creates a huffman tree from standard count format header
    private TreeNode createTreeFromCounts(BitInputStream bIn) throws IOException {

        int[] decompFreqs = new int[ALPH_SIZE + 1];
        for (int i = 0; i < decompFreqs.length - 1; i++) {
            decompFreqs[i] = bIn.readBits(BITS_PER_INT);
        }
        // make PEOF have freq of one
        decompFreqs[ALPH_SIZE] = 1;
        PriorityQueue<TreeNode> dehuffQueue = new PriorityQueue<>();
        for (int i = 0; i < decompFreqs.length; i++) {
            if (decompFreqs[i] != 0) {
                TreeNode node = new TreeNode(i, decompFreqs[i]);
                dehuffQueue.enqueue(node);
            }
        }

        // build huffman tree
        buildTree(dehuffQueue);
        TreeNode dehuffTree = dehuffQueue.dequeue();
        return dehuffTree;
    }

    // creates a huffman tree from standard tree format header
    private TreeNode createTreeFromBitTree(BitInputStream bIn, int[] size) throws IOException {
        if (size[0] != 0) {
            // base case, reached leaf node
            if (bIn.readBits(1) == 1) {
                size[0] -= BITS_PER_WORD + 1;
                int readBits = bIn.readBits(BITS_PER_WORD + 1);
                return new TreeNode(readBits, 1);
            } else {
                TreeNode nLeft = createTreeFromBitTree(bIn, size);
                if (nLeft == null) {
                    TreeNode n = new TreeNode(-1, 1);
                    n.setLeft(nLeft);
                }
                TreeNode nRight = createTreeFromBitTree(bIn, size);
                TreeNode n = new TreeNode(nLeft, -1, nRight);
                return n;
            }
        }
        return null;
    }

    // writes to uncompressed file using created tree
    private int createUncompressed(BitInputStream bIn, BitOutputStream bOut, TreeNode root)
            throws IOException {
        boolean foundEOF = false;
        TreeNode temp = root;
        int written = 0;
        while (!foundEOF) {
            while (!temp.isLeaf() && temp != null) {
                int bit = bIn.readBits(1);
                if (bit == -1) {
                    throw new IOException("Error reading compressed file \n"
                            + " Unexpected end of input. No PSEUDO_EOF value");
                } else if (bit == 0) {
                    temp = temp.getLeft();
                } else if (bit == 1) {
                    temp = temp.getRight();
                }
            }
            if (temp.getValue() == PSEUDO_EOF) {
                foundEOF = true;
            } else {
                bOut.writeBits(BITS_PER_WORD, temp.getValue());
                written++;
                temp = root;
            }
        }
        return written * BITS_PER_WORD;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}

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

import java.util.LinkedList;
import java.util.Iterator;

public class PriorityQueue<E extends Comparable<? super E>> {
    LinkedList<E> con;

    public PriorityQueue() {
        con = new LinkedList<>();
    }

    // adds element to correct index based on lowest frequency and breaking ties based on 
    // time
    public void enqueue(E val) {
        Iterator<E> it = con.iterator();
        int index = 0;
        // find insertion index
        while(it.hasNext() && val.compareTo(it.next()) >= 0) {
            index++;
        }

        con.add(index, val);
    }

    // removes from front of queue
    public E dequeue(){
        return con.pollFirst();
    }

    // returns size of internal container
    public int size() {
        return con.size();
    }

    // returns a string representation of the queue contents
    public String toString(){
        StringBuilder st = new StringBuilder();
        Iterator<E> it = con.iterator();
        while(it.hasNext()){
            st.append(it.next().toString() + "\n");
        }

        return st.toString();
    }
}
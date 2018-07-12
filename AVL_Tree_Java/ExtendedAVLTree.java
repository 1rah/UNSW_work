// ======================================================================================	
// ======================================================================================
// 		COMP9024 - Assignment 2 - Irah Wajchman - z3439745
// ======================================================================================


package net.datastructures;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.math.*;

public class ExtendedAVLTree<K,V> extends AVLTree<K,V> {
	
	
	// ======================================================================================
	// 		Question 1: Clone AVLTree Method 
	// ======================================================================================
	
	/* Take the original tree (ot) and create an empty new copy tree (ct)
	 * 
	 * Start at the original tree root node (on) and the copy copy tree root node (cn)
	 * create a left null leaf node  and right null leaf node for for the the copy node,
	 * copy the original node's (on) key and value to the copy node (cn), 
	 * 
	 * Recursively repeat this process each left subtree, and each right subtree.
	 * If a null child node in the original tree is reached, the recursion calls are returned.
	 * 
	 * 
	 * 
	 * Each time an original node is visited, a copy node (cn) is made in the
	 * corresponding position on the copy tree. The copy tree is traversed within the same 
	 * recursion frame as the original tree so that all copied nodes are in an identical
	 * position to the original nodes. 
	 * 
	 * So if the original tree has n elements. 
	 * Each node of the original tree is visited once, ie. there are n recursion frames 
	 * 
	 * Therefore the running time of this program is linearly proportional to the number 
	 * of nodes in the original tree... the program is in O(n)
	 * 
	 */
	
	// Driving method for Clone function
	public static <K,V> AVLTree<K,V> clone(AVLTree<K,V> ot) {
		
		AVLTree<K,V> ct = new AVLTree<>();
		
		if (ot.root.element() == null) { return ct;} // check if cloned tree is empty
		
		// recursively copy the children directly into position, starting with root
		cloneAllChild((AVLNode)ot.root, (AVLNode)ct.root);
		
		ct.size = ot.size;
		ct.numEntries = ot.numEntries;

		
		return ct;
	}
	
	// Recursively visit each of the original tree nodes (on), and each of it's children
	// each time a node is visited, a copy node (c) is made in the corresponding
	// position on the copy tree
	private static <K,V> void cloneAllChild(AVLTree.AVLNode<K,V> on, AVLTree.AVLNode<K,V> cn){
		
		// clone the element, copy the element from on to cn 
		cn.setElement(on.element());
		cn.height = on.getHeight();
		
		// recursively clone the Left Children until a null node is reached
		if (on.getLeft() != null) {
			AVLNode<K,V> l = new AVLNode<K,V>();
			l.setParent(cn);
			cn.setLeft(l);
			cloneAllChild((AVLNode)on.getLeft(), (AVLNode)cn.getLeft());
		}
		
		// recursively clone the Right Children until a null node is reached
		if (on.getRight() != null) {
			AVLNode<K,V> r = new AVLNode<K,V>();
			r.setParent(cn);
			cn.setRight(r);
			cloneAllChild((AVLNode)on.getRight(), (AVLNode)cn.getRight());
		}
		
		return;
	}

	// ======================================================================================
	//		Question 2: Merge AVLTree Method
	// ======================================================================================
	
	/* The merge function is performed by a number of sub-functions:
	 *  
	 *  tree2Array (convert a tree to a sorted array of nodes)
	 *  
	 *  	a recursive function that starts at a tree root, then recursively traverses the tree,
	 * 		visiting each node once, and then copying the value of the visited node into an
	 * 		array of empty nodes.
	 * 
	 * 		The tree is visited in order, so that the resulting tree is ordered smallest to largest.
	 * 		The length of the resulting array is equal to the number of nodes in the tree.
	 * 		
	 * 		For an input tree size of n, since each node is visited once to create a copy of the node,
	 * 		the running time is linear proportional to the number of element in the tree.
	 * 
	 * 				This sub-function is in O(n)
	 * 
	 * 
	 * treeMerge2Array (merge two sorted arrays into a single sorted array)
	 * 
	 * 		for 2 input arrays of size n and m, a new output array of size (n+m) is created.
	 * 		over (n+m) iterations 1 node from the two input arrays is selected for addition
	 * 		to the output array.
	 * 		
	 * 		The node that is selected is the smallest (by index number) of the two arrays.
	 * 		A counter for each array is used to track which nodes have already been added to the output.
	 * 		If all nodes from one of the input lists have already been added to the output, 
	 * 		then all remaining nodes from the other list are added to the output 
	 *
	 *		If the input arrays have n and m elements. 
	 * 		Each node of input arrays are copied once, then there are n+m iterations 
	 * 
	 *				This sub-function is in O(n+m)
	 * 
	 * 
	 * index2Tree (build a balanced binary tree from a sorted array of nodes)
	 * 
	 * 		recursively build a subtree 
	 * 		select the middle node a[m] from in an array range (between f (first) and l (last)
	 * 		the middle node a[m] becomes the parent (p) of the subtree
	 * 		the sub-arrays to the left (a[0..m-1]) and the right (a[m+1..l])
	 * 		are then recursively split into the left and right sub-trees.
	 * 
	 * 	    If the array has n + m elements. 
	 * 		Each node of the Array is visited once (while each node of the tree
	 * 		is created, ie. there are n+m recursions 
	 * 
	 *				This sub-function is in O(n+m)
	 *
	 *
	 * Performing all sub-functions for two trees, one of length n and one of length m.
	 * 
	 * 		Convert both Trees to a sorted Array:
	 * 		 O(n) + O(m)
	 * 
	 * 		Merge both Arrays into a single sorted Array:
	 * 		 O(n+m)
	 * 
	 * 		Convert the merged Array into a balanced AVL tree:
	 * 		 O(n+m)
	 * 
	 * 		Overall:
	 * 		 O(n) + O(m) + O(n+m) + O(n+m)
	 * 										... which is in O(n+m)
	 * 
	 */
	
	// Driving method for Merge function
	public static <K, V> AVLTree<K,V> merge( AVLTree<K,V> tree1, AVLTree<K,V> tree2 ) {
		
		AVLNode<K, V>[] mergedArray;	
		mergedArray = treeMerge2Array(tree1, tree2);
		
		AVLTree<K,V> mergedTree;
		mergedTree = array2Tree(mergedArray);
		
		return mergedTree;
		
	}
	
	// creates a global variable used to keep track of the number of elements inserted into
	// an array.
	private static class GlobalVar {
	     private static int count;
	}
	
	// calling function to move all the elements from a tree into a sorted array (smallest to largest)
	private static <K,V>   AVLNode<K, V>[]   tree2Array(AVLTree<K,V> t){
		
       AVLTree.AVLNode<K,V>[] nlist;
       
       nlist = new AVLNode[ t.numEntries ];
		
       int cool = 0;
       
       int count = 0;
       GlobalVar.count=0;
       
       all2ChildArray( (AVLNode)t.root, nlist, count);
       
       GlobalVar.count=0;

		return nlist;
	}
	
	/* recursive function that starts at a tree root, then recursively traverses the tree,
	 * visiting each node once, and then copying the value of the visited node into an
	 * array of empty nodes.
	 * The tree is visited in order, so that the resulting tree is ordered smallest to largest.
	 */
    private static <K,V> void all2ChildArray(AVLNode<K,V> n, AVLNode<K, V>[] nlist, int count) {
		
		if (n.getLeft().element() != null) { all2ChildArray( (AVLNode)n.getLeft(), nlist, count ); }
		
		AVLNode<K,V> ln = new AVLNode<>();
		ln.setElement(n.element());
		
		// add node then step array counter
		nlist[GlobalVar.count] = ln;
		GlobalVar.count++;
		
		if (n.getRight().element() != null) { all2ChildArray( (AVLNode)n.getRight(), nlist, count ); }
		
		return;
	}  
	
    
    // Function that takes two sorted Arrays of Nodes and merges them into a single sorted Array
	private static <K,V>   AVLNode<K, V>[]   treeMerge2Array(AVLTree<K,V> t1, AVLTree<K,V> t2){
		
        AVLTree.AVLNode<K,V>[] jlist, klist, mergeList;
        
        jlist = tree2Array(t1);
		
        klist = tree2Array(t2);
        
        mergeList = new AVLNode[ t1.numEntries + t2.numEntries ];
        
        int j, k, m;
        j = k = 0;
        
        for (m=0; m < mergeList.length; m++){
        	
        	int hj, hk;
        	
        	// if array already traversed, then select head from other list
        	if (j >= jlist.length) { mergeList[m] = klist[k]; k++; continue;}
        	if (k >= klist.length) { mergeList[m] = jlist[j]; j++; continue;}
        	
        	// evaluate current head node
        	hj = (int) jlist[j].element().getKey(); 
        	hk = (int) klist[k].element().getKey();
        	
        	// add the smaller of the two head nodes to merged, and then step the array counter
        	if ( hk > hj ){
        		mergeList[m] = jlist[j];
        		j++;
        	
        	}else{
        		mergeList[m] = klist[k];
        		k++;
        		
        	}
        }
		return mergeList;
	}
	
       
    // Driving function to convert an array of nodes into a balanced AVLTree   
    private static <K,V>   AVLTree<K,V>  array2Tree( AVLNode<K, V>[] a){
    	
    	AVLTree<K,V> t = new AVLTree<K,V>();
    	
    	int f = 0; // first
    	int l = a.length-1; // last
    	
    	AVLNode<K,V> p = (AVLNode)t.root; 
    	
    	// call recursive splitting function
    	index2Tree( f, l, (AVLNode)t.root, a);
    	
    	t.numEntries = a.length;
    	  	
    	return t;
    }
    
    /* recursively build a subtree 
     * select the middle node a[m] from in an array range (between f (first) and l (last)
     * the middle node a[m] becomes the parent (p) of the subtree
     * the sub-arrays to the left (a[0..m-1]) and the right (a[m+1..l])
     *  are then recursively split into the left and right sub-trees
     */  
    private static <K,V> void index2Tree( int f, int l, AVLNode<K, V> p, AVLNode<K, V>[] a){
    	
    	/* 	when sub-array has only 1 element; the leaf node has been reached
    		set value of p (parent of subtree) to a[f] (only node of sub array)
    	 	and leave the children as empty nodes */
    	
    	if (l-f == 0){		

    		AVLNode<K,V> lNode = new AVLNode<K,V>();
    		p.setLeft(lNode);
    		lNode.setParent(p);
    		
    		AVLNode<K,V> rNode = new AVLNode<K,V>();
    		p.setRight(rNode);
    		rNode.setParent(p);

    		p.setElement(a[f].element());
    		
        	int h = Math.max(lNode.getHeight(), rNode.getHeight());
        	p.setHeight(h+1);

    		return;
    		}
    	
    	
    	int m = f + (l-f)/2; // mid
    	
		AVLNode<K,V> lNode = new AVLNode<K,V>();
		p.setLeft(lNode);
		lNode.setParent(p);
		
		AVLNode<K,V> rNode = new AVLNode<K,V>();
		p.setRight(rNode);
		rNode.setParent(p);
		
    	//left, set left node to p, and repeat splitting on left sub-array
    	if (m-1 >= f ){
     		index2Tree(f, m-1, (AVLNode)p.getLeft(), a);
    		}
    	
    	
    	//right set right node to p, and repeat splitting on right sub-array  	
    	if (l >= m+1 ){
			index2Tree(m+1, l, (AVLNode)p.getRight(), a);
			}
    	
    	
    	// set value of p (parent of subtree) to a[m] (middle node of current sub array)
    	p.setElement(a[m].element());
    	    	
    	int h = Math.max(lNode.getHeight(), rNode.getHeight());
    	p.setHeight(h+1);

    }
    
    
	// ======================================================================================
    // 									Question 3
	// ======================================================================================
	

    // Driving method for the Print function
    public static <K, V> void print(AVLTree<K, V> tree) {
    	
    	// set frame size based on Tree height and number of elements
        JFrame frame = new JFrame();
        
        int fW = 80 * (tree.numEntries);
        int fH = 100 * (((AVLNode)tree.root).height+1);
        
        frame.setSize(fW, fH);
        frame.setResizable(false);
        frame.setTitle("AVLTree: "+tree.toString());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // call recursive print function
        printAllChild((AVLNode)tree.root, frame, fW/2, 20, fW/2, 0);
    }
    
    // Recursive method for printing nodes (including labels, and connector lines)
    private static <K, V> void printAllChild(AVLNode<K,V> n, JFrame frame, int x, int y, int xH, int yH) {
        int cD = 40 - (yH*2);
        int boxW = 30;
        int boxH = boxW/2;
        String s;
        
        int dx = xH/2;
        int dy = yH+1;
        int nl = 90-y/90;
        
        // recursively print right children nodes until a null node is reached
        if ( n.getLeft().element() != null) { 
            printAllChild((AVLNode)n.getLeft(), frame, x-dx, y+nl, dx, dy);
            
        } else {
        	// print empty Node box
            frame.getContentPane().add(new Rectangle(x-dx, y+nl, boxW, boxH));
            frame.setVisible(true);
        }
        
        // recursively print right children nodes until a null node is reached
        if ( n.getRight().element() != null) { 
            printAllChild((AVLNode)n.getRight(), frame, x+dx, y+nl, dx, dy);
            
        } else {
        	// print empty Node box
            frame.getContentPane().add(new Rectangle(x+dx, y+nl, boxW, boxH));
            frame.setVisible(true);
        }

        
        // Print current node and connectors to children
        
        // draw red node circle
        frame.getContentPane().add(new Circle(x, y, cD));
        frame.setVisible(true);
        
        // print node Key in circle
        s = n.element().getKey().toString();
        frame.getContentPane().add(new Text(s, x-(3*s.length()), y+cD/2 ));
        frame.setVisible(true);
        
        // print node Value under circle
        s = "[" + n.element().getValue().toString() + " h:" + n.getHeight() + "]";
        frame.getContentPane().add(new Text(s, x-cD/2-(2*s.length()), y+cD+12 ));
        frame.setVisible(true);
        
        // print left child connector
        frame.getContentPane().add(new Line(x-cD/4, y+cD, x-dx, y+nl, Color.gray));
        frame.setVisible(true);
        
        // print right child connector
        frame.getContentPane().add(new Line(x+cD/4, y+cD, x+dx, y+nl, Color.blue));
        frame.setVisible(true);
        
    }
    
	
	// Class for Drawing a Circle
    private static class Circle extends JComponent {

        int x, y, w, h = 0;
        
        Circle(int x, int y, int d) {
            this.x = x-(d/2);
            this.y = y;
            this.w = d;
            this.h = d;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
        	super.paintComponent(g);
        	g.setColor(Color.RED);
            g.drawOval(x, y, w, h);
        }
    }

    
    // Class for drawing connector Lines between nodes
    private static class Line extends JComponent {
        
        int x1, y1, x2, y2 = 0;
        Color c;
        
        Line(int x1, int y1, int x2, int y2, Color c) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.c = c;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
        	super.paintComponent(g);
        	g.setColor(c);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    // Class for drawing Text labels
    private static class Text extends JComponent {        
        
    	String s;
        int x, y = 0;
        
        Text(String s, int x, int y) {
            this.s = s;
            this.x = x;
            this.y = y;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            g.drawString(s, x, y);
            g.setColor(getForeground());
        }
    }
    
    // Class for Drawing a Box at the Empty Nodes
    private static class Rectangle extends JComponent {

    	int x, y, w, h = 0;
        
        Rectangle(int x, int y, int w, int h) {
            this.x = x - (w/2);
            this.y = y;
            this.w = w;
            this.h = h;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
        	super.paintComponent(g);
        	g.setColor(Color.lightGray);
            g.fillRect(x, y, w, h);
        }
    }
    
    
	// ======================================================================================
    //  Methods for inspecting Nodes and  Arrays, used for testing 
	// ======================================================================================
    
    /* These methods were used for testing and debugging, and are included for only for
     * completeness and future reference.
     */
    
//    // print the elements in an Array as a string
//	private static <K,V> void printArray(AVLNode<K, V>[] anArray) {
//		System.out.print("[");
//		for (int i = 0; i < anArray.length; i++) {
//			if (i > 0) {
//				System.out.print(", ");
//			}
//			AVLNode<K,V> node;
//			node=(AVLNode)anArray[i];
//			System.out.print( node.element().getKey() + "|" + node.element().getValue() );
//			
//		}
//		System.out.println("]");
//	}
//	
//	// print the values of a node as a String
//	private static <K,V> void textNode(AVLTree.AVLNode<K,V> node) {
//		// if empty node
//		if (node.element() == null){
//			System.out.print("	null node | Parent:");
//			System.out.print(node.getParent().element().getKey() + "|" + node.getParent().element().getValue());
//			System.out.println();
//			return;}
//		
//		// else non-empty node 	
//		System.out.print(node.element().getKey() + "|" + node.element().getValue());
//		System.out.print("|H:"+ node.height );
//		System.out.println();
//		return;
//	}
//	
//	// calling method for printing the values of each Tree node as a string
//	private static <K,V> void textTree( AVLTree<K,V> t ) {
//		
//		System.out.println("Key|Node - Size:" + t.size + ", NumEntries:" + t.numEntries + " [Tree:" + t +"]");
//		
//		if (t.root.element() == null) { System.out.println("empty tree, null root");}
//		else {textAllChild((AVLNode)t.root);}
//		return;
//	}
//	
//	// recursively print the values of each tree node as a string
//	private static <K,V> void textAllChild(AVLTree.AVLNode<K,V> n){
//
//		if (n.getLeft() != null) { textAllChild( (AVLNode)n.getLeft() ); }
//		//else {System.out.println("L");}
//		
//		textNode((AVLNode)n);
//		
//		if (n.getRight() != null) { textAllChild( (AVLNode)n.getRight() ); }
//		//else {System.out.println("R");}
//		
//		return;
//	}
//	
//	// print height value of a node
//	private static <K,V> void seeHeight(AVLTree.AVLNode<K,V> n) {
//		System.out.println(n.height);
//	}
//	
//	// copy a tree via Insert method (used for testing / debugging) ================ 	
//	private static <K,V> void insertAllChild(AVLTree.AVLNode<K,V>  on, AVLTree<K,V> ct){
//
//		if (on.getRight().element() != null) {
//			insertAllChild((AVLNode)on.getRight(), ct);
//		}
//		
//		K k = on.element().getKey();
//		V v = on.element().getValue();
//		
//		System.out.println("*ins*:"+k+"|"+v);
//		ct.insert(k, v);
//		
//		if (on.getLeft().element() != null) {
//			insertAllChild((AVLNode)on.getLeft(), ct);
//		}
//		
//		return;
//	}
//	
//	// Driving method to replicate a tree via insertions
//	private static <K,V> AVLTree<K,V> cloneInsert(AVLTree<K,V> ot) {
//		AVLTree<K,V> ct = new AVLTree<>();
//		
//		if (ot.root.element() == null) { return ct;} // check if cloned tree is empty
//		
//		// use insertions for generating the clone
//		insertAllChild((AVLNode)ot.root, ct);
//		
//		System.out.println("**INSERT cloned**");
//		
//		return ct;
//	}
	
	
	// ======================================================================================
    // 									MAIN
	// ======================================================================================
	
	
	public static void main(String[] args) {

        String values1[]={"Sydney", "Beijing","Shanghai", "New York", "Tokyo", "Berlin",
       "Athens", "Paris", "London", "Cairo"}; 
        int keys1[]={20, 8, 5, 30, 22, 40, 12, 10, 3, 5};
        
        String values2[]={"Fox", "Lion", "Dog", "Sheep", "Rabbit", "Fish"}; 
        int keys2[]={40, 7, 5, 32, 20, 30};
           
        /* Create the first AVL tree with an external node as the root and the
       default comparator */ 
           
          AVLTree<Integer, String> tree1 = new AVLTree<Integer, String>();

        // Insert 10 nodes into the first tree
           
          for ( int i=0; i<10; i++)
              tree1.insert(keys1[i], values1[i]);
         
        /* Create the second AVL tree with an external node as the root and the
       default comparator */
           
          AVLTree<Integer, String> tree2=new AVLTree<Integer, String>();
          
          // Insert 6 nodes into the tree
          
          for ( int i=0; i<6; i++)
              tree2.insert(keys2[i], values2[i]);
           
          ExtendedAVLTree.print(tree1);
          ExtendedAVLTree.print(tree2); 
          ExtendedAVLTree.print(ExtendedAVLTree.clone(tree1));
          ExtendedAVLTree.print(ExtendedAVLTree.clone(tree2));
          
          ExtendedAVLTree.print(ExtendedAVLTree.merge(ExtendedAVLTree.clone(tree1), 
          ExtendedAVLTree.clone(tree2)));
          
          ExtendedAVLTree.print(
        		  ExtendedAVLTree.merge(
        				  ExtendedAVLTree.merge(tree1,tree2),
        				  ExtendedAVLTree.merge(
        						  ExtendedAVLTree.clone(tree2),
        						  ExtendedAVLTree.clone(tree1)
        						  )
        				  )
        		  );
          
  		
	}

}


// navigate via AVLNODE, go to root, then, left, right, parent, and element() 
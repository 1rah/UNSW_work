// COMP9041 Assignment 4, Irah Wajchman z3439745

import java.io.*;


/* Build Suffix Tree pseudo-code:
 * 
 *  read in a string of n characters, create a character array of n element ... O(n)
 * 	
 * 	for each suffix in the input string (from last to first) do:
 * 			
 * 			for each character in the suffix, from first to last do:
 * 
 * 				for each character in the node do:
 * 				
 * 					check the character against a node in the suffix tree:
 * 			
 * 						if the end of of a node is reached, step to next child node:
 * 								if the child node is null, place the next node here
 * 								else continue at the next node
 * 
 * 						if an non-matching character is found:
 * 								split the node into three
 * 								with the parent made up the matching characters,
 * 								and non matching character assigned to the children
 * 
 * 	
 * 	The worst case is for  a string of n identical characters, which would require each 
 *  traversed node to be split, and each node would have a length of only one.
 *  In this situation the governing case would be the last suffix (made up of n characters)
 *  which would be checked against (n-1) character nodes, before being placed at child of the (n-1)th node
 *  This would be n x (n-1) operations, or in O(n) x o(n) operations... which is in O(n^2) 
 *  
 * 
 * The Build Suffix Tree Function is in O(n^2) 
 * 
 * 
 * ------------------------------------------------------------------------------------
 * Find String Analysis pseudo-code:
 * 	read in a string of n characters, create a character array of n element ... O(n)
 * 
 * 	
 * 	for each character in input string do:
 * 		
 * 			for node on path:
 * 
 * 				for each character referenced in a node:
 * 
 * 					check if Trie node contains all in order characters
 *					
 *					if mismatch is found, return -1
 *					 
 * 					if edge of node is reached, step to next node
 * 
 * 
 * 
 * each character in the input string is checked once against the next path character in the tree,... O(n)
 * 
 * Find String Function is in O(n) + O(n) = O(n)
 * -------------------------------------------------------------------------------------------
 * 
 * 
 * 	Similarity Analyser Pseudo-code:
 * 
 * 	Build the Comparison Array:
 * 	Find Longest Common Substring - Function( String1, String 2):
 * 		
 * 		for each character in String1 do:
 * 			for each character in String2 do:
 * 				if character 1 == character 2:
 * 					array[i][j] == array[i-1][i-j] + 1
 * 				else:
 * 					array[i][j] == max( array[i][i-j], array[i-1][j] )
 * 				
 * 	Walk the array from the bottom right corner:
 * 		
 * 			append character from String1 to the output string
 * 					
 * 			if At a matching element step diagonally left
 * 			else step left or up, whichever is bigger
 * 			else if they are the same step left
 * 
 * 			if the edge of the array is reached, break
 * 
 * 	
 * 
 * 	Building the Comparison array for String1 with n character and String2 with m character,
 *  requires each element of an m x n array to be filled.... this step is in O(mn)
 *  
 *  Walking the path would require at worst case m steps up and n steps across,
 *  with m+n steps... this function is in O(m) + O(n)
 *  
 *  
 *  The Similarity Analyser, combined function time is in O(mn) + O(m) + O(n)... which is in O(mn)
 * 
 * 
 */


public class CompactCompressedSuffixTrie {

	public Node root;
	public char[] inChars;
	static char[] dict = "ACGT".toCharArray();
	static char endChar = '$';

	public CompactCompressedSuffixTrie(String inFile) {
		
		this.root = new Node();
		
		char[] readChars = readInFile(inFile);
		
		if(readChars!=null) {
			this.inChars = readChars;
			buildTrie();
			}
	}
	
	// method to build the tree 
	public void buildTrie(){
		int c;
		int end = inChars.length-1;
		
//		System.out.print("INPUT::");
//		for (char chr : inChars){
//			System.out.print(chr);
//		}
//		System.out.println();
		
		// the suffix tree is built from the last character forward
		for(int i = inChars.length-1; i>=0; i--) {
			c = inChars[i];
			
			// first fill the empty child nodes of the root node
			if(root.getChild(c) == null){
				root.setChild(c, new Node(i,end));
				
			//if the node is already filled, it must be split until an empty node is reached for insertion			
			} else {	
				splitChild(root.getChild(c), inChars,  i , c, end);
			}
		}
	}

	
	public void splitChild(Node current, char[] treeWord,  int index, int chara, int maxend) {
		
		int start = current.start;
		int end = current.end;
		
		int index2 = index;
		int j = start;
		
		Node child;
		
		//determine the number of matching characters between the new suffix and existing suffix
		while(j <= end) {
			if(treeWord[j] == treeWord[index2]){
				j++;
				index2++;
			} else {
				break;
			}
		}
		
		//if the new suffix matches existing suffix path or is longer then add the suffix
		if(j > end) {
			int c = treeWord[index2];
			child = current.getChild(c);
			current.start = index;
			current.end = index2-1;
			
			//if empty leaf child node, add node  
			if(child == null) {
				current.setChild(c, new Node(index2,maxend));
			
			//else if non-empty child, split the node to create space 	
			} else {
				splitChild(current.getChild(c), treeWord, index2, c, maxend);
			}
		
			
		/*if the the new suffix is longer than existing suffix path and some characters
		 *  overlap, then split the node into a parent and a child node (of non-overlap character)
		 */
		} else {
			int spltStart = j;
			int spltEnd = end;
			
			current.start = index;
			current.end = index2-1;
			
			int c1 = treeWord[spltStart];
			int c2 = treeWord[index2];
			
			current.setChild(c1, new Node(spltStart, spltEnd));
			current.setChild(c2, new Node(index2, maxend));
		}
		
	}
	
	private static char[] readInFile(String inFile) {
		String outStr = "";
		try {
			String ln;
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			
			// read each character in the specified text file, and check that it is in the dictionary [A, C, G, T]
			while ((ln = br.readLine()) != null) {
				for (char c: ln.toCharArray()){
					for (char d: dict){
						if (c==d) {	
							outStr += c;
						}
					}
				}
			}
			
			// close file
			br.close();
			
		} catch (Exception e) {
			System.out.println("file "+ inFile +" not found.");
			e.printStackTrace();
		}
		
		// return input file string plus endChar appended
		return (outStr + endChar).toCharArray();
	}

	
	
	//Find the first matching position of a string within the Suffix tree
	public int findString(String s){
		int c;
		char[] inChars = s.toCharArray();
		
		Node current = this.root;
		
		int cStart = 0;
		int cEnd = -1;
		
		int endPosition = 0;
		
		for(int i=0; i<inChars.length; i++) {
			c = inChars[i];
			
			//reached the end of a node, step to the next child[c]
			if(cStart > cEnd) {
				current = current.getChild(c);
				
				//if child is null, no match
				if(current == null) {
					return -1;
					
				//else stop to child	
				} else {
					cStart = current.start;
					cEnd = current.end;
				}
			}
			
			// the characters of inChar match the suffix path, step forward
			if(inChars[i] == this.inChars[cStart]) {
				endPosition = cStart;
				cStart++;
				
			// does match a character in the node, no match
			} else {
				return -1;
			}
		}
		
		// Start pos = the number of characters matched - number of character in inChars
		return (endPosition - inChars.length + 1);
	}



	private static void writeOutFile(String outStr , String fileName) {
		
		File outFile = new File(fileName);
		try {
			// if file doesn't exist try to create it
			if (!outFile.exists()) {
				try {
					outFile.createNewFile();
				} catch (IOException e) {
					System.out.println(fileName + ".txt could not be created.");
					e.printStackTrace();
				}
			} else {
				// if file already exists, overwrite it
				System.out.print(fileName + ".txt already exists, over-writing file... ");
			}	
	
			FileWriter fw = new FileWriter(outFile.getName());
			
			// write the output string to the specified file
			fw.write(outStr);
			System.out.print("subsequence written to "+fileName+".txt");
			System.out.println();
			fw.close();
			
		} catch (IOException e) {
			System.out.println(fileName + ".txt could not be accessed");
			e.printStackTrace();
		}
	}

	public static float similarityAnalyser(String file1, String file2, String file3){
			String f1 = String.valueOf(readInFile(file1));
			String f2 = String.valueOf(readInFile(file2));
	
			String l = lcs(f1, f2);
			
	//		System.out.println(f1.length()+"-"+f2.length()+"-"+l.length());
			
			float freq = (float) l.length() / (float) Math.max(f1.length(), f2.length());
			
			writeOutFile(l , file3);
			
			return freq;
		}

	public static String lcs(String a, String b) {
		
		// based on algorithm from text-book
		// returns the longest common subsequence
	    int[][] table = new int[a.length()+1][b.length()+1];
	    
	    // create the character matching table
	    for (int i = 0; i < a.length(); i++)
	        for (int j = 0; j < b.length(); j++)
	            if (a.charAt(i) == b.charAt(j))
	                table[i+1][j+1] = table[i][j] + 1;
	            else
	                table[i+1][j+1] =
	                    Math.max(table[i+1][j], table[i][j+1]);
	
	    // read the substring by traversing the table 
	    StringBuffer sb = new StringBuffer();
	    int j = a.length();
	    int k = b.length();
	    while(table[j][k]>0) {
	    	if (a.charAt(j-1) == b.charAt(k-1)){
	    		sb.append(a.charAt(j-1));
	    		j--;
	    		k--;
	    	} else if (table[j-1][k] >= table[j][k-1]) {
	    		j--;
	    	} else {
	    		k--;
	    	}
	    }
	    	
	    return sb.reverse().toString();
	}
	
	public static void main(String args[]) throws Exception{
        
		 /** Construct a compact compressed suffix trie named trie1
		  */       
		 CompactCompressedSuffixTrie trie1 = new CompactCompressedSuffixTrie("file1.txt");
		         
		 System.out.println("ACTTCGTAAG is at: " + trie1.findString("ACTTCGTAAG"));

		 System.out.println("AAAACAACTTCG is at: " + trie1.findString("AAAACAACTTCG"));
		         
		 System.out.println("ACTTCGTAAGGTT : " + trie1.findString("ACTTCGTAAGGTT"));
		         
		 System.out.println(CompactCompressedSuffixTrie.similarityAnalyser("file2.txt", "file3.txt", "file4.txt"));
		
//		CompactCompressedSuffixTrie t1 = new CompactCompressedSuffixTrie("my_Test.txt");
		CompactCompressedSuffixTrie t2 = new CompactCompressedSuffixTrie("my_Test2.txt");

		System.out.println("my test:" + t2.findString("CCC"));
		System.out.println("my test:" + t2.findString("AAA"));
		
		
	}
}


// Node Class: subclass of CompactCompressedSuffixTrie
// container object for child pointer array, and (start, end) tuples

// since the number of children nodes is fixed, all set and get operations occur in constant time .... O(1)
class Node {

	private final static int n = 5;

	public int start;
	public int end;
	public Node[] child;

	public char element;
	public Node next;

	public Node(char element) {
		this.element = element;
	}
	
	
	public Node() {
		this.start = -1;
		this.end = -1;
		
		child = new Node[n];
		for (int i = 0; i < n; i++){
			child[i] = null;
		}
	}
	
	public Node(int start, int end) {
		this();
		this.start = start;
		this.end = end;
	}
	
	public void setChild(int i, Node node) {
		i = map(i);
		child[i] = node;
	}
	
	public Node getChild(int i) {
		i = map(i);
		return child[i];
	}
	
	private int map(int i) {
		int m;
		/*Map ASCII value to child array index
		 * A = 65, C = 67, G = 71, T = 84, $ = 36
		 */
		if (i == 65) { m = 0;}
		else if (i == 67) { m = 1;}
		else if (i == 71) { m = 2;}
		else if (i == 84) { m = 3;}
		else {assert i == 36;  m = 4;}
		
		return m;
	}
	
}

/*
 * COMP9024 ASSIGNMENT 3
 * Irah Wajchman, z3439745
 * 
 */

package net.datastructures;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class TaskScheduler {
	
	// object used to hold each Task's name, release time, and deadline time
	private static class TaskObj {

		private String name;
		private Integer release;
		private Integer deadline;

		public TaskObj() {
			this.name = null;
			this.release = null;
			this.deadline = null;
		}

		public TaskObj(String tname, Integer rtime, Integer dtime) {
			this.name = tname;
			this.release = rtime;
			this.deadline = dtime;
		}

		public String getName() {
			return name;
		}

		public void setName(String tname) {
			this.name = tname;
		}

		public Integer getRel() {
			return release;
		}

		public void setRel(Integer rtime) {
			this.release = rtime;
		}

		public Integer getDeadL() {
			return deadline;
		}

		public void setDeadL(Integer dtime) {
			this.deadline = dtime;
		}
		
		public String toString() { 
		    return "N:" + this.name + " R:" + this.release + " D:" + this.deadline + "| ";
		}
	}

	// Method function that reads in the data from a user specified file, and returns an ArrayList of Task Objects
	// The program checks that each input task is in a valid format or breaks the read loop if not
	private static ArrayList<TaskObj> readInFile(String fileName) {
		
		ArrayList<TaskObj> taskList = new ArrayList<>();
		
		File inFile = new File(fileName);
		Scanner s;
		String tname;
		Integer rtime, dtime;
		
		try {
			s = new Scanner(inFile);
			
			ReadLoop:
			while (s.hasNext()) {
				
				tname = s.next();
//				System.out.println("T:" + tname);
				
				try {
					rtime = Integer.valueOf(s.next());
//					System.out.println("R:" + rtime);
					
					dtime = Integer.valueOf(s.next());
//					System.out.println("D:" + dtime);
					
				} catch (NumberFormatException e) {
					System.out.println("input error when reading the attributes of the task " + tname);
					e.printStackTrace();
					break ReadLoop;
				}
				
				 taskList.add(new TaskObj(tname, rtime, dtime));
			}
			
			s.close();
			
			} catch (FileNotFoundException e) {
				System.out.println(fileName +" does not exist" );
				e.printStackTrace();	
			}
			
		return taskList;
	}
	
	
	// Method function that writes an input string to a user specified file
	// the function checks if the file can opened and written to 
	// if the speicified file already exists, the existing file is overwritten
	private static void writeOutFile(String outStr , String fileName) {
		
		File outFile = new File(fileName + ".txt");
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
				System.out.println(fileName + ".txt already exists, over-writing file");
			}	

			FileWriter fw = new FileWriter(outFile.getName());
			
			// write the output string to the specified file
			fw.write(outStr);
			System.out.println("Feasible schedule exists, written to "+fileName+".txt");
			fw.close();
			
		} catch (IOException e) {
			System.out.println(fileName + ".txt could not be accessed");
			e.printStackTrace();
		}
	}
	
	static void scheduler(String file1, String file2, int m) {
		
		/* file1 = input filename
		 * file2 = output filename
		 * m = maximum available cores
		 * 
		 * Function that reads in a list of tasks from file1 and determines is a feasible schedule exists
		 * for the task list to be completed with the available number of cores (m)
		 * Input file must be a space separated format as: TaskName1 ReleaseTime1 DeadlineTime1 ...
		 * If a valid schedule is determined, it is output to the user specified text file2
		 * 
		 * 
		 * RUNNING TIME ANALYSIS:
		 *
		 * insert each task object into the priority queue pq1, using release time as the sort key
		 * 
		 * [ n Tasks, log(n) operations to insert 1 element into a priority queue, this step is in O (n log(n)) ] 
		 * 
		 * 
		 * 
		 * for (currentTime = 0 ; !pq1.empty() && !pq1.empty() ;currentTime ++1):
		 * 		remove all tasks from pq1 that have Release Time == Current Time,
		 * 		then insert them into pq2 using Deadline Time as the sort key
		 * 
		 *  
		 * [there are at most n items to insert into pq2, and n items to remove n item from pq1,
		 * this step is in O(n log(n) + n log(n))]
		 * 
		 * 
		 * 
		 * 		for each core, check the minimum deadline task in pq2, and check that it's Deadline <= the current time step:
		 * 				if current time is greater than the smallest deadline, then there NOT a feasible schedule - BREAK
		 * 				else remove the task from pq2, and add the task details (name and time) to the output string
		 * 
		 * [there are at most n items to remove n item from pq2, this step is in O(n log(n))]
		 * 
		 *  
		 * [ Total running time is: O(n log(n)) + O(n log(n)) + O(n log(n)) + O(n log(n))... which is in O(n log(n)) ]
		 * 	
		 */
		
		ArrayList<TaskObj> tList = readInFile(file1);
		
		HeapPriorityQueue<Integer, TaskObj>  pq1 = new HeapPriorityQueue<Integer, TaskObj>();
		HeapPriorityQueue<Integer, TaskObj>  pq2 = new HeapPriorityQueue<Integer, TaskObj>();
		
		// insert each Task object into the priority pq1 [ n log(n) ]
		for (TaskObj task : tList) {
			pq1.insert(task.getRel(), task);
		}
		
		String outStr = "";
		for (int curTime = 0; !pq1.isEmpty(); curTime++){
			
			// remove all tasks from pq1 that have Release Time == Current Time, then insert into pq2 [ n log(n) ]
			while (!pq1.isEmpty() && pq1.min().getValue().getRel() == curTime ){
				TaskObj t = pq1.removeMin().getValue();
				pq2.insert(t.getDeadL(), t);
			}
			
			// for each core, remove 1 task from  pq2, and check that it's Deadline <= the current time step [ n log(n) ]
			for (int core = 1; !pq2.isEmpty() && core <= m; core++) {
				
				// if current time is greater than the smallest deadline, then NOT a feasible schedule.
				if (curTime >= pq2.min().getKey()) {
					System.out.println("No feasible schedule exists.");
					return;
					
				// else remove the task from pq2, and add to the output string (taskName currentTime, ... ) [ n log(n) ]  	
				} else {
					String taskName = pq2.min().getValue().getName();
					outStr = outStr + taskName + " " + curTime + " ";
					pq2.removeMin();
				}
			}
		}
				
//		System.out.println("OUT:"+outStr);
		writeOutFile(outStr, file2);
	}
	
	//============================================================================================================================


	/*
	 * =========== MAIN =======================
	 */
	public static void main(String[] args) {
	    TaskScheduler.scheduler("samplefile1.txt", "MY_feasibleschedule1", 4);
	    /** There is a feasible schedule on 4 cores */      
	     TaskScheduler.scheduler("samplefile1.txt", "MY_feasibleschedule2", 3);
	    /** There is no feasible schedule on 3 cores */
	     TaskScheduler.scheduler("samplefile2.txt", "MY_feasibleschedule3", 5);
	    /** There is a feasible scheduler on 5 cores */ 
	     TaskScheduler.scheduler("samplefile2.txt", "MY_feasibleschedule4", 4);
	    /** There is no feasible schedule on 4 cores */
	     
	    /** The sample task sets are sorted. You can shuffle the tasks and test your program again */ 
					
	     TaskScheduler.scheduler("example_from_PDF.txt", "MY_feasibleschedule_from_PDF", 2);
	}
		
}

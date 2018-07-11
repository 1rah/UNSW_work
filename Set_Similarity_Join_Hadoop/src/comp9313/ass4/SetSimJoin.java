package comp9313.ass4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.io.DataInput;
import java.io.DataOutput;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class SetSimJoin {

	public static String IN = "";
	public static String OUT = "";
	public static Integer nRED;

	public static class InputMapperV2 extends Mapper<Object, Text, LongWritable, Text> {
		/*
		 * Mapper 1:
		 *  uses the prefix method to generate pairs of format:
		 * 	DocID, lineNo|id1:id2:id3...
		 * 		For each id# in the prefix length
		 */
		HashSet<String> nodeSet = new HashSet<>();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			List<String> line = new ArrayList<>(Arrays.asList(value.toString().split("\\s")));
			String rid = line.remove(0);
			
			Configuration conf = context.getConfiguration();
			String tauIn = conf.get("tau");
			Double tau = Double.parseDouble(tauIn);
			
			int r = line.size();

			int l = (int) Math.ceil(r * tau);

			// p is the number of id elements to include in the prefix, 1 pair will be emitted for each
			int p = r - l + 1;

			String outString = rid + "|" + String.join(":", line);

			int i = 0;
			while  (!line.isEmpty()){
				if (i >= p) {break;}
				i++;
				String k = line.remove(0);
				context.write(new LongWritable(Integer.valueOf(k)), new Text(outString));
			}

		}
	}
	
	
	public static class FirstReducerArrayListPairs extends Reducer<LongWritable, Text, Text, Text> {
		@Override
		public void reduce(LongWritable key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
	
			/*
			 * Reducer 1:
			 * 	for each pair received from the mapper:
			 * 		create intersection and union between elements in the pair:
			 * 			Duplicate Check:
			 * 			check of the Minimum element in the intersection set is the same value 
			 * 			as the current reducer key
			 * 			if yes: continue
			 * 			else: ignore
			 * 			
			 * 			Similarity check:
			 * 			Caluculate simularity (size of intersection / size of union)
			 * 			if similarity is >= tau: emit
			 * 			else: ignore 
			 * 
			 * Output format:
			 * (docID1, docID2)	similarity_value
			 * 
			 */
			Double simVal;
			String outString;
			
			Configuration conf = context.getConfiguration();
			Double tau = Double.parseDouble(conf.get("tau"));
	
			ArrayList<KeySetPair> valList = new ArrayList<>();
			for (Text val: values) {
				
				String[] valSplit = val.toString().split("\\|"); // split to <rid | list>
				valList.add(new KeySetPair(valSplit[0], valSplit[1]));
			}
			
			if (valList.size() > 1) { // if there is at least 1 pair
				KeySetPair ksVal1;
				while(!valList.isEmpty()) {
					ksVal1 = valList.remove(0);
	
					for (KeySetPair ksVal2: valList) {
						/*
						 * Compare val1 to val2 -> similarity
						 */
						
						simVal = ksVal1.compare(ksVal2);
	
						/*
						 * Duplicate Pair Check:
						 * Emit only pairs that have 
						 * 		Minimum value from the set == the current reducer key
						 */
						Boolean isPrime =
								ksVal1.getMinIntersect().toString().equals(key.toString())
								|| ksVal1.getIntersectSize() == 1;
						
						/*
						 * Minimum similarity filter:
						 * 	Emit only pairs with greater than TAU similarity
						 */
						if (simVal >= tau & isPrime) {
							// place smallest rid on LHS of pair, and emit
							if (Integer.valueOf(ksVal1.key) < Integer.valueOf(ksVal2.key)) {
//								outString = "(" + ksVal1.key + "," + ksVal2.key + ")";
								outString = ksVal1.key + "," + ksVal2.key;
							} else {
								outString = ksVal2.key + "," + ksVal1.key;
							}
							context.write(new Text(outString), new Text(simVal.toString()));
						}
					}// end inner loop
				}// end while loop
			} // end if len > 1
		}
	}
	
	public static class KeySetPair {
		
		/*
		 * Class used in Reducer 1 to allow easy comparisons for each item
		 * each item is stores as:
		 * 	docID, {set of all elements in the doc} 
		 */
		
		private String key;
		private HashSet<Integer> valSet;
		private SetView<Integer> numSet, denSet; // value from the last comparison made
	
		public KeySetPair() {
		}
	
		public KeySetPair(String first, String second) {
			set(first, second);
		}
	
		public void set(String left, String right) {
	
			key = left;
			String[] rSplit = right.split("\\:");
			HashSet<Integer>inSet = new HashSet<>();
			for (String val: rSplit ) {
				inSet.add(Integer.valueOf(val));
			}
			valSet = inSet;
		}
		
		public Double compare(KeySetPair KSP2) {
			Double simVal;
			numSet = Sets.intersection(valSet, KSP2.valSet); // calc numerator set
			denSet = Sets.union(valSet, KSP2.valSet); // calc denominator set
			simVal = Double.valueOf(numSet.size()) / denSet.size(); // calc similarity
			return simVal;
		}
		
		public String compareString(KeySetPair KSP2) {
			Double simVal;
			numSet = Sets.intersection(valSet, KSP2.valSet); // calc numerator set
			denSet = Sets.union(valSet, KSP2.valSet); // calc denominator set
			simVal = Double.valueOf(numSet.size()) / denSet.size(); // calc similarity
			return numSet + "/" + denSet + "=>" + simVal;
		}
		
		public Integer getMinIntersect() {
			return Collections.min(numSet);
		}
		
		public int getIntersectSize() {
			return numSet.size();
		}
		
		public String toString() {
			return key + "|" + valSet;
		}
	}
	

	public static class SecondMapper extends Mapper<Object, Text, IntPair, Text> {

		/*
		 * Reads and emits them in the format:
		 * 			(docID1, docID2)		similarity_value
		 * 
		 * Uses Writable-Comparable IntPair Class as the key, to allow sorting by two keys
		 */

		HashSet<String> nodeSet = new HashSet<>();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String[] split1 = value.toString().split("\t");
//			String[] split2 = split1[0].replaceAll("[\\(\\)]", "").split(",");
			String[] split2 = split1[0].split(",");

			IntPair keyPair = new IntPair(Integer.valueOf(split2[0]), Integer.valueOf(split2[1]));

			context.write(keyPair, new Text(split1[1]));
			
		}
	}
	

	public static class SecondReducer extends Reducer<IntPair, Text, Text, Text> {
		@Override
		public void reduce(IntPair key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			/*
			 * Reducer Class, simply reads then emits each item as received
			 * 		(docID1, docID2)		similarity_value
			 */
				Text val = values.iterator().next(); // assume only 1 item per key pair (dupes are removed in step 1)
				context.write(new Text("("+key.i.toString()+","+key.j.toString()+")"),
						new Text(val));
		}

	}
	

	
    /*
     * 	A Class used to act as a 2 integer key, for use with IndexPairComparator
     * 	As implemented from the following website:
     *	https://vangjee.wordpress.com/2012/03/30/implementing-rawcomparator-will-speed-up-your-hadoop-mapreduce-mr-jobs-2/
     */
	public static class IntPair implements WritableComparable<IntPair> {
	    private IntWritable i;
	    private IntWritable j;
	    
	    public IntPair() {
	    }
	    
	    public IntPair(int i, int j) {
	        this.i = new IntWritable(i);
	        this.j = new IntWritable(j);
	    }
	    @Override
	    public int compareTo(IntPair o) {
	        int cmp = i.compareTo(o.i);
	        if(0 != cmp)
	            return cmp;
	        return j.compareTo(o.j);
	    }
		@Override
		public void readFields(DataInput arg0) throws IOException {
			i = new IntWritable(arg0.readInt());
			j = new IntWritable(arg0.readInt());
			
		}
		@Override
		public void write(DataOutput arg0) throws IOException {
			arg0.writeInt(i.get());
			arg0.writeInt(j.get());
			
		}
	}
	
	
    /*
     * 	A Comparator class that allows sorting using bitwise comparisons
     * 	As implemented from the following website:
     *	https://vangjee.wordpress.com/2012/03/30/implementing-rawcomparator-will-speed-up-your-hadoop-mapreduce-mr-jobs-2/
     */
	public static class IndexPairComparator extends WritableComparator {
	    protected IndexPairComparator() {
	        super(IntPair.class);
	    }
	     
	    @Override
	    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
	        int i1 = readInt(b1, s1);
	        int i2 = readInt(b2, s2);
	         
	        int comp = (i1 < i2) ? -1 : (i1 == i2) ? 0 : 1;
	        if(0 != comp)
	            return comp;
	         
	        int j1 = readInt(b1, s1+4);
	        int j2 = readInt(b2, s2+4);
	        comp = (j1 < j2) ? -1 : (j1 == j2) ? 0 : 1;
	         
	        return comp;
	    }
	}
	

	public static void FirstRun(String inDir, String outDir, int numReducers, String tau) throws Exception {
		/*
		 * Run Mapper 1 and Reducer 1
		 */
		Configuration conf = new Configuration();
		conf.set("tau", tau);
		
		Job job = Job.getInstance(conf, "First Run");
		
		
		job.setJarByClass(SetSimJoin.class);

		// set the Map class
		job.setMapperClass(InputMapperV2.class);
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(Text.class);

		// set the reducer class
		job.setReducerClass(FirstReducerArrayListPairs.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setNumReduceTasks(numReducers);
		
		FileInputFormat.addInputPath(job, new Path(inDir));
		FileOutputFormat.setOutputPath(job, new Path(outDir));

		job.waitForCompletion(true);

	}

	public static void SecondRun(String inDir, String outDir, int numReducers, String tau) throws Exception {
		/*
		 * Run Mapper 2 and Reducer 2
		 */

		Configuration conf = new Configuration();
		conf.set("tau", tau);
		
		Job job = Job.getInstance(conf, "Second Run");
		
		job.setJarByClass(SetSimJoin.class);

		// set the Map class
		job.setMapperClass(SecondMapper.class);
		job.setMapOutputKeyClass(IntPair.class);
		job.setMapOutputValueClass(Text.class);
		
		// set the reducer class
		job.setReducerClass(SecondReducer.class);
		job.setOutputKeyClass(IntPair.class);
		job.setOutputValueClass(Text.class);
		job.setNumReduceTasks(numReducers); // TODO: check should be one?? numReducers
		
		FileInputFormat.addInputPath(job, new Path(inDir));
		
		Path outPath = new Path(outDir);
		FileOutputFormat.setOutputPath(job, outPath);
		
		job.setSortComparatorClass(IndexPairComparator.class);

		job.waitForCompletion(true);
	}
	
	
    public static void main(String[] args) throws Exception {
		/*
		 * Main Driver Function
		 */
		IN = args[0];
		OUT = args[1];
		String tau = args[2];
		nRED = Integer.valueOf(args[3]);

		FirstRun(IN, OUT+"_TMP", nRED, tau);
		
		SecondRun(OUT+"_TMP", OUT, nRED, tau);

	}

}

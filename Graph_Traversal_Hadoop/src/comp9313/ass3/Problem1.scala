/* 
 * comp9313 assignment 3 - Problem 1
 * by Irah Wajchman z3439745
 * 
 */
package comp9313.ass3

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

object Problem1 {
	def main(args: Array[String]) {
	    // set context and read in args
      val conf = new SparkConf().setAppName("Problem1").setMaster("local")
      val sc = new SparkContext(conf)
      val inputFile = args(0)
      val outputFolder = args(1)
      val k = args(2).toInt
      
      // split lines of input file, convert to lower-case, remove duplicate words from each line
      val textFile = sc.textFile(inputFile).map(_.toLowerCase.split("[\\s*$&#/\"'\\,.:;?!\\[\\](){}<>~\\-_]+").distinct)
      
      // flat map all words then convert to (word, 1) pair
      val wordCount = textFile.flatMap(x=>x).filter(_.matches("^[a-z].*")).map(x=>(x,1))
      
      // reduce by word and sum the instances of each word, sort alphabetically then by the count value
      val wordTotal = wordCount.reduceByKey(_+_).sortByKey(true).map(_.swap).sortByKey(false).map(_.swap)
      
      // format output string as per spec.
      val out = wordTotal.map(x => s"${x._1}\t${x._2}")
      
      // send output to file
      sc.parallelize(out.take(k)).saveAsTextFile(outputFolder)
	}
}
/* 
 * comp9313 assignment 3 - Problem 2
 * by Irah Wajchman z3439745
 * 
 */
package comp9313.ass3

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

object Problem2 {
  	def main(args: Array[String]) {
  	  	  // set context and read in args
      val conf = new SparkConf().setAppName("Problem2").setMaster("local")
      val sc = new SparkContext(conf)
      val inputFile = args(0)
      val outputFolder = args(1)
      
      //read input file, split on white space, swap argument order of input
      val in = sc.textFile(inputFile).map(_.split("\\s")).map(x=>(x(1).toInt,x(0).toInt))
      
      // reduce by key, aggregate values into sorted adjacency list. Sort by key then, format output string
      val out = in.groupByKey.mapValues(_.toList.sorted).sortByKey(true).map(x=> s"${x._1}\t${x._2.mkString(",")}")

      // output to file
      out.saveAsTextFile(outputFolder)
  	}
}
/**
 * Created by Michael on 11/13/15.
 */

import java.util.StringTokenizer
import java.util.logging.{Level, Logger, FileHandler, LogManager}

import org.apache.spark.api.java.JavaRDD
import scala.sys.process._
import scala.io.Source

import java.io.File
import java.io._





class Test extends userTest[String] {

  def usrTest(inputRDD: JavaRDD[String], lm: LogManager, fh: FileHandler): Boolean = {
    val logger: Logger = Logger.getLogger(classOf[Test].getName);
    lm.addLogger(logger)
    logger.addHandler(fh)
    var returnValue = false;
    val spw = new sparkOperations()
    val result = spw.sparkWorks(inputRDD)
    val output  = result.collect()
    val fileName = "/Users/Michael/IdeaProjects/Classification/file2"
    val file = new File(fileName)

    inputRDD.saveAsTextFile(fileName)
    Seq("hadoop", "jar", "/Users/Michael/Documents/UCLA Senior/F15/Research-Fall2015/benchmark/examples/HistogramRatings.jar", "org.apache.hadoop.examples.HistogramRatings", fileName, "output").!!

    var truthList:Map[Integer, Integer] = Map()
    for(line <- Source.fromFile("/Users/Michael/IdeaProjects/Histogram_ratings_lineageDD/output/part-00000").getLines()) {
      val token = new StringTokenizer(line)
      val bin :Integer = token.nextToken().toInt
      val number :Integer = token.nextToken().toInt
      truthList += (bin -> number)
      //logger.log(Level.INFO, "TruthList[" + (truthList.size - 1) + "]: " + bin + " : "+ number)
    }


    val itr = output.iterator()
    while (itr.hasNext) {
      val tupVal = itr.next()
      if (tupVal._1 != 0.0f) {
        val binName = tupVal._1.toInt
        val num = tupVal._2.toInt
        //logger.log(Level.INFO, "Output: " + binName + " : " + num)
        if (truthList.contains(binName)) {
          if (num != truthList.get(binName).get){
            returnValue = true
          }
        } else returnValue = true
      }
    }

    val outputFile = new File("/Users/Michael/IdeaProjects/Histogram_ratings_lineageDD/output")

    if (file.isDirectory) {
      for (list <- Option(file.listFiles()); child <- list) child.delete()
    }
    file.delete
    if (outputFile.isDirectory) {
      for (list <- Option(outputFile.listFiles()); child <- list) child.delete()
    }
    outputFile.delete
    return returnValue
  }
}

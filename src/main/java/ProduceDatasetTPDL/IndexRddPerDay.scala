package ProduceDatasetTPDL

import java.text.SimpleDateFormat
import java.util.Date

import SpamDetection.ProcessingOfRow
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.{DateTime, Period}

object IndexRddPerDay {

  def dateRange(from: DateTime, to: DateTime, step: Period): Iterator[DateTime]      =Iterator.iterate(from)(_.plus(step)).takeWhile(!_.isAfter(to))

  val from = new DateTime().withYear(2013).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0)
  val to = new DateTime().withYear(2017).withMonthOfYear(4).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0)

  def main(args: Array[String]) {
    val sdf: SimpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
    val sdf_2: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

    def conf = new SparkConf().setAppName(IndexRddPerDay.getClass.getName)
    val processingOfRow: ProcessingOfRow = new ProcessingOfRow
    val sc = new SparkContext(conf)
    println("Hello, world!")
    sc.setLogLevel("ERROR")

    val unlabeledInput = sc.textFile(args(0))


//    dateRange(from, to, new Period().withMonths(1)).toList.foreach { step =>
    dateRange(from, to, new Period().withDays(1)).toList.foreach { step =>
      println(step)
      val unlabeledMapped = unlabeledInput.filter{ line =>
        val parts = line.split('\t')
        val stringDate = parts(2)
        val temp: Date = sdf.parse(stringDate)
        try {

          val date: DateTime = new DateTime(temp)

          step.getMonthOfYear.equals(date.getMonthOfYear) && step.getYear.equals(date.getYear) && step.getDayOfMonth.equals(date.getDayOfMonth)

        } catch {
          case e: java.lang.NumberFormatException => println(line, e)
            false
        }
      }

      if (!unlabeledMapped.isEmpty()){
        unlabeledMapped.saveAsTextFile("indexed/" + sdf_2.format(step.toDate))
      }
    }

  }

}

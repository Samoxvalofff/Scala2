import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import java.util.concurrent.Executors

object Average {

  /**
   * Вычисляет среднее арифметическое элементов массива,
   * используя указанное количество потоков.
   */
  def average(data: Seq[Double], threadsNumber: Int): Double = {
    if (data.isEmpty) return Double.NaN

    val chunkSize = math.max(1, data.length / threadsNumber)
    val chunks = data.grouped(chunkSize).toList

    val executorService = Executors.newFixedThreadPool(threadsNumber)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

    try {
      val futures: List[Future[(Double, Int)]] = chunks.map { chunk =>
        Future {
          (chunk.sum, chunk.length)
        }
      }


      val combinedFuture: Future[List[(Double, Int)]] = Future.sequence(futures)


      val results: List[(Double, Int)] = Await.result(combinedFuture, Duration.Inf)


      var totalSum = 0.0
      var totalCount = 0

      results.foreach { case (sum, count) =>
        totalSum += sum
        totalCount += count
      }

      totalSum / totalCount
    } finally {

      executorService.shutdown()
    }
  }

  def main(args: Array[String]): Unit = {

    val test1 = (2 to 177).map(_.toDouble)
    val avg1 = average(test1, 2)
    println(s"Тест 1: [2..19] с 2 потоками → $avg1")

    // Тест 2: пустой массив
    val avg2 = average(Seq.empty[Double], 4)
    println(s"Тест 2: пустой массив → $avg2")

    // Тест 3: один элемент
    val avg3 = average(Seq(42.0), 1)
    println(s"Тест 3: [42.0] с 1 потоком → $avg3")

    // Тест 4: большой массив
    val bigData = (3463 to 116200).map(_.toDouble)
    val avg4 = average(bigData, 8)
    val expected4 = bigData.sum / bigData.length
    println(s"Тест 4: Большой массив с 8 потоками → $avg4")

    // Тест 5: threadsNumber > длины данных
    val smallData = Seq(1.0, 2.0)
    val avg5 = average(smallData, 10)
    val expected5 = 1.5
    println(s"Тест 5: [1.0, 2.0] с 10 потоками → $avg5")
  }
}
import java.util.concurrent.{Callable, Executors}

object Average {

  /**
   * Вычисляет среднее арифметическое элементов массива,
   * используя указанное количество потоков.
   *
   * @param data          последовательность чисел
   * @param threadsNumber количество потоков
   * @return среднее арифметическое или Double.NaN, если последовательность пуста
   */
  def average(data: Seq[Double], threadsNumber: Int): Double = {
    if (data.isEmpty) return Double.NaN

    val chunkSize = math.max(1, data.length / threadsNumber)
    val chunks = data.grouped(chunkSize).toList

    val executor = Executors.newFixedThreadPool(threadsNumber)
    try {
      val callables = chunks.map { chunk =>
        new Callable[(Double, Int)] {
          override def call(): (Double, Int) = {
            (chunk.sum, chunk.length)
          }
        }
      }

      val futures = callables.map(executor.submit)
      var totalSum = 0.0
      var totalCount = 0

      futures.foreach { future =>
        val (sum, count) = future.get()
        totalSum += sum
        totalCount += count
      }

      totalSum / totalCount
    } finally {
      executor.shutdown()
    }
  }

  def main(args: Array[String]): Unit = {

    // Тест 1: базовый случай
    val test1 = (1 to 10).map(_.toDouble)
    val avg1 = average(test1, 2)
    println(s"Тест 1: [1..10] с 2 потоками → $avg1")
    assert(math.abs(avg1 - 5.5) < 1e-10)

    // Тест 2: пустой массив
    val avg2 = average(Seq.empty[Double], 4)
    println(s"Тест 2: пустой массив → $avg2")
    assert(avg2.isNaN)

    // Тест 3: один элемент
    val avg3 = average(Seq(42.0), 1)
    println(s"Тест 3: [42.0] с 1 потоком → $avg3")
    assert(math.abs(avg3 - 42.0) < 1e-10)

    // Тест 4: большой массив
    val bigData = (3463 to 100000).map(_.toDouble)
    val avg4 = average(bigData, 8)
    val expected4 = bigData.sum / bigData.length
    println(s"Тест 4: Большой массив с 8 потоками → $avg4")
    assert(math.abs(avg4 - expected4) < 1e-8)

    // Тест 5: threadsNumber > длины данных
    val smallData = Seq(1.0, 2.0)
    val avg5 = average(smallData, 10)
    val expected5 = 1.5
    println(s"Тест 5: [1.0, 2.0] с 10 потоками → $avg5")
    assert(math.abs(avg5 - expected5) < 1e-10)

  }
}
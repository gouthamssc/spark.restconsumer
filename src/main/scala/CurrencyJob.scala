
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object CurrencyJob {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("application.conf").getConfig("spark")
    implicit val spark: SparkSession =
      SparkSession.builder
        .appName("Currency converter")
        .getOrCreate()
    val kafkaURI = config.getString("kafka-cluster")
    val checkpointDir = config.getString("checkpoint-path")
    val batchSize = config.getInt("batch-size")
    val converter = CurrencyConverter(new HttpClientImpl(config.getString("currency-api")), batchSize)

    import CurrencyRateDataFrame._

    val stream = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaURI)
      .option("subscribe", "currency_requests")
      .option("startingOffsets", "earliest")
      .load()
      .convertCurrency(converter)
      .select(to_json(struct("id", "from_currency", "initial", "converted", "to_currency"))
        .alias("value"))
      .writeStream
      .format("kafka")
      .outputMode("append")
      .option("kafka.bootstrap.servers", kafkaURI)
      .option("topic", "currency_responses")
      .option("checkpointLocation", checkpointDir)
      .start()

    stream.awaitTermination()
    spark.stop()
  }
}


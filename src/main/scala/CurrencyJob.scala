import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object CurrencyJob extends App {
  val config = ConfigFactory.load("application.conf").getConfig("spark")
  private implicit val spark: SparkSession = SparkSession.builder
    .master("local[4]")
    .appName("Currency converter")
    .config("spark.driver.memory", "2g")
    .getOrCreate()

  val rootLogger = Logger.getRootLogger
  rootLogger.setLevel(Level.ERROR)

  Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
  Logger.getLogger("org.spark-project").setLevel(Level.ERROR)

  val checkpointDir = config.getString("checkpoint-path")
  val df = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "currency_requests")
    .option("startingOffsets", "earliest")
    .load()

  CurrencyConverter(new HttpClientImpl(config.getString("currency-api")))
    .convertCurrency(df)
    .select(to_json(struct("id", "from_currency", "initial", "converted", "to_currency"))
      .alias("value"))
    .writeStream
    .format("kafka")
    .outputMode("append")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("topic", "currency_responses")
    .option("checkpointLocation", checkpointDir)
    .start()
    .awaitTermination()
}


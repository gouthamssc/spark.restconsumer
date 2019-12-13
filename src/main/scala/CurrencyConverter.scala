import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

case class CurrencyRequest(id: String, value: Double, from_currency: String, to_currency: String)

case class CurrencyResponse(id: String, initial: Double, converted: Double, from_currency: String, to_currency: String)

case class CurrencyConverter(httpClient: HttpClientTrait, batchSize: Int) {

  def convertCurrency(df: DataFrame)(implicit spark: SparkSession): Dataset[CurrencyResponse] = {

    import spark.implicits._

    val schema = new StructType()
      .add("value", IntegerType)
      .add("from_currency", StringType)
      .add("to_currency", StringType)

    val uuid = udf(() => java.util.UUID.randomUUID().toString)

    df.select($"value".cast("string"))
      .where(from_json($"value", schema).isNotNull)
      .select(from_json($"value", schema).as("value"))
      .selectExpr("value.*")
      .withColumn("id", uuid())
      .as[CurrencyRequest]
      .mapPartitions(requests => new CurrencyResponseIterator(requests, httpClient, batchSize))
  }
}

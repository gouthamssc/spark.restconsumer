import org.apache.spark.sql.{DataFrame, SparkSession}

object CurrencyRateDataFrame {
  import scala.language.implicitConversions

  implicit def createCurrencyRateDataFrame(dataFrame: DataFrame): CurrencyRateDataFrame =
    new CurrencyRateDataFrame(dataFrame)
}

class CurrencyRateDataFrame(dataFrame: DataFrame) {
  def convertCurrency(converter: CurrencyConverter)(implicit spark: SparkSession) =
    converter.convertCurrency(dataFrame)(spark)
}

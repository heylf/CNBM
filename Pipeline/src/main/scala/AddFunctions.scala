import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.ml.common.LabeledVector
import org.apache.flink.ml.math.DenseVector
import math.pow
import math.sqrt

object AddFunctions {

  // *************************************************************************
  //     USER DATA TYPES
  // *************************************************************************

  case class One(stage: String, nodes: String, ratio: String, days: String)
  case class Patient(id: String, nodes: String, ratio: String, days: String)

  // *************************************************************************
  //     UTIL METHODS
  // *************************************************************************

  // http://stackoverflow.com/questions/19044114/how-to-find-the-largest-element-in-a-list-of-integers-recursively
  // find max element in a list
  def min(xs: List[Double]): Option[Double] = xs match {
    case Nil => None
    case List(x: Double) => Some(x)
    case x :: y :: rest => min( (if (x < y) x else y) :: rest )
  }

  // http://stackoverflow.com/questions/20477201/splitting-a-scala-seq-dataset-into-a-training-and-testing-set
  // splittin data into train and test data
  def splitIntoTrainingAndTest[T](all: DataSet[T], startpoint: Int, endpoint: Int, shuffled_list: List[Int]):
  (DataSet[T], DataSet[T]) = {

    // slice the index list of the patients into a block specified by the start and endpoint
    val r = shuffled_list.slice(startpoint, endpoint)

    // go through the data twice
    var iter1 = 0
    var iter2 = 0
    (
      // train data (if iterator is not part of the index list, i.e. part of the rest 90%
      // take this line of data)
      all.filter({ i: T => iter1 = iter1 + 1
        !r.contains(iter1)}),
      // test data (if iterator is part of the index list take this line of data)
      all.filter({ i: T => iter2 = iter2 + 1
        r.contains(iter2) })

      )
  }

  /*  convert tain Datasets to Labeledvector like '283.70973413888356 DenseVector(56.77569)'
     where first col = y and second col = x
     convert test Dataset to Densevector where entries are x
     this has to be done for the linear regression function
     [http://ci.apache.org/projects/flink/flink-docs-master/libs/ml/quickstart.html]
 */
  def TranformData(train: DataSet[One], test: DataSet[One]):
  (DataSet[LabeledVector], DataSet[DenseVector],DataSet[LabeledVector]) = {

    // make square root transformation as a linearization
    val transformTrain = train
      .map { tuple =>
      val list = tuple.productIterator.toList
      val numList = list.map(_.asInstanceOf[String].toDouble)
      //LabeledVector(pow(numList(0), 2), DenseVector(numList(1),numList(2)))
      LabeledVector(numList(0), DenseVector(numList(1),numList(2)))
    }

    val tranformTest = test
      .map { tuple =>
      val list = tuple.productIterator.toList
      val numList = list.map(_.asInstanceOf[String].toDouble)
      DenseVector(numList(1),numList(2))
    }

    // this dataset is only needed for a personal check-up to see if the how good
    // the model can reproduce the labels of the test data
    val original = test
      .map { tuple =>
      val list = tuple.productIterator.toList
      val numList = list.map(_.asInstanceOf[String].toDouble)
      LabeledVector(numList(0), DenseVector(numList(1),numList(2)))
    }

    // return all datasets
    (transformTrain, tranformTest, original)
  }



  // method calculates the prediction error
  def CalcPredError(predictions: DataSet[LabeledVector], original: DataSet[LabeledVector],
       outputPath: String, outputPath2: String, outputPath3: String): (DataSet[LabeledVector], Double) ={

    var iter = 0

    // so method first gives each predicted label and each original label an index
    // such that both datasets can be joined based on the indices
    // than the joined dataset is used to calculate the prediciton error

    // give each predicted label an index
    // the predicted label is rounded 1.24 = 1.0
    // because the original values have no decimal numbers
    val transformPred = predictions
      .map { tuple =>
      iter = iter + 1
      LabeledVector(iter, DenseVector(BigDecimal(tuple.label).setScale(0, BigDecimal.RoundingMode.HALF_UP).toDouble))
    }

    iter = 0

    // give each original label an index
    val tranformOrg = original
      .map { tuple =>
      iter = iter + 1
      LabeledVector(iter, DenseVector(tuple.label))
    }

    // join prediction and origianl
    // you get a dataset with two columns (first: prediciton, sec: original)
    val JoinPredictionAndOriginal = transformPred.join(tranformOrg).where(0).equalTo(0) {
      (l, r) => (l.vector.head._2, r.vector.head._2)
    }

    // creat a tuple of each entry in the joint dataset
    // List( (1.0, 1.0), (2.0, 2.0), ... (1.0,1.0) )
    val list_JoinPredictionAndOriginal = JoinPredictionAndOriginal.collect

    // lenght of the list
    val N = list_JoinPredictionAndOriginal.length

    // calculate the residual sum
    // e.g. residualSum = (1.0-1.0)^2 + (2.0-2.0)^2 + ... + (1.0-1.0)^2
    val residualSum = list_JoinPredictionAndOriginal.map {
      num => pow((num._1 - num._2), 2)
    }.sum

    // calculates the realtive error of the predicitons
    val predictionError = sqrt(residualSum / N)

    print("\n" + predictionError + "\n")

    original.writeAsCsv(outputPath, "\n", " ", WriteMode.OVERWRITE)
    transformPred.writeAsCsv(outputPath2, "\n", " ", WriteMode.OVERWRITE)
    //JoinPredictionAndOriginal.writeAsCsv(outputPath3, "\n", " ", WriteMode.OVERWRITE)

    // returns the prediction and the prediction error
    (predictions,predictionError)
  }


  // read in the data
  def getLineDiseaseDataSet(env: ExecutionEnvironment , inputpath: String): DataSet[One] = {
    env.readCsvFile[One](
      inputpath,
      fieldDelimiter = "\t",
      includedFields = Array(1,2,3,4) )
  }

  // read in the patients
  def getLinePatients(env: ExecutionEnvironment , inputpath: String): DataSet[Patient] = {
    env.readCsvFile[Patient](
      inputpath,
      fieldDelimiter = "\t",
      includedFields = Array(0,1,2,3) )
  }

  // http://stackoverflow.com/questions/4604237/how-to-write-to-a-file-in-scala
  // method to write something into a file
  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

}

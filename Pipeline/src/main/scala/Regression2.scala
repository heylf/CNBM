import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.ml.common.LabeledVector
import org.apache.flink.ml.math.DenseVector
import org.apache.flink.ml.regression.MultipleLinearRegression
import scala.util.Random

import java.io._

object Regression2 {

  def buildModelRegression(env: ExecutionEnvironment, train: DataSet[AddFunctions.One], test: DataSet[AddFunctions.One],
         outputpath1: String, outputpath2: String, outputpath3: String):
          (DataSet[LabeledVector], Double, Double, Double, Double) = {

    // tranform train and test data in the necessary dataformat
    val (transformTrain, tranformTest, original) = AddFunctions.TranformData(train, test)

    //--------------- REGRESSION ------------------------------------------------------------
    // http://ci.apache.org/projects/flink/flink-docs-master/libs/ml/

    // set linear regression with parameters:
    val mlr = MultipleLinearRegression()
    .setStepsize(0.001)
    .setIterations(1000000000)
    .setConvergenceThreshold(0.001)

    // Setup polynomial feature transformer of degree 3
    //val polyFeatures = PolynomialFeatures()
    //  .setDegree(3)

    // Create pipeline PolynomialFeatures -> MultipleLinearRegression
    //val pipeline = polyFeatures.chainPredictor(mlr)

    // do linear regression and time the method
    // val start: Long = System.currentTimeMillis()
    val model = mlr.fit(transformTrain)
    //val end: Long = System.currentTimeMillis()

    // The fitted model can now be used to make predictions
    val predictions = mlr.predict(tranformTest)
    // --------------------------------------------------------------------------------------

    // print time the regression needed
    //print("\n time needed: " + (end - start) + "ms \n")

    // get the weights and the intercerpt of the model

    val intercerpt = mlr.weightsOption.get.collect().head.intercept
    val weight1 = mlr.weightsOption.get.collect().head.weights(0)
    val weight2 = mlr.weightsOption.get.collect().head.weights(1)

    print("\n intercept: " + intercerpt + "\n")
    print("\n weight number of lymphnodes: " + weight1 + "\n")
    print("\n weight her2/cent17 ratio: " + weight2 + "\n")
    //print("\n weight day of last followup: " + mlr.weightsOption.get.collect().head.weights(2) + "\n")

    // emit prediction result
    val (predictions_re, predictionError) = AddFunctions.CalcPredError(predictions, original, outputpath1, outputpath2, outputpath3)

    (predictions_re, predictionError, intercerpt, weight1, weight2)

  }


  def mainRegression(inputpath: String, patientpath: String, outputpath1: String, outputpath2: String,
                     outputpath3: String, logpath: String) {

    // get execution environment
    val env = ExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    // get data
    val data = AddFunctions.getLineDiseaseDataSet(env, inputpath)

    // number of lines (number of patients)
    val NumberOfPatients = data.count.toInt
    print("\n Number of Patients " + NumberOfPatients + "\n")

    //val NumberOfPatients : Integer = 205

    // make a 10%-fold cross validation (10 blocks)
    // where 10% are the test data and 90% train data

    // set starting and endpoint for the block
    var startpoint = 0
    var endpoint = (NumberOfPatients * 0.1).toInt

    // list where each prediction error for each k-fold validation is saved
    var listPredictionErros : List[Double] = List()

    // random shuffled list of indices for each patient
    val shuffled_list = Random.shuffle((1 to NumberOfPatients).toList)

    // go over each block
    for ( a <- 1 to 10 ) {

      // for the last block the endpoint is the end of the patients list
      if (a == 10) endpoint = NumberOfPatients
      else endpoint = a * (NumberOfPatients * 0.1).toInt

      // split data into test and train data
      val (train, test) = AddFunctions.splitIntoTrainingAndTest(data, startpoint, endpoint, shuffled_list)

      startpoint = endpoint

      // build now the model with the test and train data
      // the method returns the prediction from the test data and the prediction Error
      val (predictions, predictionError, intercerpt, weight1, weight2) = buildModelRegression(env, train, test,  outputpath1, outputpath2, outputpath3)

      // save prediction error in the list
      listPredictionErros = listPredictionErros :+ predictionError

    }

    // find the minimum prediction error in the list
    val minPredError = AddFunctions.min(listPredictionErros).get

    // get the index of the minimum prediction error in the list
    // index is one less cause it counts from 0 to n
    val indexMinPredError = listPredictionErros.indexOf(minPredError)

    print("\n" + listPredictionErros)
    print("\n" + minPredError)
    print("\n" + indexMinPredError + "\n")

    // reasign the starting and endpoint of the minimum prediciton error
    startpoint = indexMinPredError * (NumberOfPatients * 0.1).toInt

    if ( indexMinPredError == 9 ) endpoint = NumberOfPatients
    else endpoint = (indexMinPredError + 1) * (NumberOfPatients * 0.1).toInt

    // build up the model with the minimum prediction error
    val (train, test) = AddFunctions.splitIntoTrainingAndTest(data, startpoint, endpoint, shuffled_list)
    val (transformTrain, tranformTest, original) = AddFunctions.TranformData(train, test)

    val mlr = MultipleLinearRegression()
      .setStepsize(0.001)
      .setIterations(1000000000)
      .setConvergenceThreshold(0.001)

    val model = mlr.fit(transformTrain)

    val intercerpt = mlr.weightsOption.get.collect().head.intercept
    val weight1 = mlr.weightsOption.get.collect().head.weights(0)
    val weight2 = mlr.weightsOption.get.collect().head.weights(1)

    // now predict the cancer stage

    // get data which you sampled from patients and now want to see which cancerstage they have
    val predictCancerStage = AddFunctions.getLinePatients(env, patientpath)

    // transform data into necassary data format
    val tranformPatientData = predictCancerStage
      .map { tuple =>
      val list = tuple.productIterator.toList
      val numList = list.tail.map(_.asInstanceOf[String].toDouble)
      DenseVector(numList(0),numList(1))
    }

    // make predicitons
    val predictions = mlr.predict(tranformPatientData)

    // collect the patients IDs from the Dataset
    var patientList: List[String] = List()
    predictCancerStage.collect().map{tupel => patientList = patientList :+ tupel.id }

    // round the cancer stage from the prediction and build up a list
    var stagesList: List[(Double,Serializable)] = List()
    predictions.collect().map{
      tuple => stagesList = stagesList :+
        (
          BigDecimal(tuple.label).setScale(0, BigDecimal.RoundingMode.HALF_UP).toDouble,
          tuple.vector
        )
    }

    // join the patient IDs with the prediction values
    val joinPLandSL = patientList.zip(stagesList)

    // write the detials of the model into a log file
    val datawrite = Array("relative error: " + minPredError,
      "model:    " + "y = x1 * " + weight1 + " + x2 * " + weight2 + " + " + intercerpt,
      "", "(PatienId, Cancer Stage, number of lymphe nodes, her2/cent17 ratio)")

    AddFunctions.printToFile(new File(logpath)) { p =>
      datawrite.foreach(p.println)
      joinPLandSL.foreach(p.println)
    }

    predictions.writeAsCsv(outputpath3, "\n", " ", WriteMode.OVERWRITE)

    env.execute("Scala Regression")
  }

}


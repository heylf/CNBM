//package org.apache.flink.examples.scala

import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.ml.classification.SVM
import org.apache.flink.api.scala._
import org.apache.flink.ml.common.LabeledVector
import org.apache.flink.ml.math.DenseVector
import scala.util.Random

import java.io._

object Classification {

  def buildSVM(env: ExecutionEnvironment, train: DataSet[AddFunctions.One], test: DataSet[AddFunctions.One],
               outputpath1: String, outputpath2: String, outputpath3: String): (DataSet[LabeledVector], Double) ={

    // tranform train and test data in the necessary dataformat
    val (transformTrain, tranformTest, original) = AddFunctions.TranformData(train, test)

    //--------------- SVM ------------------------------------------------------------
    // https://ci.apache.org/projects/flink/flink-docs-release-0.9/libs/ml/svm.html
    // Create the SVM learner

    val svm = SVM().
      setBlocks(10).
      setIterations(100).
      setLocalIterations(100).
      setRegularization(0.002).
      setStepsize(0.1).
      setSeed(123)

    // Learn the SVM model
    svm.fit(transformTrain)

    // Calculate the predictions for the testing data set
    val predictions = svm.predict(tranformTest)

    // emit prediction result
    AddFunctions.CalcPredError(predictions, original, outputpath1, outputpath2, outputpath3)

  }

  def mainSVM(inputpath: String, patientpath: String, outputpath1: String, outputpath2: String,
              outputpath3: String, logpath: String) {

    // get execution environment
    val env = ExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    // read in data
    val data = AddFunctions.getLineDiseaseDataSet(env, inputpath)

    // number of lines (number of patients)
    val NumberOfPatients = data.count.toInt
    print("\n Number of Patients " + NumberOfPatients + "\n")

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
      val (predictions, predictionError) = buildSVM(env, train, test, outputpath1, outputpath2, outputpath3)

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

    val svm = SVM().
      setBlocks(10).
      setIterations(100).
      setLocalIterations(100).
      setRegularization(0.002).
      setStepsize(0.1).
      setSeed(123)

    // Learn the SVM model
    svm.fit(transformTrain)

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
    val predictions = svm.predict(tranformPatientData)

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

    // write the details of the model into a log file
    val datawrite = Array("prediction error: " + minPredError, "",
      "(PatienId, Cancer Stage, number of lymphe nodes, her2/cent17 ratio)")

    AddFunctions.printToFile(new File(logpath)) { p =>
      datawrite.foreach(p.println)
      joinPLandSL.foreach(p.println)
    }

    predictions.writeAsCsv(outputpath3, "\n", " ", WriteMode.OVERWRITE)

    env.execute("Scala Classification")

  }

}
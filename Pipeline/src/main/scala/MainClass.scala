//import org.apache.flink.examples.scala.{Classification, Regression2}

object MainClass {

  // *************************************************************************
  //     Globals
  // *************************************************************************

  // /Users/Baal/Dropbox/UniIreneFlo/Project/Program
  var workingDir: String = null
  var threGenes: Double = -2.0
  var threProteins: Double = -2.0

  // *************************************************************************
  //     Arguments
  // *************************************************************************

  // error message if you do not specify the arguments
  private def parseParameters(args: Array[String]): Boolean = {
    if (args.length == 3) {
      workingDir = args(0)
      threGenes = args(1).toDouble
      threProteins = args(2).toDouble
      true
    } else {
      System.err.println("<working dir path> <threshold for genes> <threshold for proteins>");
      false
    }
  }

  def main(args: Array[String]): Unit = {

    if (!parseParameters(args)) {
      return
    }

    print("Working directory: " + workingDir  + "\n")
    print("threshold Genes: " + threGenes  + "\n")
    print("threshold Protiens: " + threProteins  + "\n \n")

    // *************************************************************************
    //     Classification
    // *************************************************************************

    // call the regression
    print("START REGRESSION \n")
    Regression2.mainRegression(
      workingDir + "/Input/Classification/Project_Regression_Matrix.txt", 
      workingDir + "/Input/Classification/Patients.txt", 
      workingDir + "/IntermediateFiles/Regression/RegressionOrigianls.txt", 
      workingDir + "/IntermediateFiles/Regression/RegressionPredictions1.txt", 
      workingDir + "/IntermediateFiles/Regression/RegressionPredictions2.txt", 
      workingDir + "/Output/Regression/RegressionOut.txt"
    )
    print("END REGRESSION \n")

    // cal the SVM (classification)
    print("START SVM \n")
    Classification.mainSVM(
      workingDir + "/Input/Classification/Project_Regression_Matrix.txt", 
      workingDir + "/Input/Classification/Patients.txt", 
      workingDir + "/IntermediateFiles/SVM/SVMOrigianls.txt", 
      workingDir + "/IntermediateFiles/SVM/SVMPredictions1.txt", 
      workingDir + "/IntermediateFiles/SVM/SVMPredictions2.txt", 
      workingDir + "/Output/SVM/SVMOut.txt"
    )
    print("END SVM \n")


    // *************************************************************************
    //     Gene expression
    // *************************************************************************
    // call the calculation of the correlation (gene expression)
    print("START CORRELATION \n")
    Correlation.mainCorrelation(
      workingDir + "/Input/GeneExpression",
      workingDir + "/IntermediateFiles/GeneExpression/edges.txt",
      workingDir + "/IntermediateFiles/GeneExpression/means.txt",
      workingDir + "/IntermediateFiles/GeneExpression/edges_partial.txt",
      threGenes // we used 0.9 and -0.9
    )
    print("END CORRELATION \n")

    // call the CommunityDetection of the correlation (gene expression)
    print("START COMMUNITY DETECTION \n")
    CommunityDetection.mainCommunityDetection(
      workingDir + "/IntermediateFiles/GeneExpression/edges.txt",
      workingDir + "/IntermediateFiles/GeneExpression/communities.txt",
      100,
      0.5
    )
    print("END COMMUNITY DETECTION \n")

    // call the module to find the 3 biggest modules
    print("START LARGEST MODULES \n")
    LargestModules.mainLargestMod(
      workingDir + "/IntermediateFiles/GeneExpression/communities.txt",
      workingDir + "/IntermediateFiles/GeneExpression/"
    )
    print("END LARGEST MODULES \n")


    // call the module to find the 3 biggest modules
    print("START PATIENT \n")
    Patient.mainPatient(
      workingDir + "/Input/Patient/GeneExpression/patient.txt",
      workingDir + "/IntermediateFiles/GeneExpression/firstModule.txt",
      workingDir + "/IntermediateFiles/GeneExpression/secondModule.txt",
      workingDir + "/IntermediateFiles/GeneExpression/thirdModule.txt",
      workingDir + "/IntermediateFiles/GeneExpression/means.txt",
      workingDir + "/Output/GeneExpression/",
      workingDir + "/Input/geneToId.txt"
    )
    print("END PATIENT \n")







    // call the CommunityDetection of the correlation (gene expression)
    print("START COMMUNITY DETECTION \n")
    CommunityDetection.mainCommunityDetection(
      workingDir + "/IntermediateFiles/GeneExpression/edges_partial.txt",
      workingDir + "/IntermediateFiles/GeneExpression/communities_partial.txt",
      1000,
      0.5
    )
    print("END COMMUNITY DETECTION \n")

    // call the module to find the 3 biggest modules
    print("START LARGEST MODULES \n")
    LargestModules.mainLargestMod(
      workingDir + "/IntermediateFiles/GeneExpression/communities_partial.txt",
      workingDir + "/IntermediateFiles/GeneExpression/Partial/"
    )
    print("END LARGEST MODULES \n")


    // call the module to find the 3 biggest modules
    print("START PATIENT \n")
    Patient.mainPatient(
      workingDir + "/Input/Patient/GeneExpression/patient.txt",
      workingDir + "/IntermediateFiles/GeneExpression/Partial/firstModule.txt",
      workingDir + "/IntermediateFiles/GeneExpression/Partial/secondModule.txt",
      workingDir + "/IntermediateFiles/GeneExpression/Partial/thirdModule.txt",
      workingDir + "/IntermediateFiles/GeneExpression/means.txt",
      workingDir + "/Output/GeneExpression/Partial/",
      workingDir + "/Input/geneToId.txt"
    )
    print("END PATIENT \n")






    // *************************************************************************
    //     Protein detection
    // *************************************************************************

    // call the calculation of the correlation (gene expression)
    print("START CORRELATION PROTEIN \n")
    CorrelationProtein.mainCorrelationProt(
      workingDir + "/Input/Protein",
      workingDir + "/IntermediateFiles/Protein/edges.txt",
      workingDir + "/IntermediateFiles/Protein/means.txt",
      workingDir + "/IntermediateFiles/Protein/edges_partial.txt",
      threProteins // we used 0.75 and -0.75
    )
    print("END CORRELATION PROTEIN \n")


    // call the CommunityDetection of the correlation (gene expression)
    print("START COMMUNITY DETECTION PROTEIN \n")
    CommunityDetection.mainCommunityDetection(
      workingDir + "/IntermediateFiles/Protein/edges.txt",
      workingDir + "/IntermediateFiles/Protein/communities.txt",
      1000,
      0.5
    )
    print("END COMMUNITY DETECTION PROTEIN \n")

    // call the module to find the 3 biggest modules
    print("START LARGEST MODULES PROTEIN \n")
    LargestModules.mainLargestMod(
      workingDir + "/IntermediateFiles/Protein/communities.txt",
      workingDir + "/IntermediateFiles/Protein/"
    )
    print("END LARGEST MODULES PROTEIN \n")


    // call the module to find the 3 biggest modules
    print("START PATIENT PROTEIN \n")
    Patient.mainPatient(
      workingDir + "/Input/Patient/Protein/patient.txt",
      workingDir + "/IntermediateFiles/Protein/firstModule.txt",
      workingDir + "/IntermediateFiles/Protein/secondModule.txt",
      workingDir + "/IntermediateFiles/Protein/thirdModule.txt",
      workingDir + "/IntermediateFiles/Protein/means.txt",
      workingDir + "/Output/Protein/",
      workingDir + "/Input/proteinToId.txt"
    )
    print("END PATIENT PROTEIN \n")






    // call the CommunityDetection of the correlation (gene expression)
    print("START COMMUNITY DETECTION PROTEIN \n")
    CommunityDetection.mainCommunityDetection(
      workingDir + "/IntermediateFiles/Protein/edges_partial.txt",
      workingDir + "/IntermediateFiles/Protein/communities_partial.txt",
      1000,
      0.5
    )
    print("END COMMUNITY DETECTION PROTEIN \n")

    // call the module to find the 3 biggest modules
    print("START LARGEST MODULES PROTEIN \n")
    LargestModules.mainLargestMod(
      workingDir + "/IntermediateFiles/Protein/communities_partial.txt",
      workingDir + "/IntermediateFiles/Protein/Partial/"
    )
    print("END LARGEST MODULES PROTEIN \n")


    // call the module to find the 3 biggest modules
    print("START PATIENT PROTEIN \n")
    Patient.mainPatient(
      workingDir + "/Input/Patient/Protein/patient.txt",
      workingDir + "/IntermediateFiles/Protein/Partial/firstModule.txt",
      workingDir + "/IntermediateFiles/Protein/Partial/secondModule.txt",
      workingDir + "/IntermediateFiles/Protein/Partial/thirdModule.txt",
      workingDir + "/IntermediateFiles/Protein/means.txt",
      workingDir + "/Output/Protein/Partial/",
      workingDir + "/Input/proteinToId.txt"
    )
    print("END PATIENT PROTEIN \n")
  }

}

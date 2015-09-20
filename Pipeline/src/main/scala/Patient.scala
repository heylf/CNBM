import org.apache.flink.api.scala._
import org.apache.flink.api.scala.table._
import org.apache.flink.core.fs.FileSystem.WriteMode

//case class Data1(bcr_patient_uuid:String, bcr_aliquot_barcode:String)
//case class Data2(bcr_patient_uuid:String, experimental_protocol_type:String)


object Patient {

  def mainPatient(one: String, two: String, three: String, four: String, five: String, six: String, seven: String) {

    /*
    if (!parseParameters(args)) {
      return
    }
    */

    patientPath = one
    firstModule = two
    secondModule = three
    thirdModule = four
    means = five
    result = six
    geneToID = seven

    // get execution environment
    val env = ExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    //gene to id
    val geneID = env.readCsvFile[(Int, String)](
      geneToID,
      fieldDelimiter = "\t",
      includedFields = Array(0,1)
    ).as('ID, 'name)


    //patient
    val patient = env.readCsvFile[(String, String)](
      patientPath,
      fieldDelimiter = "\t",
      includedFields = Array(0,1)
    ).filter( (tuple) => tuple._2 != "null").map( (tuple) => (tuple._1, tuple._2.toDouble)).as('gene, 'ratio)
      .join(geneID).where('gene === 'name).select('ID, 'name, 'ratio)

    //means
    val mean = env.readCsvFile[(Int, Double)](
      means,
      fieldDelimiter = "\t",
      includedFields = Array(0, 1)
    ).as('geneNrMean, 'meanRatio)

    //firstModule
    val first = env.readCsvFile[(Int, Int)](
      firstModule,
      fieldDelimiter = "\t",
      includedFields = Array(0, 1)
    ).as('geneNr, 'placeholder).join(mean).where('geneNr === 'geneNrMean).select('geneNr, 'meanRatio)
    .join(patient).where('ID === 'geneNr).select('ID, 'name, 'ratio, 'meanRatio)

    //secondModule
    val second = env.readCsvFile[(Int, Int)](
      secondModule,
      fieldDelimiter = "\t",
      includedFields = Array(0, 1)
    ).as('geneNr, 'placeholder).join(mean).where('geneNr === 'geneNrMean).select('geneNr, 'meanRatio)
      .join(patient).where('ID === 'geneNr).select('ID, 'name, 'ratio, 'meanRatio)

    //thirdModule
    val third = env.readCsvFile[(Int, Int)](
      thirdModule,
      fieldDelimiter = "\t",
      includedFields = Array(0, 1)
    ).as('geneNr, 'placeholder).join(mean).where('geneNr === 'geneNrMean).select('geneNr, 'meanRatio)
      .join(patient).where('ID === 'geneNr).select('ID, 'name, 'ratio, 'meanRatio)

    // emit result, seperate cols by a tab
    first.writeAsCsv(result + "resultFirst.txt", "\n", "\t", WriteMode.OVERWRITE)
    second.writeAsCsv(result + "resultSecond.txt", "\n", "\t", WriteMode.OVERWRITE)
    third.writeAsCsv(result + "resultThird.txt", "\n", "\t", WriteMode.OVERWRITE)
    // execute program
    env.execute("join two tables")
  }

  // *************************************************************************
  //     UTIL METHODS
  // *************************************************************************

  private var patientPath: String = null
  private var firstModule: String = null
  private var secondModule: String = null
  private var thirdModule: String = null
  private var means: String = null
  private var result: String = null
  private var geneToID: String = null

  /*
  private def parseParameters(args: Array[String]): Boolean = {
    if (args.length == 7) {
      patientPath = args(0)
      firstModule = args(1)
      secondModule = args(2)
      thirdModule = args(3)
      means = args(4)
      result = args(5)
      geneToID = args(6)
      true
    } else {
      System.err.println("patient firstModule secondModule thirdModule means result");
      false
    }
  }
  */

}
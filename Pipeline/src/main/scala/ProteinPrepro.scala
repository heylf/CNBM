import java.io.File

import org.apache.flink.api.scala._
import org.apache.flink.api.scala.table._
import org.apache.flink.core.fs.FileSystem.WriteMode
import scala.collection.JavaConversions._



object ProteinPrepro {

  def main(args: Array[String]) {

    if (!parseParameters(args)) {
      return
    }

    // get execution environment
    val env = ExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    //gene to id
    val NT = env.readCsvFile[(String, String)](
      NTpath,
      fieldDelimiter = "\t",
      includedFields = Array(0,1)
    ).as('protein1, 'exp1)

    val TN = env.readCsvFile[(String, String)](
      TNpath,
      fieldDelimiter = "\t",
      includedFields = Array(0,1)
    ).as('protein2, 'exp2)

    val result = NT.join(TN).where('protein1 === 'protein2).select('protein1, 'protein2)

    for(file <- new File(inputpath).listFiles)
    {
      val patient = env.readCsvFile[(String, String)](
        file.getAbsolutePath,
        fieldDelimiter = "\t",
        includedFields = Array(0,1)
      ).as('protein, 'exp).join(result).where('protein === 'protein1).select('protein, 'exp)
      patient.writeAsCsv(outputpath + file.getName , "\n", "\t", WriteMode.OVERWRITE)
    }

    result.writeAsCsv(resultpath , "\n", "\t", WriteMode.OVERWRITE)

    env.execute("join two tables")
  }

  // *************************************************************************
  //     UTIL METHODS
  // *************************************************************************

  private var NTpath: String = null
  private var TNpath: String = null
  private var resultpath: String = null
  private var inputpath: String = null
  private var outputpath: String = null

  private def parseParameters(args: Array[String]): Boolean = {
    if (args.length == 5) {
      NTpath = args(0)
      TNpath = args(1)
      resultpath = args(2)
      inputpath = args(3)
      outputpath = args(4)
      true
    } else {
      System.err.println("patient firstModule secondModule thirdModule means result");
      false
    }
  }

}

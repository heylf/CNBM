import java.io.*;
import java.util.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

// compute correlation (pearson)
public class CorrelationProtein {

    //main
    public static void mainCorrelationProt(String folderPath, String outputPath, String outputPath2,
                                           String outputPath3, Double threshold) throws IOException {

        //read in data
        //create new file for over whole input folder
        File folder = new File(folderPath);
        //get all files in this folder
        File[] listOfFiles = folder.listFiles();

        int NumberOfPatients = listOfFiles.length;
        // -2 because of the header
        int NumberOfProteins = countLines( folderPath + "/" + listOfFiles[1].getName() ) - 2;

        System.out.println("\n Number of Patients " + NumberOfPatients + "\n");
        System.out.println("\n Number of Proteins " + NumberOfProteins + "\n");

        //matrix to save input (gene expression)
        double[][] geneExpMatrix = new double[NumberOfProteins][NumberOfPatients];

        //go through all files
        int counterFile = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {

                //new reader
                BufferedReader br = null;
                int counterLine = 0;
                try {

                    //open reader on one file
                    br = new BufferedReader(new FileReader(folderPath + "/" + file.getName()));
                    String line = br.readLine();

                    //read in gene exp
                    while (line != null) {
                        //skip first 2 lines
                        if (counterLine > 1) {
                            //split line
                            String[] parts = line.split("\t");
                            //save exp
                            if(!parts[1].equals("null")){
                                Double expValue = Double.parseDouble(parts[1]);
                                geneExpMatrix[counterLine - 2][counterFile] = expValue;
                            }
                        }
                        //read in next line
                        line = br.readLine();
                        counterLine++;
                    }
                } catch (Exception e) {

                } finally {
                    try
                    {
                        if (br != null)
                            br.close( );
                    }
                    catch (Exception e)
                    {
                    }
                }
                //System.out.println(file.getName());
            }
            counterFile++;

            //if(counterFile==1)
            //    System.exit(1);
        }

        //get mean ratios for genes
        double[] geneExpMean = new double[NumberOfProteins];
        Mean meanValue = new Mean();
        for (int i = 0; i < NumberOfProteins; ++i)
        {
            geneExpMean[i] = meanValue.evaluate(geneExpMatrix[i], 0, NumberOfPatients);
        }

        //save means
        //compute correlation
        BufferedWriter bw = null;
        try
        {
            //new writer on output path
            bw = new BufferedWriter(new FileWriter(outputPath2));
            for (int i = 0; i < NumberOfProteins; ++i)
            {
                bw.write(i+1 + "\t" + geneExpMean[i] + "\n");

            }

        }
        catch (IOException e)
        {
        }
        finally
        {
            try
            {
                if (bw != null)
                    bw.close( );
            }
            catch (IOException e)
            {
            }
        }

        // define correlation matrix
        float[][] correlationMatrix = new float[NumberOfProteins][NumberOfProteins];

        //compute correlation
        BufferedWriter bw2 = null;
        try
        {
            //new writer on output path
            bw2 = new BufferedWriter(new FileWriter(outputPath));
            for (int i = 0; i < NumberOfProteins; ++i)
            {
                // do not compute cor double + diagonal not computed
                for (int j = 0; j < i; ++j)
                {
                    // compute pearson correlation
                    double cor = new PearsonsCorrelation().correlation(geneExpMatrix[i], geneExpMatrix[j]);

                    if( Double.isNaN(cor) == true ) {
                        System.out.println("Wrong correlation value, Error 1");
                        System.exit(1);
                    }

                    // add correlation value to correlation matrix
                    correlationMatrix[i][j] = (float) cor;
                    correlationMatrix[j][i] = (float) cor;

                    if (cor > 0.75 || cor < -0.75)
                    {
                        //write out in edges files
                        //use weight one for all edges (like unweighted)
                        bw2.write(i + "\t" + j + "\t" + "1" + "\n");
                    }
                }
            }

        }
        catch (IOException e)
        {
        }
        finally
        {
            try
            {
                if (bw2 != null)
                    bw2.close( );
            }
            catch (IOException e)
            {
            }
        }

        //compute correlation
        BufferedWriter bw3 = null;
        try
        {
            //new writer on output path
            bw3 = new BufferedWriter(new FileWriter(outputPath3));
            for (int i = 0; i < NumberOfProteins; ++i)
            {
                // do not compute cor double + diagonal not computed
                for (int j = 0; j < i; ++j)
                {
                    if ( j != i && (correlationMatrix[i][j] > threshold || correlationMatrix[i][j] < - threshold) )
                    {
                        ArrayList<Float> partialList = new ArrayList<Float>();
                        for (int z = 0; z < NumberOfProteins; ++z) {
                            if ( z != i && z != j) {
                                partialList.add(
                                        partialCorrleation(
                                                correlationMatrix[i][j],
                                                correlationMatrix[i][z],
                                                correlationMatrix[j][z]
                                        )
                                );
                            }
                        }
                        float min = Collections.min(partialList);
                        if ( min >= 0.1 ) {
                            //write out in edges files
                            //use weight one for all edges (like unweighted)
                            bw3.write(i + "\t" + j + "\t" + "1" + "\n");
                        }
                    }
                }
            }

        }
        catch (IOException e)
        {
        }
        finally
        {
            try
            {
                if (bw3 != null)
                    bw3.close( );
            }
            catch (IOException e)
            {
            }
        }

    }


    // partial correlation
    public static float partialCorrleation(Float Cor_AB, Float Cor_AC, Float Cor_BC)
    {
        Float partOne = Cor_AB - Cor_AC * Cor_BC;
        Float partTwo = (float) (1 - Math.pow(Cor_AC, 2));
        Float partThree = (float) (1 - Math.pow(Cor_BC, 2));

        return (float) Math.abs(partOne / Math.sqrt(partTwo * partThree));

    }

    // http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
    // count the number of lines in a file
    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }


}

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//extract the three largest modules (result of comm detection is input)
public class LargestModules {

    //main
    public static void mainLargestMod(String inputPath, String outputPath) {


        //input + output path
        //String inputPath = args[0];
        //String outputPath = args[1];

        //create hashmap
        HashMap<Integer, Integer> modules = new HashMap<Integer, Integer>();

        //new reader
        BufferedReader br = null;
        try {

            //open reader on one file
            br = new BufferedReader(new FileReader(inputPath));
            String line = br.readLine();

            //read in gene exp
            while (line != null) {

                //split line
                String[] parts = line.split("\t");
                //get number of modules
                int moduleNumber = Integer.parseInt(parts[1]);
                //insert module in hashmap
                //check if already in
                if(modules.containsKey(moduleNumber))
                {
                    modules.put(moduleNumber, modules.get(moduleNumber) + 1);
                }
                else {
                    modules.put(moduleNumber, 1);
                }

                //read in next line
                line = br.readLine();
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


        //get the three largest modules
        int first = 0;
        int second = 0;
        int third = 0;

        //map entry -> to get max modules
        HashMap.Entry<Integer, Integer> maxEntry = null;

        //first
        for (HashMap.Entry<Integer, Integer> entry : modules.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }

        //save module, then delete from hashmap to be able to repeat looking for largest (which is then the second largest)
        first = maxEntry.getKey();
        modules.remove(first);
        maxEntry = null;

        //second
        for (HashMap.Entry<Integer, Integer> entry : modules.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }

        second = maxEntry.getKey();
        modules.remove(second);
        maxEntry = null;

        //last
        for (HashMap.Entry<Integer, Integer> entry : modules.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }

        third = maxEntry.getKey();

        //now read in communites files again to get the nodes in the modules
        ArrayList<Integer> firstModule = new ArrayList<Integer>();
        ArrayList<Integer> secondModule = new ArrayList<Integer>();
        ArrayList<Integer> thirdModule = new ArrayList<Integer>();

        BufferedReader br2 = null;

        try {

            //open reader
            br2 = new BufferedReader(new FileReader(inputPath));
            String line = br2.readLine();

            //read in gene exp
            while (line != null) {

                String[] parts = line.split("\t");
                int moduleNumber = Integer.parseInt(parts[1]);

                //check if first
                if(moduleNumber == first)
                {
                    firstModule.add(Integer.parseInt(parts[0]));
                }
                //check if in second
                if(moduleNumber == second)
                {
                    secondModule.add(Integer.parseInt(parts[0]));
                }
                //check if in third
                if(moduleNumber == third)
                {
                    thirdModule.add(Integer.parseInt(parts[0]));
                }

                //read in next line
                line = br2.readLine();
            }
        } catch (Exception e) {

        } finally {
            try
            {
                if (br2 != null)
                    br2.close( );
            }
            catch (Exception e)
            {

            }
        }

        //write out results
        BufferedWriter bw = null;
        try
        {
            //new writer on output path
            bw = new BufferedWriter(new FileWriter(outputPath + "firstModule.txt"));
            for (int i = 0; i < firstModule.size(); ++i)
            {
                bw.write(firstModule.get(i) + "\t" + "0" + "\n");
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

        try
        {
            //new writer on output path
            bw = new BufferedWriter(new FileWriter(outputPath + "secondModule.txt"));
            for (int i = 0; i < secondModule.size(); ++i)
            {
                bw.write(secondModule.get(i) + "\t" + "0" + "\n");
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

        try
        {
            //new writer on output path
            bw = new BufferedWriter(new FileWriter(outputPath + "thirdModule.txt"));
            for (int i = 0; i < thirdModule.size(); ++i)
            {
                bw.write(thirdModule.get(i) + "\t" + "0" + "\n");
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


    }
}

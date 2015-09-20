This Pipeline was created by Irene Ziska and Florian Heyl.
Until now the pipeline is provided with data from the Cancer Genome Atlas (http://cancergenome.nih.gov/) for BRCA. 

INPUT:
In order to run the pipeline, the following folder structure must be created:

/Program
 /Input [should contain a geneToId.txt and a proteinToId.txt]
   /Classification [should contain a file called Patients.txt and a file called Project_Regression_Matrix.txt]
   /GeneExpression [should contain the different input files (gene expression)]
   /Protein [should contain the different input files (protein expression)]
   /Patient:
     /GeneExpression [contains one patient.txt file (same format as input files but the first two lines are deleted)]
     /Protein	[contains one patient.txt file (same format as input files but the first two lines are deleted)]
 /IntermediateFiles
   /GeneExpression 
	/Partial
   /Protein
	/Partial
   /Regression
   /SVM	
 /Output
     /GeneExpression
	/Partial
     /Protein
	/Partial
     /Regression
     /SVM
 /Pipeline [contains the IntelliJ project and a jar file of the pipeline]

RUN:
When downloading everything from the github repository, the folder structure is already created. The folder 'Pipeline' contains the source code. The best thing is to open the project in IntelliJ. But we also provided a jar file. In order to run the program in IntelliJ, create a run configuration for 'MainClass' and define the following arguments:

<working dir path> <threshold for genes> <threshold for proteins>

working dir path: path of the program folder e.g. path/Program
threshold for genes: threshold of the Pearson correlation for the gene expression networks
threshold for proteins: threshold of the Pearson correlation for the protein networks

OUTPUT:

As a result the user get the following main output:

/Output/Regression/RegressionOut.txt [predicted cancer stages for the patients of interest from the MLR, the prediction error and the details of the model]
/Output/SVM/SVMOut.txt 		     [predicted cancer stages for the patients of interest from the SVM and the prediction error]
/Output/GeneExpression/*.txt	     [three biggest modules of the gene expression network based on Pearson correlation]
/Output/GeneExpression/Partial/*.txt	     [three biggest modules of the gene expression network based on Patial correlation]
/Output/Protein/*.txt			[three biggest modules of the protein network based on Pearson correlation]
/Output/Protein/Partial/*.txt			[three biggest modules of the protein network based on Pearson correlation]

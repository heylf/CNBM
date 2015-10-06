# CNBM
The classification and network based method pipeline was built to classify features (e.g., parameters, genes, proteins) that are specifically associated with breast invasive carcinoma (BRCA). For that purpose a pipeline was created that can be split into two major parts. The first part constructs a classifier based on a regression model and a support vector machine (SVM) to predict the cancer stage (spreading or non-spreding cancer type) of a patient by evaluating two parameters the number of lymph nodes positive by hematoxylin and eosin staining and light microscopy and the her2/cent17-ratio. The second part evaluates genes that could play a role in this specific type of cancer by considering gene expression data. The same is done for protein expression data. The data for both parts has been derived from the Cancer Genome Atlas (http://cancergenome.nih.gov/).
This pipeline is very specifically design for BRCA because we still try to test which clinical data we can use and which parameters could play a significant role. In a later step we ant to provide this pipeline in a more general version such that it can be used for other cancer types.

INPUT:
------------
In order to run the pipeline, the following folder structure must be created:

- /CNBM
	- /Input [should contain a geneToId.txt and a proteinToId.txt]
		- /Classification [should contain a file called Patients.txt and a file called Project_Regression_Matrix.txt]
		- /GeneExpression [should contain the different input files (gene expression)]
		- /Protein [should contain the different input files (protein expression)]
		- /Patient:
			- /GeneExpression [contains one patient.txt file (same format as input files but the first two lines are deleted)]
			- /Protein	[contains one patient.txt file (same format as input files but the first two lines are deleted)]
	- /IntermediateFiles
   		- /GeneExpression 
			- /Partial
   		- /Protein
			- /Partial
		- /Regression
   		- /SVM	
 	- /Output
 		- /GeneExpression
 			- /Partial
 		- /Protein
 			- /Partial
 		- /Regression
 		- /SVM
 	- /Pipeline [contains the IntelliJ project and a jar file of the pipeline]

RUN:
------
When downloading everything from the github repository, the folder structure is already created. The folder 'Pipeline' contains the source code. The best thing is to open the project in IntelliJ. But we also provided a jar file. In order to run the program in IntelliJ, create a run configuration for 'MainClass' and define the following arguments:

 <working dir path> <threshold for genes> <threshold for proteins>

- **working dir path:** path of the program folder e.g. path/CNBM
- **threshold for genes:** threshold of the Pearson correlation for the gene expression networks
- **threshold for proteins:** threshold of the Pearson correlation for the protein networks

OUTPUT:
---------
As a result the user get the following main output:

- **/Output/Regression/RegressionOut.txt** <br />
[predicted cancer stages for the patients of interest from the MLR, the prediction error and the details of the model] 
- **/Output/SVM/SVMOut.txt** 		     
[predicted cancer stages for the patients of interest from the SVM and the prediction error]
- **/Output/GeneExpression/*.txt** <br />
[three biggest modules of the gene expression network based on Pearson correlation]
- **/Output/GeneExpression/Partial/*.txt** <br />
[three biggest modules of the gene expression network based on Patial correlation]
- **/Output/Protein/*.txt** <br />
[three biggest modules of the protein network based on Pearson correlation]
- **/Output/Protein/Partial/*.txt** <br />
[three biggest modules of the protein network based on Pearson correlation]

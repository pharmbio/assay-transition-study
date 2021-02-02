# Assay Transition study
Code used for running the experiments of the Assay transition study. The conformal prediction (CP) is handled by the Java library/CLI [CPSign](https://arosbio.com/) licensed by Aros Bio. 

## Requirements
There are two things required to compile and run the code on your own dataset;

- A license. An Academic license can be aquired by contacting [Aros Bio](https://arosbio.com/) using the email on the website. The license should be put in the location `java-code/src/resources` and called `cpsign.license` or make approprate changes to the file `java-code/src/utils/Utils.jar`
- The CPSign JAR file, which can be downloaded from the [CPSign download page](https://arosbio.com/cpsign/download/), the study used the version **1.5.0-beta-4**. 

## Building the runnable JAR
Once the two requirements above have been sorted, the runnable JAR can be packaged using the ant build script by simply running `ant` in the java-code folder (assuming you have Ant installed and set up correctly on your machine). 

## Running experiments
An example sbash-file of running a single sampling strategy and all combinations of A1 and A2 sizes is supplied in `bash-scripts/run.hERG.classification.sh`. Note that the Java code can run a single A1 size and the loop over different A1 sizes is performed in bash - while the loop over different A2 sizes is handled by the Java code. Appropriate changes can be done to increase / decrese the number of parallel jobs.   

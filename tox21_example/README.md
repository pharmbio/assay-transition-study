# Tox21 Example 
Here is an example of using the **SR-ARE** endpoint of the publicly available data sets supplied in the [Tox21 data challenge](https://tripod.nih.gov/tox21/challenge/data.jsp#). This data does not have the same background as the data used in our study, as the experimental setups were not replaced at any time, but an investigation of the [calibration of the models indicate an assay drift](https://www.researchsquare.com/article/rs-220364/v1) so it could serve as a proxy for a similar problem that we are addressing. Further note that this example experiment is simplified, with a single seed used, a single *k*-fold cross-validation and with no parameter-tuning. The sole purpose is to validate our findings in the paper and to supply the foundation for someone else to run their own tests using our work.

## Requirements to replicate
- A CPSign license, which can be acquired using the info in the main [README](../README.md).
- The runnable JAR of the project, again outlined in the main [README](../README.md).

## Basic pipeline
1. Data files were downloaded from [Tox21 data challenge website](https://tripod.nih.gov/tox21/challenge/data.jsp#) and saved in [resources](resources) folder.
2. The original dataset (`sr-are.smiles.gz`) was used as `old assay` and the scoring data was used as `new assay` in the terminology of the paper. The python script [get_score_dataset](get_score_dataset.py) was used to merge the files for the scoring dataset, the result saved in `tox21_score.smiles.gz`.
3. The smiles datasets were used to compute descriptors using the CPSign software and converted them to LIBSVM formatted files which are required by the runnable JAR. The code needed to do this step and handling duplicate records can be found in [PreprocessDatasets.java](src/code/PreprocessDatasets.java).
4. The experiments are then runned using the bash script [run-exp.sh](run-exp.sh), which outputs some CSV files.
5. R was then used for generating calibration plots and get the efficiency for each sampling strategy. 

## Results

[Calibration plot](run_output/calibration_plot.pdf) for all sampling strategies.

Efficiency for all sampling strategies:
| Strategy | Observed Fuzziness  |
| ---------- |------------------------:|
| CCP<sub>new</sub> | 0.342 |
| CCP<sub>AT</sub> | 0.285 |
| ICP<sub>old</sub><sup>new</sup> | 0.282 |
| CCP<sub>pool</sub> | 0.241 |
| CCP<sub>old</sub> | 0.253 |
| CCP<sub>AT2</sub> | 0.267 |

The calibration of the strategies seems to match well with the results found in the paper, with CCP<sub>new</sub>, CCP<sub>AT</sub> and ICP<sub>old</sub><sup>new</sup> being strictly well-calibrated. The remaining strategies are slightly below the desired accuracy for any given confidence. Perhaps due to higher agreement between the "old" and "new" assay data or classification being comparatively easier to model, the drift from being well-calibrated is less pronounced than most of the plots in the paper.  

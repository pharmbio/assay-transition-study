#!/bin/bash -l

A1_path="resources/train.svm.gz"
A2_path="resources/score.svm.gz"
JAR="../java-code/build/assay-transition-0.1.5.jar"
BASE_LOG_DIR="run_outputs/logs"
BASE_RES_DIR="run_outputs"
SEED=123456789

mkdir -p $BASE_RES_DIR
mkdir -p $BASE_LOG_DIR

# run the CV for each strategy
for STRATEGY in 1 2 3 4 5 6
do
    
    
    LOG="${BASE_LOG_DIR}/log.strat=${STRATEGY}.txt"
    
    RESULT_FILE="${BASE_RES_DIR}/strat=${STRATEGY}.csv"
    
    java -Xmx4g -cp $JAR evaluateDatasets.ClassificationCLI \
        -st $STRATEGY \
        --output $RESULT_FILE \
        --new-assay $A2_path \
        --old-assays $A1_path \
        --ccp 10 \
        --a2-size -1 \
        --a1-size -1 \
        --seed $SEED >> $LOG
	
done

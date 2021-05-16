#!/bin/bash -l

ENDPOINT="NR-AhR"
A1_path="resources/train.$ENDPOINT.svm.gz"
A2_path="resources/score.$ENDPOINT.svm.gz"
JAR="../java-code/build/assay-transition-0.1.5.jar"
BASE_LOG_DIR="run_outputs_$ENDPOINT/logs"
BASE_RES_DIR="run_outputs_$ENDPOINT"
SEED=123456789

mkdir -p $BASE_RES_DIR
mkdir -p $BASE_LOG_DIR

# run the CV for each strategy
for STRATEGY in 1 2 3 4 5 6
do
    
    
    LOG="${BASE_LOG_DIR}/log.strat=${STRATEGY}.$ENDPOINT.txt"
    
    RESULT_FILE="${BASE_RES_DIR}/strat=${STRATEGY}.$ENDPOINT.csv"
    
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

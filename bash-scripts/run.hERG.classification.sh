#!/bin/bash -l
#SBATCH -A XXXX
#SBATCH -p core -n 1
#SBATCH -t 3-10:00:00
#SBATCH -J hERG-class

A1_path=<A1 dataset path>
A2_path=<A2 dataset path>
JAR=<path to JAR file>

# Need to specify a sampling strategy
if [ $# -lt 1 ]; then
    echo "No strategy given - failing"
    exit 1
fi
STRATEGY=$1

# Set the sizes used for A1 and A2 datasets
A1_SIZE=( 1000 5000 25000 -1 )
A2_SIZES="50,250,1000,2500,-1"
# Strategy 5 only takes the A1 dataset, do not repeat it!
if [[ $STRATEGY == "5" ]]; then
    A2_SIZES="0"
fi

SEEDS=( 7669647485798415221 3854139472581350158 6430110629640930644 653941209355885045 755699907531858849 4829922979833292222 7158091650219667481 4859921438204898048 4251058430355052653 6271033246893671212 )
BASE_LOG_DIR="herg_classification/logs"
BASE_RES_DIR="herg_classification/results"

mkdir -p $BASE_RES_DIR
mkdir -p $BASE_LOG_DIR

# run the CV for each A1 size and SEED
for a1 in "${A1_SIZE[@]}"
do
    
    for seed in "${SEEDS[@]}"
    do
    
    LOG_STRAT_DIR=$BASE_LOG_DIR"/strat"$STRATEGY
    mkdir -p $LOG_STRAT_DIR
    
    LOG="${LOG_STRAT_DIR}/log.strat=${STRATEGY}.a1=${a1}.seed=${seed}.txt"
    
    RESULT_FILE="${BASE_RES_DIR}/strat=${STRATEGY}.a1=${a1}.seed=${seed}.csv"
    
    java -Xmx4g -cp $JAR evaluateDatasets.ClassificationCLI \
        -st $STRATEGY \
        --output $RESULT_FILE \
        --new-assay $A2_path \
        --old-assays $A1_path \
        --ccp 10 \
        --a2-size $A2_SIZES \
        --a1-size $a1 \
        --seed $seed >> $LOG
    done
done

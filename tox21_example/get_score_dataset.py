import pandas as pd

score_ids_df = pd.read_csv("resources/tox21_10k_challenge_score.smiles", delimiter='\t')
score_values_df = pd.read_csv("resources/tox21_10k_challenge_score.txt",delimiter='\t')

# Merge using the "Sample ID"
full_df = score_ids_df.merge(score_values_df, on='Sample ID')

full_df.to_csv("resources/tox21_score.smiles.gz", index=False, sep='\t')

# %%
import os
try:
	os.chdir(os.path.join(os.getcwd(), '/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.nlp/src/main/python'))
	print(os.getcwd())
except:
	pass

# %%
import sys
import tensorflow as tf
print(tf.__version__)
print(tf.keras.__version__)
sys.path.append('./tflow/ner/')
sys.path.append('./tflow/lib/ner/')
print(sys.path)
import create_models

# %%
if __name__ == "__main__":
	# %%
	# Create Graph

	CORPUS_PATH="/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/"
	MODEL_PATH=CORPUS_PATH + "models/"
	BERT_MODEL_PATH=MODEL_PATH + 'tensorflow/'

	# create_graph(BERT_PATH, True, 16, 1024, 128)
	create_models.create_graph(BERT_MODEL_PATH, 16, 1024, 128, 128)
	create_models.create_graph(BERT_MODEL_PATH, 16,  768, 128, 128)
	create_models.create_graph(BERT_MODEL_PATH, 14, 1024,  62, 128)

# %%
import os
try:
	os.chdir(os.path.join(os.getcwd(), 'D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.nlp/src/main/python'))
	print(os.getcwd())
except:
	pass

# %%
import sys
import tensorflow as tf
print("Tensorflow: " + tf.__version__)
print("Keras: " + tf.keras.__version__)
sys.path.append('./tflow/ner/')
sys.path.append('./tflow/lib/ner/')
print(sys.path)

# %%
import create_models

# %%
if __name__ == '__main__':
	# Create Graph

	TF_PATH='D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow/'

	# create_graph(BERT_PATH, True, 16, 1024, 128)
	create_models.create_graph(TF_PATH, 16, 1024, 128, 128)
	create_models.create_graph(TF_PATH, 16,  768, 128, 128)
	create_models.create_graph(TF_PATH, 14, 1024,  62, 128)
	create_models.create_graph(TF_PATH, 7,   768,  87, 128)
	create_models.create_graph(TF_PATH, 7,   768,  88, 128)


# %%

# To add a new cell, type '# %%'
# To add a new markdown cell, type '# %% [markdown]'
# %% Change working directory from the workspace root to the ipynb file location. Turn this addition off with the DataScience.changeDirOnImportExport setting
# ms-python.python added
import os
try:
	os.chdir(os.path.join(os.getcwd(), 'es.rcs.tfm.nlp/src/main/python/tfm'))
	print(os.getcwd())
except:
	pass
# %% [markdown]
# Creates Tensorflow Graphs for spark-nlp DL Annotators and Models
# 

# %%
import os
try:
	os.chdir(os.path.join(os.getcwd(), '/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.nlp/src/main/python/tfm'))
	print(os.getcwd())
except:
	pass
    
import numpy as np
import os
import tensorflow as tf
import string
import random
import math
import sys
import shutil

from pathlib import Path

from ner_model import NerModel
from dataset_encoder import DatasetEncoder
from ner_model_saver import NerModelSaver

CORPUS_PATH="/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/"
MODEL_PATH=CORPUS_PATH + "models/"
BERT_MODEL_PATH=MODEL_PATH + 'tensorflow/'

# %% [markdown]
# 

# %%
use_contrib = False if os.name == 'nt' else True

name_prefix = 'blstm-noncontrib' if not use_contrib else 'blstm'


# %%
def create_graph(ntags, embeddings_dim, nchars, lstm_size = 128):
    #RCS if sys.version_info[0] != 3 or sys.version_info[1] >= 7:
    if sys.version_info[0] != 3 or sys.version_info[1] >= 9:
        print('Python 3.7 or above not supported by tensorflow')
        return
    #RCS if tf.__version__ != '1.12.0':
    if tf.__version__ != '1.13.2':
        print('Spark NLP is compiled with Tensorflo 1.12.0. Please use such version.')
        return

    tf.reset_default_graph()
    model_name = name_prefix+'_{}_{}_{}_{}'.format(ntags, embeddings_dim, lstm_size, nchars)
    with tf.Session() as session:
        ner = NerModel(session=None, use_contrib=use_contrib)
        ner.add_cnn_char_repr(nchars, 25, 30)
        ner.add_bilstm_char_repr(nchars, 25, 30)
        ner.add_pretrained_word_embeddings(embeddings_dim)
        ner.add_context_repr(ntags, lstm_size, 3)
        ner.add_inference_layer(True)
        ner.add_training_op(5)
        ner.init_variables()
        saver = tf.train.Saver()
        file_name = model_name + '.pb'
        tf.train.write_graph(ner.session.graph, BERT_MODEL_PATH, file_name, False)
        ner.close()
        session.close()

# %% [markdown]
# ### Attributes info
# - 1st attribute: max number of tags (Must be at least equal to the number of unique labels, including O if IOB)
# - 2nd attribute: embeddings dimension
# - 3rd attribute: max number of characters processed (Must be at least the largest possible amount of characters)
# - 4th attribute: LSTM Size (128)

# %%
create_graph(16, 768, 128)
#create_graph(16, 1024, 128)

#create_graph(80, 200, 125)
# create_graph(10, 200, 100)
# create_graph(10, 300, 100)
# create_graph(10, 768, 100)
# create_graph(10, 1024, 100)
# create_graph(25, 300, 100)


# %%
tf.__version__


# %%




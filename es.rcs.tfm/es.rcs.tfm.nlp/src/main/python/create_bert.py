# To add a new cell, type '# %%'
# To add a new markdown cell, type '# %% [markdown]'
# %% Change working directory from the workspace root to the ipynb file location. Turn this addition off with the DataScience.changeDirOnImportExport setting
# ms-python.python added
import os
try:
	os.chdir(os.path.join(os.getcwd(), 'D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.nlp/src/main/python'))
	print(os.getcwd())
except:
	pass
print(sys.path)

# %%
import sys
import os
import tensorflow as tf
import shutil

from pyspark.sql import SparkSession
from pyspark.ml import Pipeline

import time
import zipfile

# %%
print("Tensorflow: " + tf.__version__)
print("Keras: " + tf.keras.__version__)
sys.path.append('./tflow/ner/')
sys.path.append('./tflow/lib/ner/')

# %%
from sparknlp.annotator import *
from sparknlp.common import *
from sparknlp.base import *
from sparknlp.embeddings import *
import sparknlp 

print("SparkNLP: " + sparknlp.version())

# %%
from embeddings_resolver import BertEmbeddingsResolver
from ner_model_saver import NerModelSaver

# %%
CORPUS_PATH="/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/"
DATASET_PATH=CORPUS_PATH + "datasets/"
BERT_PATH=DATASET_PATH + 'bert/'
BIOBERT_PATH=DATASET_PATH + 'biobert/'

SPARKNLP_BERT_MODEL_PATH=CORPUS_PATH+ "models/bert"

# %%
spark = sparknlp.start()

print("Spark NLP version")
sparknlp.version()
print("Apache Spark version")
spark.version


# %%
def download_model(url, destination_bert_folder, name):
    import os
    from pathlib import Path
    import urllib.request
    import zipfile
    model_name = destination_bert_folder + name
    zip_file = model_name + ".zip"
    if not Path(zip_file).is_file():
        print("Downloading " + url + " to " + str(Path(zip_file).resolve()))
        urllib.request.urlretrieve(url, zip_file)
    if not Path(model_name).exists():
        print("Unziping " + str(Path(zip_file).resolve()) + " to " + str(Path(model_name).resolve()))
        zip_ref = zipfile.ZipFile(zip_file, 'r')
        zip_ref.extractall(destination_bert_folder)
        zip_ref.close()

'''
def get_service_token_ids(source_bert_folder):
    start_id = 0
    end_id = 0
    with open(os.path.join(source_bert_folder, "vocab.txt")) as f:
        for line, row in enumerate(f):
            row = row.strip()
            if row == '[CLS]':
                start_id = line
            if row == '[SEP]':
                end_id = line
    return (start_id, end_id)
'''


def create_model(source_bert_folder, export_dir, max_sentence_length = 128, batch_size = 32):

    from pathlib import Path

    # if not os.path.exists(dst_folder):
    #     os.makedirs(dst_folder)
    if not Path(source_bert_folder).exists():
        print("Vamos mal")
    print("Esto no va mal")
 
    tf.reset_default_graph()
    is_cased = 'uncased' not in source_bert_folder.lower()
    string = str(Path(source_bert_folder).resolve())
    print("source_bert_folder: {}".format(string))
    print("is_cased: {}".format(is_cased))
    print("lowercase: {}".format(not is_cased))

    resolver = BertEmbeddingsResolver(source_bert_folder, max_sentence_length, lowercase = not is_cased)
    saver = NerModelSaver(resolver, None)
    saver.save_models(export_dir)
    resolver.session.close()
    os.mkdir(os.path.join(export_dir,'/assets'))
    shutil.copyfile(os.path.join(source_bert_folder, 'vocab.txt'),
                    os.path.join(export_dir, 'assets/vocab.txt'))

    # RCS: Could not find meta graph def matching supplied tags: { serve }
    #builder = tf.saved_model.builder.SavedModelBuilder(path)
    #with tf.Session(graph=graph) as sess:
    #    builder.add_meta_graph_and_variables(sess, [tf.saved_model.SERVING])
    #builder.save()
    dim = resolver.config.hidden_size
    layers = resolver.config.num_hidden_layers
    print("Number of hidden units: {}".format(dim))
    print("Number of layers: {}".format(layers))
    
    model = BertEmbeddings.loadSavedModel(export_dir, spark).setInputCols(["sentence", "token"]).setOutputCol("embeddings").setMaxSentenceLength(max_sentence_length).setBatchSize(batch_size).setDimension(dim).setCaseSensitive(is_cased)
    
    return model


def download_and_convert(url, name, max_sentence_length = 128, batch_size = 32, destination_model_folder = SPARKNLP_BERT_MODEL_PATH):

    from pathlib import Path

    # if not os.path.exists(dst_folder):
    #     os.makedirs(dst_folder)
    if not Path(destination_model_folder).exists():
        os.makedirs(destination_model_folder)

    download_model(url, BERT_PATH, name)

    bert_name = BERT_PATH + name
    model = create_model(bert_name, bert_name + '_export_dir_tmp', max_sentence_length, batch_size)
    # Remove but it's possible to use this model
    shutil.rmtree(bert_name + '_export_dir_tmp')
    # shutil.rmtree(name)

    final_model_name = name + '_M-{}'.format(max_sentence_length) + '_B-{}'.format(batch_size)
    model.write().overwrite().save(os.path.join(destination_model_folder, final_model_name))
    print("SPARKNLP BERT model has been saved: {}".format(destination_model_folder+'/'+final_model_name))
    return model

# %%
def convert(name, max_sentence_length = 512, batch_size = 32, destination_model_folder = SPARKNLP_BERT_MODEL_PATH):

    model = create_model(BIOBERT_PATH + name, BERT_PATH + name + '_export_dir', max_sentence_length, batch_size)
    # Remove but it's possible to use this model
    shutil.rmtree(BERT_PATH + name + '_export_dir')

    final_model_name = name + '_M-{}'.format(max_sentence_length) + '_B-{}'.format(batch_size)
    model.write().overwrite().save(os.path.join(destination_model_folder, final_model_name))
    print("SPARKNLP BERT model has been saved: {}".format(destination_model_folder+'/'+final_model_name))

    return model

# %%
if __name__ == "__main__":

    # https://github.com/JohnSnowLabs/spark-nlp/commit/118ec1012c8a58f6459a2cddfdbe465ab6f3f3aa
    # https://github.com/JohnSnowLabs/spark-nlp/pull/753
    # BERTEmbedding funciona sobre los modelos de TF HUB
    # %% [markdown]
    # ## Find models and source code here https://github.com/google-research/bert 

    # %%
    # 1. Base uncased
    url = 'https://storage.googleapis.com/bert_models/2018_10_18/uncased_L-12_H-768_A-12.zip'
    name = 'uncased_L-12_H-768_A-12'
    download_and_convert(url, name, max_sentence_length = 512, batch_size = 32)


    # %%
    # 2. Large uncased
    url = 'https://storage.googleapis.com/bert_models/2018_10_18/uncased_L-24_H-1024_A-16.zip'
    name = 'uncased_L-24_H-1024_A-16'
    download_and_convert(url, name, max_sentence_length = 512, batch_size = 32)


    # %%
    # 3. Base cased
    url = 'https://storage.googleapis.com/bert_models/2018_10_18/cased_L-12_H-768_A-12.zip'
    name = 'cased_L-12_H-768_A-12'
    download_and_convert(url, name, max_sentence_length = 512, batch_size = 32)


    # %%
    # 4. Large cased
    url = 'https://storage.googleapis.com/bert_models/2018_10_18/cased_L-24_H-1024_A-16.zip'
    name = 'cased_L-24_H-1024_A-16'
    download_and_convert(url, name, max_sentence_length = 512, batch_size = 32)


    # %%
    # 5. Multilingual Cased (New, recommended)
    url = 'https://storage.googleapis.com/bert_models/2018_11_23/multi_cased_L-12_H-768_A-12.zip'
    name = 'multi_cased_L-12_H-768_A-12'
    download_and_convert(url, name, max_sentence_length = 512, batch_size = 32)


    # %%
    # 6. Large uncased
    url = 'https://storage.googleapis.com/bert_models/2019_05_30/wwm_uncased_L-24_H-1024_A-16.zip'
    name = 'wwm_uncased_L-24_H-1024_A-16'
    download_and_convert(url, name, max_sentence_length = 512, batch_size = 32)


    # %%
    # 7. Large cased
    url = 'https://storage.googleapis.com/bert_models/2019_05_30/wwm_cased_L-24_H-1024_A-16.zip'
    name = 'wwm_cased_L-24_H-1024_A-16'
    download_and_convert(url, name, max_sentence_length = 512, batch_size = 32)


    # %%
    print('All generated models are inside "models/" directory')

    # %% [markdown]
    # ## Find models in: 
    # GOOGLE: https://github.com/google-research/bert
    # 
    # BIOBERT: https://github.com/naver/biobert-pretrained/releases
    # 
    # Dependiendo del nombre ocurren varias cosas:
    # 
    # - Busca la cadena uncased en el nombre para establecer si el modelo es uncased
    # - embeddings_resolver.py requiere que el modelo se denomine internamente bert_model.ckpt

    # %%
    name = 'biobert_v1.1_pubmed'
    convert(name, max_sentence_length = 512, batch_size = 32)



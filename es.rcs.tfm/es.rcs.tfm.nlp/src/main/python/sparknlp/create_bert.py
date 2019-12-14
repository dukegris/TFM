# To add a new cell, type '# %%'
# To add a new markdown cell, type '# %% [markdown]'
# %% Change working directory from the workspace root to the ipynb file location. Turn this addition off with the DataScience.changeDirOnImportExport setting
# ms-python.python added
import os
try:
	os.chdir(os.path.join(os.getcwd(), 'es.rcs.tfm.nlp/src/main/python'))
	print(os.getcwd())
except:
	pass

# %%
import sys
import os
import tensorflow as tf
import shutil
sys.path.append('../')
from ner.embeddings_resolver import BertEmbeddingsResolver
from ner.ner_model_saver import NerModelSaver

from pyspark.sql import SparkSession
from pyspark.ml import Pipeline

# Manully add sparknlp developer library
sparknlp_path = '../../'
if sparknlp_path:
    sys.path.append(sparknlp_path)

from sparknlp.annotator import *
from sparknlp.common import *
from sparknlp.base import *
from sparknlp.embeddings import *
import sparknlp 

import time
import zipfile
#Setting location of resource Directory
resource_path= "../../../src/test/resources/"


# %%
spark = sparknlp.start()

print("Spark NLP version")
sparknlp.version()
print("Apache Spark version")
spark.version


# %%
def download_model(url, folder):
    import os
    from pathlib import Path
    import urllib.request
    import zipfile
    zip_file = folder + ".zip"
    if not Path(zip_file).is_file():
        print("Downloading " + url)
        urllib.request.urlretrieve(url, zip_file)
    if not os.path.exists(folder):
        print("Unziping ")
        zip_ref = zipfile.ZipFile(zip_file, 'r')
        zip_ref.extractall("./")
        zip_ref.close()


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


def create_model(source_bert_folder, export_dir, max_sentence_length = 128, batch_size = 32):
    tf.reset_default_graph()
    is_cased = 'uncased' not in source_bert_folder.lower()
    print("source_bert_folder: {}".format(source_bert_folder))
    print("is_cased: {}".format(is_cased))
    print("lowercase: {}".format(not is_cased))
    resolver = BertEmbeddingsResolver(source_bert_folder, max_sentence_length, lowercase = not is_cased)
    saver = NerModelSaver(resolver, None)
    saver.save_models(export_dir)
    resolver.session.close()
    shutil.copyfile(os.path.join(source_bert_folder, 'vocab.txt'),
                    os.path.join(export_dir, 'vocab.txt'))

    dim = resolver.config.hidden_size
    layers = resolver.config.num_hidden_layers
    print("Number of hidden units: {}".format(dim))
    print("Number of layers: {}".format(layers))
    
    model = BertEmbeddings.loadFromPython(export_dir, spark)         .setInputCols(["sentence", "token"])         .setOutputCol("embeddings")         .setMaxSentenceLength(max_sentence_length)         .setBatchSize(batch_size)         .setDimension(dim)         .setCaseSensitive(is_cased)
    
    return model


def download_and_convert(url, name, max_sentence_length = 128, batch_size = 32, dst_folder = 'models'):
    if not os.path.exists(dst_folder):
        os.makedirs(dst_folder)
    download_model(url, name)
    model = create_model(name, name + 'export_dir', max_sentence_length, batch_size)
    # Remove but it's possible to use this model
    shutil.rmtree(name + 'export_dir')
    shutil.rmtree(name)
    final_model_name = name + '_M-{}'.format(max_sentence_length) + '_B-{}'.format(batch_size)
    model.write().overwrite().save(os.path.join(dst_folder, final_model_name))
    print("BERT model has been saved: {}".format(dst_folder+'/'+final_model_name))
    return model

# %% [markdown]
# ## Find models and source code here https://github.com/google-research/bert 

# %%
# 1. Base uncased
url = 'https://storage.googleapis.com/bert_models/2018_10_18/uncased_L-12_H-768_A-12.zip'
name = 'uncased_L-12_H-768_A-12'
download_and_convert(url, name, max_sentence_length = 128, batch_size = 32)


# %%
# 2. Large uncased
url = 'https://storage.googleapis.com/bert_models/2018_10_18/uncased_L-24_H-1024_A-16.zip'
name = 'uncased_L-24_H-1024_A-16'
download_and_convert(url, name, max_sentence_length = 128, batch_size = 32)


# %%
# 3. Base cased
url = 'https://storage.googleapis.com/bert_models/2018_10_18/cased_L-12_H-768_A-12.zip'
name = 'cased_L-12_H-768_A-12'
download_and_convert(url, name, max_sentence_length = 128, batch_size = 32)


# %%
# 4. Large cased
url = 'https://storage.googleapis.com/bert_models/2018_10_18/cased_L-24_H-1024_A-16.zip'
name = 'cased_L-24_H-1024_A-16'
download_and_convert(url, name, max_sentence_length = 128, batch_size = 32)


# %%
# 5. Multilingual Cased (New, recommended)
url = 'https://storage.googleapis.com/bert_models/2018_11_23/multi_cased_L-12_H-768_A-12.zip'
name = 'multi_cased_L-12_H-768_A-12'
download_and_convert(url, name, max_sentence_length = 128, batch_size = 32)


# %%
print('All generated models are inside "models/" directory')


# %%




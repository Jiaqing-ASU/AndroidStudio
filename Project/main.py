import socket

from time import ctime
import argparse, os, sys
import flask
from glob import glob
import numpy as np
from sklearn import svm, neighbors, linear_model, cluster
from sklearn.model_selection import StratifiedShuffleSplit, GroupKFold, cross_val_score, train_test_split
from sklearn.ensemble import RandomForestClassifier
from joblib import dump, load
from ae.model import Network
import torch
from tqdm import tqdm
from sklearn import metrics as skmet
from flask import jsonify
import json

metrics = {
	"accuracy": skmet.accuracy_score,
	"auc": skmet.roc_auc_score,
	"f1_score": skmet.f1_score,
	"hamming_loss": skmet.hamming_loss,
	"jaccard_score": skmet.jaccard_score,
	"matthews_corrcoef": skmet.matthews_corrcoef,
	"precision_score": skmet.precision_score,
	"recall_score": skmet.recall_score,
	"true_negative_rate": lambda yt, yp: skmet.confusion_matrix(yt, yp)[0][0] / np.float32((yp==0).shape[0]),
	"false_negative_rate": lambda yt, yp: skmet.confusion_matrix(yt, yp)[1][0] / np.float32((yp==1).shape[0]),
	"true_positive_rate": lambda yt, yp: skmet.confusion_matrix(yt, yp)[1][1] / np.float32((yp==1).shape[0]),
	"false_positive_rate": lambda yt, yp: skmet.confusion_matrix(yt, yp)[0][1] / np.float32((yp==0).shape[0]),
	"half_total_error_rate": lambda yt, yp: (metrics["false_positive_rate"](yt, yp) + metrics["false_negative_rate"](yt, yp))/2,
}
def calc_metrics(yt, yp):
	ret = dict()
	for m in metrics.keys():
		ret[m] = metrics[m](yt, yp)
		
	return ret


def predict_clf(data, label, clf):
	clf = load(f'models/{clf}.joblib')
	pred = clf.predict(data)

	if data.shape[0] == 1:
		return pred

	return pred, calc_metrics(label, pred)


def predict_cluster(data, label):
	clf = load(f'models/dbscan_knn.joblib')
	clf2 = load(f'models/dbscan_rfc.joblib')
	pred = clf2.predict(clf.predict(data)[:, np.newaxis])

	return calc_metrics(label, pred)


def predict_ae(data, label):
	parser = argparse.ArgumentParser()
	args = parser.parse_args()
	args.embedding_size = 8

	device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

	dataset = torch.utils.data.TensorDataset(torch.Tensor(data), torch.Tensor(label))
	loader = torch.utils.data.DataLoader(dataset, batch_size=1, shuffle=False, num_workers=2)

	net = Network(args).to(device)
	net.load_state_dict(torch.load("models/ae.pth", map_location=lambda storage, loc: storage))
	net.eval()

	with torch.no_grad():
		encX = []
		for i, d in tqdm(enumerate(loader, 0), total=data.shape[0]):
			# get the inputs; data is a list of [inputs, labels]
			inputs, labels = d
			inputs = inputs.to(device)
			labels = labels.to(device)

			# forward + backward + optimize
			emb = net.encode(inputs)
			encX.append(emb.cpu().numpy())

		encX = np.array(encX).squeeze()
		clf = load(f'models/ae_knn.joblib')
		pred = clf.predict(encX)

		# print("score:", clf.score(encX, label))

		return calc_metrics(label, pred)


def main():
	print( predict_clf(tX, ty, "svm") )
	print( predict_cluster(tX, ty) )
	print( predict_ae(tX, ty) )


def load_data(split, f):
	try:
		X = np.load(f"features/{split}X_{f}.npy")
		y = np.load(f"features/{split}Y_{f}.npy")
	except Exception as e:
		X, y = features[f](data[split])
		np.save(f"features/{split}X_{f}.npy", X)
		np.save(f"features/{split}Y_{f}.npy", y)

	return X, y
def feature(row_id):
	import extract_features as feat

	data = {
		"test": np.load("data/testGYX.npy"),
	}

	features = {
		"dwt": feat.extract_dwt,
		"fft": feat.extract_fft,
		"peaks": feat.extract_peaks,
		"svd": feat.extract_svd,
		"cnn": feat.extract_cnn,
		"rnn": feat.extract_rnn,
	}

	for f in features.keys():
		features[f](data["test"][int(row_id):int(row_id)+1])

	return f"Feature extracted successfully for Row id: {row_id}!"

def inference_all():

	import extract_features as feat
	print('start')
	data = {
		"test": np.load("data/testGYX.npy"),
	}

	features = {
		"dwt": feat.extract_dwt,
		"fft": feat.extract_fft,
		"peaks": feat.extract_peaks,
		"svd": feat.extract_svd,
		"cnn": feat.extract_cnn,
		"rnn": feat.extract_rnn,
	}


	models = ["svm", "nn", "lr"]
	res = {}

	for f in features.keys():
		tX, ty = load_data("test", f)
		for m in models:
			fmid = f"{m}_{f}"
			l = ty.tolist()
			p, met = predict_clf(tX, ty, fmid)
			res[fmid] = {
				# "pred": p.ravel().tolist(),
				# "gt": l,
				"metrics": met
			}
		

	return res

def inference(row_id):

	row_id = int(row_id)

	import extract_features as feat

	data = {
		"test": np.load("data/testGYX.npy"),
	}

	features = {
		"dwt": feat.extract_dwt,
		"fft": feat.extract_fft,
		"peaks": feat.extract_peaks,
		"svd": feat.extract_svd,
		"cnn": feat.extract_cnn,
		"rnn": feat.extract_rnn,
	}


	models = ["svm", "nn", "lr"]
	res = {}

	for f in features.keys():
		tX, ty = load_data("test", f)
		for m in models:
			fmid = f"{m}_{f}"
			l = ty[row_id:row_id+1].ravel().tolist()
			p = predict_clf(tX[row_id:row_id+1], ty[row_id:row_id+1], fmid)
			res[fmid] = {
				"pred": p.ravel().tolist(),
				"gt": l
			}
		

	return res
HOST,PORT='',8088
BUFFER_SIZE=1024
rowid=0
ADDR=("localhost",PORT)
#dest = gethostbyname("localhost")
#print(dest)

tcpServerSocket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)

tcpServerSocket.bind(ADDR)

tcpServerSocket.listen(6)
print('wait for connection')

while True:

    tcpClientSocket,addr = tcpServerSocket.accept()
    print('connect to ：',addr)

    while True:
          #decode() bytes --> str
        data = tcpClientSocket.recv(BUFFER_SIZE).decode()
        rowid = data
        if not data:
            break
        print('rowid =',rowid)
        print('version:1.0.8')
        # if(rowid =='0'):
        # 	result = inference_all()
        # 	Jresult = json.dumps(result)
        # 	print(Jresult)
        # 	tcpClientSocket.send(Jresult.encode())
        # 	tcpClientSocket.close()
        # else:
        result = inference(rowid)
        Jresult = json.dumps(result)
        #print(Jresult)
        tcpClientSocket.send(Jresult.encode())	
        tcpClientSocket.close()

    
#tcpServerSocket.close()

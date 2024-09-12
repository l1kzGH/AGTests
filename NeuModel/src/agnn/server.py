import concurrent.futures
import re
import time
import requests

import os
import subprocess
import sys

from flask import Flask, request, jsonify

import train

app = Flask(__name__)


@app.route('/')
def hi():
    return 'Hello'


@app.route('/gnrt/deprecated/v1', methods=['POST'])
def generate():
    data = request.json
    print(data)
    return jsonify(data)


@app.route('/gnrt/v1', methods=['POST'])
def generate2():
    data = request.json
    if isinstance(data, dict) and len(data) == 1:
        print("get a method")
        method = data.get('method')
        if method:
            # result = transform(method)
            result = train.predict(method)
            print(result)
            return jsonify(result)
    elif isinstance(data, list):
        print("get an array of methods")
        with concurrent.futures.ThreadPoolExecutor() as executor:
            # futures = [executor.submit(transform, element) for element in data]
            futures = [executor.submit(train.predict, element) for element in data]
            result_arr = [future.result() for future in concurrent.futures.as_completed(futures)]
        print(result_arr)
        return jsonify(result_arr)
    return jsonify(data), 400


def transform(method):
    return {"method": method}


if __name__ == '__main__':
    app.run(port=8000)

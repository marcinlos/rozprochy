#!/bin/bash

export PYTHONPATH=${ICE_HOME}/python:src:generated

CONF=conf/client.config 
FILE=src/rozprochy/lab4/bank/client.py

python ${FILE} --Ice.Config=${CONF}

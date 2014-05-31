#!/bin/bash

CONF=conf/client.config

export PYTHONPATH=${ICE_HOME}/python:client/generated:client/src
python client/src/main.py --Ice.Config=${CONF}

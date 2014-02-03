#!/bin/sh
mkdir mongo
mkdir mongo/data
mongod -dbpath mongo/data --smallfiles
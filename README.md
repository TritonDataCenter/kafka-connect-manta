[![Build Status](https://travis-ci.org/joyent/hadoop-manta.svg?branch=master)](https://travis-ci.org/joyent/hadoop-manta)

# Kafka Connect Sink Connector for Manta

## Introduction


This project is licensed under the [Apache 2.0 license](LICENSE.txt).

## Run Requirements
 * Java 8
 * A running instance of the Manta object store ([available on the public cloud](https://www.joyent.com/object-storage))

## Build Requirements
 * Java 8
 * Maven 3.0+
 
## Configuration

You will need to have the public/private keys needed to access Manta on the machine
in which Hadoop is running. It is often best to verify that these keys are setup
correctly using the [Node.js Manta CLI](https://www.npmjs.com/package/manta).

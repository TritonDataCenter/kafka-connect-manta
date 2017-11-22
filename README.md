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



## Test

You need to have access to running Zookeeper and Kafka.  Following sections are the steps for how to setup 1 node Zookeeper and Kafka on your localhost.


### Build kafka-connect-manta

Run maven to build the package:

```
$ mvn package
```

### Zookeeper setup

1. Download [Zookeeper](https://zookeeper.apache.org/releases.html), untar it. (e.g. `zookeeper-3.4.10.tar.gz`)
2. Create zookeeper configuration by copying `zookeeper-3.4.10/conf/zoo_sample.cfg` to `zookeeper-3.4.10/conf/zoo.cfg`.  For the experimental purpose, you don't need to change any configuration.
2. `cd` to zookeeper directory (e.g. `zookeeper-3.4.10/`),
3. Run `bin/zkServer.sh start`.   Or, you could run `bin/zkServer.sh start-foreground` if you want to start Zookeeper in foreground.

### Kafka setup

1. Read [here](https://kafka.apache.org/quickstart), as it describe how to setup the Kafka, and creating a topic.  In short, once downloaded and untared it:

        $ bin/kafka-server-start.sh config/server.property
        
        # In another terminal, create a topic called "test".
        $ bin/kafka-topic.sh --create --zookeeper locahost:2181 --replication-factor 1 --partitions 1 --topic test

### Kafka Connect

The details are describe in [Kafka Connect User Guide](https://kafka.apache.org/documentation/#connect_user).  We are going to use Kafka Connect distributed mode here.

1. In the `misc` directory of this package, update `plugin.path` value in `kafka-connect.properties` file.   It should be the absolute pathname of the `kafka-connect-manta/target`.

2. Return to the Kafka base directory, and run `bin/connect-distributed.sh DIRECTORY-OF-THIS-PACKAGE/misc/kafka-connect.properties`.

Now, the Connect is running and listening on `localhost:8083`,  you could verify it by:

```
$ curl http://localhost:8083
{"version":"1.1.0-SNAPSHOT","commit":"f29203d022f11076"}
```

### Run Kafka-Connect-Manta connector

You could use `curl(1)` to manage Kafka connector as described in the Kafka document [here](https://kafka.apache.org/documentation/#connect_rest), or use the small *bash* functions in `kafka-connect-manta/misc/conn-common`.  Here, we're going to use *bash* functions.

1. Return to the `kafka-connect-manta/misc` directory.
2. Load the *bash* functions in your *bash* session, by `source conn-common`.
3. Run `conn-list` to verify it:

```
$ source conn-common
$ conn-list
[]
```

5. copy `kafka-connect-manta/misc/connector.json.example` to `connector.json`, and update it providing your Manta account information. (esp. update `manta.url`, `manta.key_path`, `manta.key_id`, and `manta.user`.)

6. Run `conn-create connector.json` to register the connector to Kafka Connect.
6. Verify the registration by:

```
$ conn-list
[
  "manta-sink"
]
```

### Produce some data

1. Go back to the Kafka `bin/` directory.

2. Run following to generate some load to the Kafka `test` topic:

```
$ seq 1000000 | ./kafka-console-producer.sh --topic test --broker-list localhost:9092
```

### Check for Manta objects

By default, Kafka topic `XXX` will have Manta directory, `~~/stor/kafka/XXX`.

1. Check your Manta directory.  Note that depending on how you configured the topic, and the producer, you may see one or more directories here:

```
$ mls -l ~~/stor/kafka/test
01/
03/
```

2. Check the actual file that Kafka-Connect-Manta generated:

```
$ mls -l ~~/stor/kafka/test/01/
-rwxr-xr-x 1 csk       4458643 Nov 22 11:51 2017-11-22-19-51-34-00000000000000038684.msg.gz
```





   
   


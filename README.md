# Kafka Connect Sink Connector for Manta

## Introduction


This project is licensed under the [Apache 2.0 license](LICENSE.txt).

## Run Requirements
 * Java 8
 * A running instance of the Manta object store 
   ([available on the public cloud](https://www.joyent.com/object-storage))

## Build Requirements
 * Java 8
 * Maven 3.0+
 
## Configuration

You will need to have the public/private keys needed to access Manta on the 
machine in which Kafka is running. It is often best to verify that these keys 
are setup correctly using the 
[Node.js Manta CLI](https://www.npmjs.com/package/manta).



## Test

You need to have access to a running Zookeeper and Kafka. The following sections 
describe the steps for how to setup 1 node Zookeeper and Kafka on your local 
system.


### Build kafka-connect-manta

Run maven to build the package:

```
$ mvn package
```

### Zookeeper setup

1. Download [Zookeeper](https://zookeeper.apache.org/releases.html), untar it. 
   (e.g. `zookeeper-3.4.10.tar.gz`)
2. Create a zookeeper configuration by copying 
   `zookeeper-3.4.10/conf/zoo_sample.cfg` to `zookeeper-3.4.10/conf/zoo.cfg`.
   For our experimental purposes, you won't need to change any configuration.
2. `cd` to zookeeper directory (e.g. `zookeeper-3.4.10/`),
3. Run `bin/zkServer.sh start`. Alternatively, you could run 
   `bin/zkServer.sh start-foreground` if you want to start Zookeeper in 
   foreground.

### Kafka setup

1. Read the documentation [here](https://kafka.apache.org/quickstart) to learn 
   how to setup Kafka and how to create a topic. In short, once you have 
   downloaded and untared it:
```
 $ bin/kafka-server-start.sh config/server.property
        
 # In another terminal, create a topic called "test".
 $ bin/kafka-topic.sh --create --zookeeper locahost:2181 --replication-factor 1 --partitions 1 --topic test
```

### Kafka Connect

The [Kafka Connect User Guide](https://kafka.apache.org/documentation/#connect_user)
contains the details of setting up Kafka connect. Please refer to it for more
information.
 
For our example, we are going to use Kafka Connect in distributed mode.

1. In the [misc](misc) directory of this respository, update the `plugin.path` 
   value in the `kafka-connect.properties` file. It should be the absolute 
   pathname of the `kafka-connect-manta/target`. Try:
```

```

2. Return to the Kafka base directory, and run 
   `bin/connect-distributed.sh DIRECTORY-OF-THIS-PACKAGE/misc/kafka-connect.properties`.

Now, the Connect is running and listening on `localhost:8083`, you can verify 
by:

```
$ curl http://localhost:8083
{"version":"1.1.0-SNAPSHOT","commit":"f29203d022f11076"}
```

### Run Kafka-Connect-Manta connector

You can use the `curl(1)` command to manage the Kafka connector as described 
[here](https://kafka.apache.org/documentation/#connect_rest), or use the small 
*bash* functions in `kafka-connect-manta/misc/conn-common`. Below, we are going 
to use the *bash* functions.

1. Return to the `kafka-connect-manta/misc` directory.
2. Load the *bash* functions in your *bash* session, by `source conn-common`.
3. Run `conn-list` to verify it:

```
$ source conn-common
$ conn-list
[]
```

5. Copy `kafka-connect-manta/misc/connector.json.example` to `connector.json`, 
   and update it providing your Manta account information. (esp. update 
   `manta.url`, `manta.key_path`, `manta.key_id`, and `manta.user`.)

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

2. Run the following to generate some load to the Kafka `test` topic:

```
$ seq 1000000 | ./kafka-console-producer.sh --topic test --broker-list localhost:9092
```

### Check for Manta objects

By default, Kafka topic `XXX` will have Manta directory, `~~/stor/kafka/XXX`.

1. Check your Manta directory. Note that depending on how you configured the 
   topic, and the producer, you may see one or more directories here:

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

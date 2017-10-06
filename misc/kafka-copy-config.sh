#!/bin/bash

source $(dirname "$0")/zk-common


PROGRAM_NAME=$(basename "$0")

if [ "$#" -eq 0 ]; then
    echo "Usage: $PROGRAM_NAME ZK_INSTANCE... ::: KAFKA_INSTANCE..."
    exit 1
fi

ZK_HOSTS=()
ZK_HOSTS_ADDRESS=()

KAFKA_HOSTS=()
KAFKA_HOSTS_ADDRESS=()

while [ "$#" -gt 0 ]; do
    if [ "$1" = ":::" ]; then
        shift
        break
    fi
    ZK_HOSTS=("${ZK_HOSTS[@]}" "$1")
    shift
done

while [ "$#" -gt 0 ]; do
    KAFKA_HOSTS=("${KAFKA_HOSTS[@]}" "$1")
    shift
done


ZK_CONNECT_ENDPOINTS=""
for hostid in "${ZK_HOSTS[@]}"; do
    addr=$(triton instance ip "$hostid")
    ZK_CONNECT_ENDPOINTS="${ZK_CONNECT_ENDPOINTS},${addr}:2181"
done
ZK_CONNECT_ENDPOINTS="${ZK_CONNECT_ENDPOINTS#,}"

cfg=$(mktemp -t kafka-server.cfg.XXXXXXXXXX)
trap "rm -f $cfg" EXIT

broker_id=0
for hostid in "${KAFKA_HOSTS[@]}"; do
    addr=$(triton instance ip "$hostid")
    echo "$hostid = $addr"
    cat > "$cfg" <<EOF
# The id of the broker. This must be set to a unique integer for each broker.
broker.id=${broker_id}

# The number of threads that the server uses for receiving requests from the network and sending responses to the network
num.network.threads=3

# The number of threads that the server uses for processing requests, which may include disk I/O
num.io.threads=8

# The send buffer (SO_SNDBUF) used by the socket server
socket.send.buffer.bytes=102400

# The receive buffer (SO_RCVBUF) used by the socket server
socket.receive.buffer.bytes=102400

# The maximum size of a request that the socket server will accept (protection against OOM)
socket.request.max.bytes=104857600

# A comma seperated list of directories under which to store log files
log.dirs=/tmp/kafka-logs

# The default number of log partitions per topic. More partitions allow greater
# parallelism for consumption, but this will also result in more files across
# the brokers.
num.partitions=6

# The number of threads per data directory to be used for log recovery at startup and flushing at shutdown.
# This value is recommended to be increased for installations with data dirs located in RAID array.
num.recovery.threads.per.data.dir=1

############################# Internal Topic Settings  #############################
# The replication factor for the group metadata internal topics "__consumer_offsets" and "__transaction_state"
# For anything other than development testing, a value greater than 1 is recommended for to ensure availability such as 3.
offsets.topic.replication.factor=3
transaction.state.log.replication.factor=3
transaction.state.log.min.isr=3

# The minimum age of a log file to be eligible for deletion due to age
log.retention.hours=168

# A size-based retention policy for logs. Segments are pruned from the log as long as the remaining
# segments don't drop below log.retention.bytes. Functions independently of log.retention.hours.
#log.retention.bytes=1073741824

# The maximum size of a log segment file. When this size is reached a new log segment will be created.
log.segment.bytes=1073741824

# The interval at which log segments are checked to see if they can be deleted according
# to the retention policies
log.retention.check.interval.ms=300000

zookeeper.connect=$ZK_CONNECT_ENDPOINTS
zookeeper.connection.timeout.ms=6000

group.initial.rebalance.delay.ms=3
EOF

    echo "copying config to $hostid..."
    echo "copying config files..."
    scp "$cfg" "${addr}:${KAFKA_ROOT}/config/svr.property"
    echo "updating JMX configuration..."
    cat <<'EOF' | ssh "${addr}" "bash -s"
sed -i -e 's/"$KAFKA_JMX_OPTS .*"/"$KAFKA_JMX_OPTS -Dcom.sun.management.jmxremote.rmi.port=${JMX_PORT} -Dcom.sun.management.jmxremote.port=${JMX_PORT}"/g' /opt/kafka/current/bin/kafka-run-class.sh
EOF
    broker_id=$((broker_id + 1))
done

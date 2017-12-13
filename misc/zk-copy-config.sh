#!/bin/bash

source $(dirname "$0")/zk-common


PROGRAM_NAME=$(basename "$0")

if [ "$#" -eq 0 ]; then
    echo "Usage: $PROGRAM_NAME ZK_INSTANCE..."
    exit 1
fi

ZK_HOSTS=("$@")
ZK_HOSTS_ADDRESS=()

cfg=$(mktemp -t zoo.cfg.XXXXXXXXXX)
trap "rm -f $cfg" EXIT

cat > "$cfg" <<EOF
tickTime=2000
dataDir=/var/lib/zookeeper
clientPort=2181
initLimit=5
syncLimit=2
EOF

count=1
for hostid in "${ZK_HOSTS[@]}"; do
    addr=$(triton instance ip "$hostid")
    echo "server.${count}=${addr}:2888:3888" >> "$cfg"
    ZK_HOSTS_ADDRESS=("${ZK_HOSTS_ADDRESS[@]}" "$addr")

    ssh "$addr" "rm -rf /var/lib/zookeeper/*; echo $count > /var/lib/zookeeper/myid"
    
    count=$((count + 1))
done

cat "$cfg"

parallel scp "$cfg" "{}:$ZK_ROOT/conf/zoo.cfg" ::: "${ZK_HOSTS_ADDRESS[@]}"

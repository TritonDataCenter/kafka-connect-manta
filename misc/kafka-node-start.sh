#!/bin/bash

if [ -z "$ZK_ROOT" ]; then
    echo "error: paste zk-common together" 1>&2
    echo "error: e.g.  cat zk-common kafka-node-start.sh | ssh REMOTE bash -s" 1>&2
    exit 1
fi

tmux send-keys -t ${W_KAFKA} "cd \"$KAFKA_ROOT\"" C-m
tmux send-keys -t ${W_ZK} "export JMX_PORT=9999" C-m
tmux send-keys -t ${W_KAFKA} "./bin/kafka-server-start.sh config/svr.property" C-m


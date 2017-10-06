#!/bin/bash

if [ -z "$ZK_ROOT" ]; then
    echo "error: paste zk-common together" 1>&2
    echo "error: e.g.  cat zk-common zk-node-stop.sh | ssh REMOTE bash -s" 1>&2
    exit 1
fi

tmux send-keys -t ${W_ZK} C-c


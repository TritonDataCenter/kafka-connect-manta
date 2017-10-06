#!/bin/bash

if [ -z "$ZK_ROOT" ]; then
    echo "error: paste zk-common together" 1>&2
    echo "error: e.g.  cat zk-common kafka-node-install.sh | ssh REMOTE bash -s" 1>&2
    exit 1
fi

log "Installing utilities"

yum install -q -y tmux </dev/null
cp /usr/share/doc/tmux-*/examples/screen-keys.conf /etc/tmux.conf
sed -i -e 's/set -g prefix ^A/set -g prefix ^Z/g' /etc/tmux.conf
echo "set-option -g history-limit 8196" >> /etc/tmux.conf

yum install -q -y nmap-ncat </dev/null

export LK_YUM=/tmp/zk-installer.sys.$$
export LK_WGET=/tmp/zk-installer.wget-zk.$$

rm -f "$LK_YUM" "$LK_WGET"

mkdir -p "$KAFKA_PREFIX" "$ZK_DATA"
rm -f "$KAFKA_ROOT"

tmux new-session -d -s ${TSESSION}
#sleep 0.1
#tmux rename-window -t ${TSESSION}:0 updater

tmux new-window -t ${W_WGET}
#sleep 0.5
#tmux rename-window -t ${TSESSION}:1 wget

tmux new-window -t ${W_KAFKA}
#sleep 0.5
#tmux rename-window -t ${TSESSION}:2 zookeeper

sleep 1

tmux send-keys -t ${W_UPDATOR} "yum update -y; yum install -y java-1.8.0-openjdk; touch $LK_YUM" C-m
tmux send-keys -t ${W_WGET} "wget -O /tmp/kafka.tar.gz \"http://mirror.cogentco.com/pub/apache/kafka/${KAFKA_VER}/kafka${KAFKA_POSTFIX}-${KAFKA_VER}.tgz\"" C-m
tmux send-keys -t ${W_WGET} "tar -C \"$KAFKA_PREFIX\" -xzf /tmp/kafka.tar.gz" C-m
tmux send-keys -t ${W_WGET} "(cd \"$KAFKA_PREFIX\"; rm -f current; ln -s kafka${KAFKA_POSTFIX}-${KAFKA_VER} current; touch $LK_WGET)" C-m

sleep 1

tmux rename-window -t ${W_UPDATOR} updator
sleep 0.1
tmux rename-window -t ${W_WGET} wget
sleep 0.1
tmux rename-window -t ${W_KAFKA} kafka
sleep 0.1


# exec &>> "$LOG_DIR/setup-zookeeer-instances.log"
#exec 0<&-

# echo "updateing.."
# yum update -y </dev/null
# #yum install -y screen
# yum install -y tmux </dev/null

# echo "---"
# if ! grep ^escape >&/dev/null; then
#     echo "---sed"
#     sed -i -e $'s/^defscrollback [0-9]*/defscrollback 8196/g\n$ a\\\nescape ^za' /etc/screenrc
# fi

# echo "---wget"
# wget --no-verbose -O /tmp/zookeeper.tar.gz "http://apache.cs.utah.edu/zookeeper/zookeeper-${ZK_VER}/zookeeper-${ZK_VER}.tar.gz"
# 
# mkdir -p "$ZK_PREFIX"
# 
# echo "---"
# tar -C "$ZK_PREFIX" -xzf /tmp/zookeeper.tar.gz
# 
# (cd "$ZK_PREFIX"; rm -f current; ln -sf "zookeeper-${ZK_VER}" current)
# 
# echo "---"


wait_for_file "$LK_YUM" "Updating system"
wait_for_file "$LK_WGET" "Downloading kafka"

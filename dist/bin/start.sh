#!/bin/bash

SCRIPT_DIR="$(dirname $(readlink -f $0))"
CONF_DIR=${SCRIPT_DIR}/../conf
LIB_DIR=${SCRIPT_DIR}/../lib

APP_PORT=${PORT:-9000}

MAX_MEM=$(vmstat -s -S M | grep "total memory" | awk '{print $1}')

echo "The maximum memory available is ${MAX_MEM} MB"

if [ ${MAX_MEM} -gt 2048 ]; then
    MAX_MEM=$((MAX_MEM - 1024))
else
    MAX_MEM=$((MAX_MEM - 100))
fi

echo "Using ${MAX_MEM} MB to run the app"

source ${SCRIPT_DIR}/arq2 \
    -Dlogger.file=${CONF_DIR}/logback-prod.xml \
    -Dplay.evolutions.db.default.autoApply=true \
    -Dhttp.port=${APP_PORT} \
    -Dpidfile.path=/dev/null \
    -J-Xmx$((MAX_MEM))M \
    -J-XX:+UseG1GC \
    -J-XX:+UseStringDeduplication

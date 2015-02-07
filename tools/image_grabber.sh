#!/bin/bash
if [ $# -lt 2 ]; then
  echo Usage: `basename $0` targetdir studentlist 1>&2
  exit 1
fi
targetdir=$1
students=$2
while read line; do
  if [ -n "${line}" ]; then
    info=`grep "\b${line}\b" $students`
    netid=`echo "$info" | awk -F $'\t' '{print $1}'`
    url=`echo "$info" | awk -F $'\t' '{print $3}'`
    wget -c -O "${targetdir}${netid}.png" "$url"
    sleep 0.5
  fi
done

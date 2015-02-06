#!/bin/sh
dir="../res"
while read line; do
  if [ -n "${line}" ]; then
    info=`grep "$line" ../res/students.txt`
    netid=`echo "$info" | awk -F $'\t' '{print $1}'`
    url=`echo "$info" | awk -F $'\t' '{print $3}'`
    wget -c -O "${dir}/${netid}.png" "$url"
    sleep 0.5
  fi
done

#!/bin/bash

LOCK=lock
rm -f $LOCK
while [ ! -f $LOCK ]; do
	sleep 0.5
done

for i in `find tests | grep -v OFF | grep \.test\$ | grep $1 | sort`; do
	rm -f $i.RESULT
	DIR=`dirname $i`
	echo astral:set output $i.RESULT
#	grep . $i | sed "s%\(register.*\)(\(.*\..*\))%\1(\\\\\"$DIR/\2\\\\\")%g" | sed "s%\(prepare \)\(.*\)%\1\"\2\"%g" | sed "s/\(.*\)/astral:\1/g"
	echo astral:prepare $i
	echo astral:launch
	echo astral:join
	echo astral:reset
done

rm -f $LOCK

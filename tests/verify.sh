#!/bin/bash
TEST=$2
SOURCE=$1
REPORT=$2.REPORT
TMP=report.tmp

#head -n 1 $TEST | awk -F, '{for(i = 1 ; i <= NF ; i++) print $i;}'
#result=( `head -n 1 $TEST | awk -F, '{print $0;}'` )
headTest=( `head -n 1 $TEST  | sed 's/$//' | awk -F, '{for(i = 1 ; i <= NF ; i++) printf $i OFS; print NFS; }'` )
headSrc=( `head -n 1 $SOURCE  | sed 's/$//' | awk -F, '{for(i = 1 ; i <= NF ; i++) printf $i OFS; print NFS; }'` )

if [[ ! ${#headTest[@]} == ${#headSrc[@]} ]]; then
	echo "FAILED Entities does not share the same header count"
	exit 1;
fi
test2src=""

#for i in `jot ${#headTest[@]} 0`; do
for i in `seq 0 $(( ${#headTest[@]} - 1 ))`; do
	found=0
#for j in `jot ${#headTest[@]} 0`; do
for j in `seq 0 $(( ${#headTest[@]} - 1 ))`; do
	if [[ ${headTest[$i]} == ${headSrc[$j]} ]]; then
		found=1
		p=$(( $j + 1 ))
		if [[ $i == 0 ]]; then
			test2src=`echo \\$$p`
		else
			test2src=`echo $test2src,\\$$p`
		fi
		break
	fi
done

if [[ $found == 0 ]]; then
        echo "FAILED Could not find ${headTest[$i]} on source"
        exit 2;
fi
done

awk -F, "BEGIN {OFS=\",\"} NF>1{print $test2src}; NF==1 {print \$1}" $SOURCE > $TMP
diff -w -B --strip-trailing-cr $TEST $TMP > $REPORT
rm $TMP
if [[ `head $REPORT` == "" ]]; then
	echo SUCCESS
	rm $REPORT
	rm -f $TEST
	exit 0
else
	echo FAILED - see $REPORT for details
	exit 3
fi

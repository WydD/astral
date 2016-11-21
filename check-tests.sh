#!/bin/bash
export FELIX_HOME=~/dev/felix/
#export FELIX_HOME=H:/felix/
#"-javaagent:jip-1.2/profile/profile.jar" "-Dprofile.properties=jip-1.2/profile/profile.properties" 
felix() {
        if [[ $# > 0 ]]; then
                java -Dhttp.proxyHost=p-goodway -Dhttp.proxyPort=3128 -Dfelix.config.properties=file:$FELIX_HOME/conf/$1.properties -jar $FELIX_HOME/bin/felix.jar
        fi
        rm -rf felix-cache
}

filter="test"
if [[ $# != 0 ]]; then
    filter=$1;
fi
echo Running tests...
./test.sh $filter | felix astral-shell &> check-tests.log
echo Checking results...
state=0
total=0
for i in `find tests | grep \.test\$ | grep -v OFF | grep $filter`; do
	res=`tests/verify.sh $i.OUT $i.RESULT`
	if [[ $? != 0 ]]; then
	    echo $i: $res | sed 's%^tests/%%g' | sed 's%\.test:%:%g'
	    state=$(( $state + 1 ));
	fi
	total=$(( $total + 1 ));
done
succ=$(( $total - $state ))
echo -n "Functional tests results $succ/$total ($(( $succ * 100 / $total ))%): "
if [[ $total == $succ ]]; then
	echo SUCCESS
else
	echo FAILED
fi
exit $state

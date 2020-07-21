#!/usr/bin/env bash

# This script runs vocab and shape checks on all the primary domains,
# and fails if any one of them fails; this is better than stopping after
# the first failure, and masking other errors.

SCDIR=`dirname $0`
let Errors=0

for domain in core cm qm rm am
do
	echo "Running $SCDIR/check-$domain.sh"
    $SCDIR/check-$domain.sh
    let Errors=Errors+$?
    echo "Cumulative error status is $Errors"
done

exit $Errors
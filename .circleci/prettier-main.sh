#!/usr/bin/env bash

# This script runs prettier checks on all the primary domains,
# and fails if any one of them fails; this is better than stopping after
# the first failure, and masking other errors.

SPECDIR=`dirname $0`/specs
let Errors=0

for domain in core config qm rm query
do
	echo "Checking $SPECDIR/$domain"
    prettier --check $SPECDIR/$domain/
    let Errors=Errors+$?
    echo -e "Cumulative error status is $Errors\n\n===================================================\n\n"
done

exit $Errors

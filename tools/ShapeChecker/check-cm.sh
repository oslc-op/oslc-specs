#!/usr/bin/env bash

build/install/ShapeChecker/bin/ShapeChecker \
-x http://open-services.net/ns/core ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/cm ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-v ../../specs/core/core-vocab.ttl \
-v ../../specs/cm/change-mgt-vocab.ttl \
-s ../../specs/cm/change-mgt-shapes.ttl

#!/usr/bin/env bash

build/install/ShapeChecker/bin/ShapeChecker \
-C ${comment# Enforce check for ResourceShapeConstraints} \
-x http://open-services.net/ns/core ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/core\#.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/rm ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/rm\#.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-v ../../specs/rm/requirements-management-vocab.ttl \
-s ../../specs/rm/requirements-management-shapes.ttl \

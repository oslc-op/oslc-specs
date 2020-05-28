#!/usr/bin/env bash

build/install/ShapeChecker/bin/ShapeChecker \
-x http://open-services.net/ns/core ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/core\#.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/rm ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/rm\#.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x 'https?://purl.org/dc/terms.*' ${comment# to address dcterms content negotiation problem} \
-v ../../specs/rm/requirements-management-vocab.ttl \
-s ../../specs/rm/requirements-management-shapes.ttl \

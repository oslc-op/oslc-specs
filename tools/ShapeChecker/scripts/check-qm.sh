#!/usr/bin/env bash

build/install/ShapeChecker/bin/ShapeChecker \
-t Warning \
-C ${comment# Enforce check for ResourceShapeConstraints} \
-q undefinedShape ${comment# TODO remove; see https://github.com/oslc-op/oslc-specs/issues/432} \
-q unusedVocabulary \
-x http://open-services.net/ns/core ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/core\#.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/qm ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/qm\#.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://jazz.net/ns/acp ${comment# See https://github.com/oslc-op/oslc-specs/issues/332} \
-x http://jazz.net/ns/acp\#.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/332} \
-v ../../specs/qm/quality-management-vocab.ttl \
-s ../../specs/qm/quality-management-shapes.ttl

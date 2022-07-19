#!/usr/bin/env bash

build/install/ShapeChecker/bin/ShapeChecker \
-D \
-C ${comment# Enforce check for ResourceShapeConstraints} \
-q unusedVocabulary \
-q unusedTerm \
-x http://open-services.net/ns/core.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x http://open-services.net/ns/auto.\* ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-v ../../specs/config/config-vocab.ttl \
-s ../../specs/config/config-shapes.ttl

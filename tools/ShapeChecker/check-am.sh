#!/usr/bin/env bash

build/install/ShapeChecker/bin/ShapeChecker \
-x http://open-services.net/ns/core  \
-x http://open-services.net/ns/am  \
-x http://jazz.net/ns/dm/linktypes \
<<<<<<< HEAD
-x 'https?://purl.org/dc/terms.*' \
-v ../../specs/core/vocab/core-vocab.ttl \
-v ../../specs/am/architecture-management-vocab.ttl \
-s ../../specs/am/architecture-management-shapes.ttl
=======
-v ../../specs/core/core-vocab.ttl \
-v ../../specs/am/architecture-management-vocab.ttl \
-s ../../specs/am/architecture-management-shapes.ttl
>>>>>>> jra-core-cm-ps01-final-updates

#!/usr/bin/env bash
set -euo pipefail

build/install/ShapeChecker/bin/ShapeChecker \
-t Warning \
-C ${comment# Enforce check for ResourceShapeConstraints} \
-q EmbeddedWhitespace \
-q MissingPeriod \
-x 'http://open-services.net/ns/promcode' ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x 'http://open-services.net/ns/promcode#.*' ${comment# See https://github.com/oslc-op/oslc-specs/issues/40} \
-x 'http://purl.org/dc/terms/' ${comment# See https://github.com/oslc-op/oslc-specs/issues/320} \
-x 'http://purl.org/dc/terms/.*' ${comment# See https://github.com/oslc-op/oslc-specs/issues/320} \
-v /Users/ezandbe/git/a/oslc/websites/open-services/content/ns/promcode/promcode-vocab.ttl \
-s /Users/ezandbe/git/a/oslc/websites/open-services/content/ns/promcode/promcode-shapes.ttl

# -t Warning \
# -q undefinedShape ${comment# TODO remove; see https://github.com/oslc-op/oslc-specs/issues/432} \
# -q unusedVocabulary \

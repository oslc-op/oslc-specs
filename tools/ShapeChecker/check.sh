#!/usr/bin/env bash

build/install/ShapeChecker/bin/ShapeChecker -x http://open-services.net/ns/core \
-x http://open-services.net/ns/core\#.\* \
-v ../../specs/core/vocab/core-vocab.ttl \
-s ../../specs/core/shapes/AttachmentDescriptor-shape.ttl \
-s ../../specs/core/shapes/Comment-shape.ttl \
-s ../../specs/core/shapes/CommonProperties-shape.ttl \
-s ../../specs/core/shapes/Compact-shape.ttl \
-s ../../specs/core/shapes/CreationFactory-shape.ttl \
-s ../../specs/core/shapes/Dialog-shape.ttl \
-s ../../specs/core/shapes/Discussion-shape.ttl \
-s ../../specs/core/shapes/Error-shape.ttl \
-s ../../specs/core/shapes/ExtendedError-shape.ttl \
-s ../../specs/core/shapes/OAuthConfiguration-shape.ttl \
-s ../../specs/core/shapes/PrefixDefinition-shape.ttl \
-s ../../specs/core/shapes/Preview-shape.ttl \
-s ../../specs/core/shapes/Publisher-shape.ttl \
-s ../../specs/core/shapes/QueryCapability-shape.ttl \
-s ../../specs/core/shapes/ResourceShapes-shape.ttl \
-s ../../specs/core/shapes/ResponseInfo-shape.ttl \
-s ../../specs/core/shapes/Service-shape.ttl \
-s ../../specs/core/shapes/ServiceProvider-shape.ttl \
-s ../../specs/core/shapes/ServiceProviderCatalog-shape.ttl

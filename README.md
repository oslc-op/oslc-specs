# OSLC Open Project specifications

[![CircleCI](https://circleci.com/gh/oslc-op/oslc-specs.svg?style=svg)](https://circleci.com/gh/oslc-op/oslc-specs)
[![Discourse status](https://img.shields.io/discourse/https/meta.discourse.org/status.svg)](https://forum.open-services.net/c/oslc-op)

This repository is for managing the development of OSLC Open Project
specifications including Core 3.0, OSLC Query, Tracked Resource Sets,
Configuration Management, and all the various OSLC domain specifications.

## Getting started

In order to preview the specifications in your local workspace, you should run a local webserver
(it is inadvisable to disable the security protections against running AJAX requests on local filesystem).
With Python 3 installed:

    cd docs/
    python3 -m http.server 8000 --bind 127.0.0.1

Now you can browse all specs via http://localhost:8000/. For an even better experience, install Browsersync (`npm i -g browser-sync`) and run it the following way:

    cd docs/
    browser-sync start --server --directory

Now you can browse all specs via http://localhost:8000/. Anytime you save a spec file, every browser tab where this file is opened will be reloaded automatically.

## Contributions

Read [how to contribute to the OSLC Open Project](https://github.com/oslc-op/oslc-admin/blob/master/CONTRIBUTING.md).

## Licensing

Please see the
[LICENSE](https://github.com/oslc-op/oslc-admin/blob/master/LICENSE.md)
file for description of the license terms and OASIS policies applicable
to the OSLC Open Project work items.


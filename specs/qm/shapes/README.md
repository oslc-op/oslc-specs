## Generation instructions

With Apache Jena installed, to produce a single shape file, run the following from within this folder:

    riot -q --check --stop --formatted=turtle --base='https://open-services.net/ns/qm/shapes/ps01#' sh_test* > ../quality-management-shapes.ttl

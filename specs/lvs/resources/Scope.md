The OSLC-OP has expressed interest in a Link Validity Service (LVS) specification. This note summarizes some of the concepts that might be covered. We welcome your feedback and input.

A Link Validity Service would provide the ability for a client application to query the status of links that represent some semantic dependency. See [OSLC Core Version 3.0. Part 7: Vocabulary section 5.2](https://docs.oasis-open-projects.org/oslc-op/core/v3.0/os/core-vocab.html) Traceability and Impact type.

OSLC Core describes a “link” as an RDF assertion with a subject, predicate and object. A link may or may not represent a dependency between the subject and object. If a change in either the subject or object of the link could result in the assertion becoming invalid, then OSLC considers the link a dependency. OSLC does not specify the semantics of such a dependency, only that it can exist.

The impact of a change in a subject or object in a dependency could follow the link direction (all RDF predicates are directional), the opposite of the link direction, or both. OSLC Core uses oslc:impactType to allow servers to specify how changes in subjects and objects impact a dependency assertion.

A Link Validity Service would provide a way of getting links that are valid, suspect or invalid, or requesting whether a specific link/dependency assertion is suspect or invalid. It would also have a way of allowing a client to assert or update link validity, or to clear suspect or invalid links assuming the dependency has been managed somehow.

A Link Validity Service would also provide a means to define what constitutes a change in a subject or object in the context of a dependency relationship. This would typically involve specifying what properties of a resource constitute a change in the context of a dependency. The impacting property changes could be different for the same resources in different dependency relationships.

See also [Link Validity in ELM applications](https://www.ibm.com/docs/en/engineering-lifecycle-management-suite/lifecycle-management/7.0.3?topic=configurations-link-validity-in-elm) for an existing implementation of link validity concepts.

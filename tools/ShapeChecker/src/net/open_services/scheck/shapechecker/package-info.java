/**
 * A program to validate the consistency of a set of OSLC shapes and vocabularies.
 *
 * <h2>Vocabulary Checks</h2>
 * <p>
 * TODO this list is out of date.
 * The program performs the following internal checks on each vocabulary:
 * </p>
 * <ul>
 * <li>Every resource referenced in a vocabulary must be a fetchable RDF resource</li>
 * <li style="margin-top:5px;">Each vocabulary document has exactly one resource of type owl:Ontology</li>
 * <li style="margin-top:5px;">The ontology should have at least the following
 * properties:
 * <ul>
 * <li>{@code rdf:type} with a value of {@code owl:Ontology}</li>
 * <li>{@code dcterms:title}, with a literal string value</li>
 * <li>{@code dcterms:source}, referencing a resource ending '.ttl'</li>
 * <li>{@code rdfs:label}, with a literal string value</li>
 * </ul>
 * <li style="margin-top:5px;">The ontology may also have any of the following
 * properties:
 * <ul>
 * <li>{@code rdfs:seeAlso}, one or more references to any resource</li>
 * <li>{@code dcterms:description}, with a literal value of type string,  {@code rdf:HTML}, or {@code rdf:XMLLiteral}</li>
 * <li>{@code dcterms:license}, with a literal value of type string, {@code rdf:HTML}, or {@code rdf:XMLLiteral}</li>
 * <li>{@code dcterms:dateCopyrighted}, with a string value</li>
 * <li>{@code vann:preferredNamespacePrefix}, with a URI value
 * </ul>
 * </li>
 * <li style="margin-top:5px;">Each term within the vocabulary should use hash URIs,
 * with the ontology resource as the base</li>
 * <li style="margin-top:5px;">Each vocabulary term has at least the following properties:
 * <ul>
 * <li>{@code rdf:type} with a value of {@code rdfs:Class}, {@code rdfs:Property}, or {@code rdf:Resource}
 *   (a missing type is treated as type {@code rdf:Resource})</li>
 * <li>{@code rdfs:label}, with a literal string value</li>
 * <li>{@code rdfs:comment}, with a literal value of type string, {@code rdf:HTML}, or {@code rdf:XMLLiteral}</li>
 * <li>{@code rdfs:isDefinedBy}, with a value that is a reference to the ontology resource</li>
 * </ul>
 * <li style="margin-top:5px;">A term may also have any of the following properties:
 * <ul>
 * <li>{@code rdfs:seeAlso}, one or more references to any resource</li>
 * <li>{@code oslc:inverseLabel}, with a literal string value</li>
 * <li>{@code oslc:impactType}, with a value {@code oslc:UpstreamImpact}, {@code oslc:DownstreamImpact},
 * {@code oslc:SymmetricImpact}, or {@code oslc:NoImpact}</li>
 * <li>{@code vs:term_status}, with one of the string values 'stable', or 'archaic'</li>
 * </ul>
 * </li>
 * <li style="margin-top:5px;">Class terms may use {@code rdfs:subClassOf} and {@code owl:sameAs}</li>
 * <li style="margin-top:5px;">Property terms may use {@code rdfs:subPropertyOf}, {@code rdfs:range},
 *   {@code rdfs:domain}, and {@code owl:sameAs}</li>
 * <li style="margin-top:5px;">TBD: Rules for Individuals</li>
 * <li style="margin-top:5px;">TBD: Rules for Enumerations</li>
 * <li style="margin-top:5px;">The vocabulary document must not contain any
 * statements other than the ontology and term definitions covered above</li>
 * </ul>
 *
 * <h2>Shape Checks</h2>
 * <p>
 * The program performs the following internal checks on each shape:
 * </p>
 * <ul>
 * <li>Every resource referenced in a shape must be a fetchable RDF resource</li>
 * <li style="margin-top:5px;">Each shape resource should have the following properties:
 * <ul>
 * <li>{@code rdf:type} with a value of {@code oslc:ResourceShape}</li>
 * <li>{@code oslc:describes}, one or more references to a resource type</li>
 * <li>{@code dcterms:title}, with a literal value of type string,
 * {@code rdf:HTML}, or {@code rdf:XMLLiteral}</li>
 * <li>{@code dcterms:description}, optional, with a literal value of type
 * string, {@code rdf:HTML}, or {@code rdf:XMLLiteral}</li>
 * <li>{@code oslc:property}, one or more hash URIs relative to the shape base</li>
 * </ul>
 * </li>
 * <li>The {@code oslc:describes} value for each shape must be unique in the values from the source document</li>
 * <li style="margin-top:5px;">Each Property of the shape has the following properties:
 * <ul>
 * <li>{@code rdf:type} with a value of {@code oslc:Property}</li>
 * <li>{@code oslc:name}, with a literal string value, containing no markup</li>
 * <li>{@code oslc:occurs}, with a value of {@code oslc:Exactly-one}, {@code oslc:One-or-many},
 * {@code oslc:Zero-or-one}, or {@code oslc:Zero-or-many}</li>
 * <li>{@code oslc:propertyDefinition}, with a URI ending in the same string as the {@code oslc:name} property</li>
 * <li>{@code oslc:valueType}, with a value of any of the following:
 * <ul>
 * <li>{@code xsd:boolean}</li>
 * <li>{@code xsd:dateTime}</li>
 * <li>{@code xsd:decimal}</li>
 * <li>{@code xsd:double}</li>
 * <li>{@code xsd:float}</li>
 * <li>{@code xsd:integer}</li>
 * <li>{@code xsd:string}</li>
 * <li>{@code rdf:XMLLiteral}</li>
 * <li>{@code oslc:Resource}</li>
 * <li>{@code oslc:LocalResource}</li>
 * <li>{@code oslc:AnyResource}</li>
 * </ul>
 * </li>
 * <li>{@code oslc:representation}, if the {@code oslc:valueType} is {@code oslc:Resource},
 * with a value of {@code oslc:Reference}, {@code oslc:Inline}, or {@code oslc:Either}</li>
 * <li>{@code dcterms:description}, with a literal value of type string, {@code rdf:HTML}, or {@code rdf:XMLLiteral}</li>
 * </ul>
 * </li>
 * <li>The {@code oslc:name} and {@code oslc:propertyDefinition} values for each property must be unique in the values for that shape</li>
 * <li style="margin-top:5px;">A Property may also have any of the following properties:
 * <ul>
 * <li>{@code oslc:readOnly} with a boolean value</li>
 * <li>A single {@code oslc:allowedValues} property and/or one or more {@code oslc:allowedValue} properties</li>
 * <li>{@code oslc:defaultValue} with a value of type matching the {@code oslc:valueType}</li>
 * <li>{@code oslc:hidden} with a boolean value</li>
 * <li>{@code oslc:isMemberProperty} with a boolean value</li>
 * <li>{@code oslc:maxSize} with an integer size</li>
 * <li>{@code oslc:range}, referencing one or more types</li>
 * <li>{@code oslc:valueShape}, referencing another shape that will be validated
 * with these same rules</li>
 * </ul>
 * </li>
 * <li style="margin-top:5px;">Each property referenced by a shape must be
 * defined in the same document as the shape resource</li>
 * <li style="margin-top:5px;">Each property defined in a shape document must be
 * referenced from one of the shape resources defined in that document</li>
 * <li style="margin-top:5px;">The shape documents must not contain any
 * statements other than the shapes and properties covered above</li>
 * </ul>
 *
 * <h2>Consistency Checks</h2>
 * <p>
 * The following checks are applied across the set of vocabularies and shapes:
 * </p>
 * <ul>
 * <li>Each term referenced in a shape resource that uses the namespace of one
 * of the vocabularies must be defined in that vocabulary</li>
 * <li style="margin-top:5px;">Each term defined in a vocabulary should be used by at least one shape</li>
 * </ul>
 * <p>TBD: the above rules do not handle abstract base classes, properties and individuals used by discovery resources without shapes, etc.</p>
 *
 * <h2>Results</h2>
 * <p>
 * The results of these checks are placed into an RDF model, as defined by {@link net.open_services.scheck.shapechecker.ResultModel}.
 * TODO: Ideally, someone will write a web app to display this result model in a nice way.</p>
 *
 * @author Nick Crossley. Released to public domain 2015.
 */
package net.open_services.scheck.shapechecker;


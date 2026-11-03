{::options toc_levels="1..5" /}
[SPARQL (SPARQL Protocol and RDF Query Language)](https://www.w3.org/TR/sparql12-query/) is a W3C standard query language for [RDF](rdf.html) data. 
FHIR SPARQL queries match graph patterns in [FHIR RDF](fhir-rdf.html) data.
SPARQL can be used to explore, join, and transform FHIR data as a graph, including together with other RDF data such as ontologies and terminologies.

### Example FHIR SPARQL
Add these prefix declarations at the top of each query:
```sparql
PREFIX fhir: <http://hl7.org/fhir/>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
PREFIX loinc: <http://loinc.org/rdf/>
```

The following examples are based on FHIR R5+ RDF syntax and semantics, unless otherwise noted.

#### Find active patients

##### Triple pattern

SPARQL queries generally use [triple patterns](https://www.w3.org/TR/sparql12-query/#QSynTriples).
Each triple names its subject, predicate, and object, including the primitive
wrapper node.

```sparql
SELECT ?patient WHERE {
  ?patient a fhir:Patient .
  ?patient fhir:active ?active .
  ?active fhir:v true .
}
```

##### Blank nodes
Blank nodes used with [Predicate-object lists](https://www.w3.org/TR/sparql12-query/#predObjLists) shorten queries to match more closely the structure of FHIR Turtle data.

```sparql
SELECT ?patient WHERE {
  ?patient a fhir:Patient ;
      fhir:active [ fhir:v true ] .
}
```

##### Property paths
[Property paths](https://www.w3.org/TR/sparql12-query/#propertypaths) are helpful for traversing through multiple blank nodes.
```sparql
SELECT ?patient WHERE {
  ?patient a fhir:Patient ;
      fhir:active/fhir:v true .
}
```

###### STU3/R4 FHIR SPARQL
FHIR STU3 and R4 RDF use long property names.
```sparql
SELECT ?patient WHERE {
  ?patient a fhir:Patient ;
      fhir:Patient.active/fhir:value true .
}
```

#### List patient names

##### RDF collections (R5+)

FHIR R5+ RDF uses [RDF collections](https://www.w3.org/TR/sparql12-query/#collections),
which are linked lists.
In list-based FHIR RDF, `fhir:name` and `fhir:given` point to RDF collections.
The property path `rdf:rest*/rdf:first` traverses the RDF collection. `fhir:family` is single-valued. [OPTIONAL](https://www.w3.org/TR/sparql12-query/#optionals) includes `fhir:given` name values only when they are present. This example returns names with a family name, even when no given name is present.

```sparql

SELECT ?patient ?family ?given WHERE {
  ?patient a fhir:Patient ;
      fhir:name/rdf:rest*/rdf:first ?name .
  ?name fhir:family/fhir:v ?family .
  OPTIONAL {
    ?name fhir:given/rdf:rest*/rdf:first/fhir:v ?given 
  }
}
ORDER BY ?family
```

##### Indexed triples (internal data only)

FHIR R4 and STU3 RDF use indexed triples.
RDF collections in FHIR R5+ RDF may be converted to indexed triples for internal
usage only, not for conformant data exchange. This allows SPARQL queries to
simply omit the `rdf:rest*/rdf:first` property path expressions.

```sparql
SELECT ?patient ?family ?given WHERE {
  ?patient a fhir:Patient ;
      fhir:name ?name .
  ?name fhir:family/fhir:v ?family .
  # Variable ?name used for OPTIONAL clause below
  OPTIONAL {
    ?name fhir:given/fhir:v ?given
  }
}
ORDER BY ?family
```

The remaining examples use this pattern to simplify the queries.


#### References: Find observations by subject

References can be resolved by simply querying the `fhir:l` links added to FHIR RDF references.
The optional value pattern selects only Observation values of type Quantity.

```sparql
SELECT ?patient ?observation ?code ?value WHERE {
  ?observation a fhir:Observation ;
    fhir:subject/fhir:l ?patient ;
    fhir:code/fhir:coding/fhir:code/fhir:v ?code .
  OPTIONAL {
    ?observation fhir:value [
      a fhir:Quantity ;
      fhir:value/fhir:v ?value
    ]
  }
}
```

##### Indirect references: Find patients by practitioner NPI
FHIR [conditional references](https://www.hl7.org/fhir/http.html#vs-refs) could use identifiers, like `Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|9999947796`. 
```turtle
# Example usage of a conditional reference
[] a fhir:Bundle ;
  fhir:nodeRole fhir:treeRoot ;
  fhir:type [ fhir:v "transaction" ] ;
  fhir:entry [
     fhir:fullUrl [ fhir:v "urn:uuid:7f3c1e2a-0b5d-4c8e-9a61-2d4f8b9e0c11"^^xsd:anyURI ] ;
     fhir:resource <urn:uuid:7f3c1e2a-0b5d-4c8e-9a61-2d4f8b9e0c11> ;
     fhir:request [
       fhir:method [ fhir:v "POST" ] ;
       fhir:url    [ fhir:v "Patient"^^xsd:anyURI ]
     ]
  ] .

<urn:uuid:7f3c1e2a-0b5d-4c8e-9a61-2d4f8b9e0c11> a fhir:Patient ;
  fhir:generalPractitioner [
    fhir:reference [ fhir:v "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|9999947796" ]
  ] .
```

These should not be queried directly as IRI values of `fhir:l`, because the actual referenced resource would have a different IRI such as `<http://example.org/Practitioner/123`>.
```sparql
SELECT DISTINCT ?patient ?practitioner WHERE {
  ?patient a fhir:Patient ;
    fhir:generalPractitioner/fhir:reference/fhir:v "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|9999947796" .

  # No direct link between the reference and the referenced resource
  OPTIONAL {
    ?practitioner a fhir:Practitioner ;
      fhir:identifier [
        fhir:system/fhir:v "http://hl7.org/fhir/sid/us-npi"^^xsd:anyURI ;
        fhir:value/fhir:v "9999947796"
      ] .
  }
}
```
Outside of the context of bundles, these kinds of references are also better represented as [logical references](https://www.hl7.org/fhir/references.html#reference), which separate the identifier into an independently queryable value. 
```turtle
# Example usage of a logical reference
<urn:uuid:7f3c1e2a-0b5d-4c8e-9a61-2d4f8b9e0c11> a fhir:Patient ;
  fhir:generalPractitioner [
     fhir:type [ fhir:v "Practitioner"^^xsd:anyURI ] ;
     fhir:identifier [
       fhir:system [ fhir:v "http://hl7.org/fhir/sid/us-npi"^^xsd:anyURI ] ;
       fhir:value  [ fhir:v "9999947796" ]
     ]
  ] .
```


#### Find coded data

Match a single code in `Observation.code` using the literal `fhir:code` and
`fhir:system` values on the same Coding node. This example finds blood pressure
observations coded with LOINC `85354-9`, without requiring a concept IRI type
assertion. The `^^xsd:token` is required as the data type for FHIR code.

```sparql
SELECT ?observation WHERE {
  ?observation a fhir:Observation ;
    fhir:code [
      fhir:coding [
        fhir:code/fhir:v "85354-9"^^xsd:token ;
        fhir:system/fhir:v "http://loinc.org"^^xsd:anyURI
      ]
    ] .
}
```

##### Concept IRIs: Extract blood pressure components

Each nested `fhir:component` pattern matches a member of the repeated property.
The CodeableConcept nodes reached through `fhir:code` must carry the indicated
LOINC concept IRI type assertions in the selected derived graph.
Both quantities are restricted to UCUM `mm[Hg]` so their magnitudes have a
known unit; no unit conversion is performed.

```sparql
SELECT ?observation ?systolic ?diastolic WHERE {
  ?observation a fhir:Observation ;
    fhir:code [ a loinc:85354-9 ] ; # Concept IRI
    fhir:component [
      fhir:code [ a loinc:8480-6 ] ; # Concept IRI
      fhir:value [
        a fhir:Quantity ;
        fhir:value/fhir:v ?systolic ;
        fhir:system/fhir:v "http://unitsofmeasure.org"^^xsd:anyURI ;
        fhir:code/fhir:v "mm[Hg]"^^xsd:token
      ]
    ] ;
    fhir:component [
      fhir:code [ a loinc:8462-4 ] ; # Concept IRI
      fhir:value [
        a fhir:Quantity ;
        fhir:value/fhir:v ?diastolic ;
        fhir:system/fhir:v "http://unitsofmeasure.org"^^xsd:anyURI ;
        fhir:code/fhir:v "mm[Hg]"^^xsd:token
      ]
    ] .
}
```

#### String operations: Find patients by email

The discriminator and value belong to the same telecom node, corresponding to
the FHIRPath selection `Patient.telecom.where(system='email')`. This example
filters an email domain rather than implementing the FHIR `email` token search.

```sparql
SELECT ?patient ?email WHERE {
  ?patient a fhir:Patient ;
    fhir:telecom [
      fhir:system/fhir:v "email" ;
      fhir:value/fhir:v ?email
    ] .
  FILTER(STRENDS(LCASE(STR(?email)), "@example.com"))
}
```

#### Date ranges: Find observations in a date range

This example covers complete `xsd:dateTime` literals, using an inclusive start
and exclusive end for the year 2012 in UTC. FHIR partial dates and Period-valued
`effective` elements require separate handling; mixing `xsd:date` bounds with
`xsd:dateTime` values is not a portable comparison.

```sparql
SELECT ?observation ?date ?code WHERE {
  ?observation a fhir:Observation ;
    fhir:effective/fhir:v ?date ;
    fhir:code/fhir:coding/fhir:code/fhir:v ?code .
  FILTER(isLiteral(?date) && DATATYPE(?date) = xsd:dateTime)
  FILTER(?date >= "2012-01-01T00:00:00Z"^^xsd:dateTime &&
         ?date < "2013-01-01T00:00:00Z"^^xsd:dateTime)
}
ORDER BY ?date
```

#### Chained references: Join an observation search to a patient name

Following `fhir:l` joins an Observation to its Patient, as in a chained search
such as `Observation?subject:Patient.name=...`. The patient resource must be
loaded under the linked IRI. Here the filter is a case-insensitive family-name
substring match, not the full FHIR `name` search behavior.

```sparql
SELECT ?observation ?patientName ?code WHERE {
  ?observation a fhir:Observation ;
    fhir:subject/fhir:l [
      a fhir:Patient ;
      fhir:name/fhir:family/fhir:v ?patientName
    ];
    fhir:code/fhir:coding/fhir:code/fhir:v ?code .
  FILTER(CONTAINS(LCASE(?patientName), "smith"))
}
```

### Triple stores

No product recommendation is made yet. Evaluate standards conformance,
SHACL/ShEx integration, reasoning profile, RDF 1.2 plans, named-graph
semantics, backup, access control, and realistic FHIR query performance. A
reproducible benchmark remains a project TODO.

### Context graphs

Use named graphs to distinguish source assertions, terminology expansions,
inferred triples, and transformed views. Keep graph identity and provenance in
the dataset rather than relying on endpoint-specific storage conventions.

```sparql
SELECT ?graph (COUNT(*) AS ?triples)
WHERE { GRAPH ?graph { ?subject ?predicate ?object } }
GROUP BY ?graph
ORDER BY DESC(?triples)
```

### Relationship to FHIR analytics approaches

| Need | Prefer |
| --- | --- |
| Standardized FHIR search with parameters and custom operations | [FHIR REST API](https://hl7.org/fhir/http.html) |
| Stable tabular projections for BI tools | [SQL-on-FHIR](https://sql-on-fhir.org/) |
| Quality measures and clinical decision support | [CQL](https://cql.hl7.org/) |
| Graph traversal, ontology joins, inference, or RDF-native data | FHIR SPARQL |
| A relational source exposed as a virtual graph | R2RML/Ontop ("FHIR SPARQL-on-SQL") |
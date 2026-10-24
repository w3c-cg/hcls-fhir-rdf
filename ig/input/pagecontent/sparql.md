{::options toc_levels="1..5" /}
[SPARQL (SPARQL Protocol and RDF Query Language)](https://www.w3.org/TR/sparql11-query/) is a W3C standard query language for [RDF](rdf.html) data. 
FHIR SPARQL queries match graph patterns in [FHIR RDF](fhir-rdf.html) data.
SPARQL can be used to explore, join, and transform FHIR data as a graph, including together with other RDF data such as ontologies and terminologies.

### Example FHIR SPARQL
Add these prefix declarations at the top of each query:
```sparql
PREFIX fhir: <http://hl7.org/fhir/>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
PREFIX loinc: <https://loinc.org/rdf/>
```

The following examples are based on FHIR R6 RDF syntax and semantics, unless otherwise noted.

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
Blank nodes used in [Predicate-object lists](https://www.w3.org/TR/sparql12-query/#predObjLists) shorten queries to match more closely the structure of FHIR Turtle data.

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

##### Indexed triples (STU3/R4 or internal)

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

#### References: Find observations by subject

On the flat, multi-valued view, match the resolved subject IRI through `fhir:l`.
The nested shape retains the code and quantity structures, without list paths
or primitive wrappers. The optional value pattern selects quantity magnitudes,
not every possible Observation value type.

```sparql
SELECT ?observation ?code ?value WHERE {
  ?observation a fhir:Observation ;
    fhir:subject/fhir:l <http://hl7.org/fhir/Patient/example> ;
    fhir:code/fhir:coding/fhir:code ?code .
  OPTIONAL {
    ?observation fhir:value [
      a fhir:Quantity ;
      fhir:value ?value
    ]
  }
}
```

#### Find coded data

Match a single code in `Observation.code` using the literal `fhir:code` and
`fhir:system` values on the same Coding node. This example finds blood pressure
observations coded with LOINC `85354-9`, without requiring a concept IRI type
assertion.

```sparql
SELECT ?observation WHERE {
  ?observation a fhir:Observation ;
    fhir:code [
      fhir:coding [
        fhir:code "85354-9" ;
        fhir:system "http://loinc.org"^^xsd:anyURI
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
        fhir:value ?systolic ;
        fhir:system "http://unitsofmeasure.org"^^xsd:anyURI ;
        fhir:code "mm[Hg]"
      ]
    ] ;
    fhir:component [
      fhir:code [ a loinc:8462-4 ] ; # Concept IRI
      fhir:value [
        a fhir:Quantity ;
        fhir:value ?diastolic ;
        fhir:system "http://unitsofmeasure.org"^^xsd:anyURI ;
        fhir:code "mm[Hg]"
      ]
    ] .
}
```

##### String operations: Find patients by email

The discriminator and value belong to the same telecom node, corresponding to
the FHIRPath selection `Patient.telecom.where(system='email')`. This example
filters an email domain rather than implementing the FHIR `email` token search.

```sparql
SELECT ?patient ?email WHERE {
  ?patient a fhir:Patient ;
    fhir:telecom [
      fhir:system "email" ;
      fhir:value ?email
    ] .
  FILTER(STRENDS(LCASE(STR(?email)), "@example.com"))
}
```

##### Date ranges: Find observations in a date range

This example covers complete `xsd:dateTime` literals, using an inclusive start
and exclusive end for the year 2012 in UTC. FHIR partial dates and Period-valued
`effective` elements require separate handling; mixing `xsd:date` bounds with
`xsd:dateTime` values is not a portable comparison.

```sparql
SELECT ?observation ?date ?code WHERE {
  ?observation a fhir:Observation ;
    fhir:effective ?date ;
    fhir:code/fhir:coding/fhir:code ?code .
  FILTER(isLiteral(?date) && DATATYPE(?date) = xsd:dateTime)
  FILTER(?date >= "2012-01-01T00:00:00Z"^^xsd:dateTime &&
         ?date < "2013-01-01T00:00:00Z"^^xsd:dateTime)
}
ORDER BY ?date
```

##### Chained references: Join an observation search to a patient name

Following `fhir:l` joins an Observation to its Patient, as in a chained search
such as `Observation?subject:Patient.name=...`. The patient resource must be
loaded under the linked IRI. Here the filter is a case-insensitive family-name
substring match, not the full FHIR `name` search behavior.

```sparql
SELECT ?observation ?patientName ?code WHERE {
  ?observation a fhir:Observation ;
    fhir:subject/fhir:l [
      a fhir:Patient ;
      fhir:name/fhir:family ?patientName .
    ];
    fhir:code/fhir:coding/fhir:code ?code .
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
| Quality measures and clinical decision support | [CQL](https://build.fhir.org/ig/HL7/cql/) |
| Graph traversal, ontology joins, inference, or RDF-native data | FHIR SPARQL |
| A relational source exposed as a virtual graph | R2RML/Ontop ("FHIR SPARQL-on-SQL") |
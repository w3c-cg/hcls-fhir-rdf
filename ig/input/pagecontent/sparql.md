[SPARQL (SPARQL Protocol and RDF Query Language)](https://www.w3.org/TR/sparql11-query/) patterns for exploring FHIR RDF as a graph.

### Choose your FHIR version and syntax

These examples use FHIR R6 data, including `fhir:l` for resource links and references. 

The first two examples query primitive wrappers and RDF collections directly. All
subsequent FHIR queries use a **flat, multi-valued derived graph**: primitives
become literal values instead of `[ fhir:v ... ]`, and RDF collections become repeated
triples with the same predicate. Structural nodes such as names, references,
codings, and quantities remain. Nested brackets are SPARQL blank-node property
lists, not RDF collections.

Keep this transformed view separate from the source: flattening can discard
primitive metadata and list order. Preserve reference links and Coding type
assertions used below. The examples assume the selected view is the query's
default graph; use `GRAPH <...>` when querying it as a named graph. These are
illustrative query patterns, not complete implementations of FHIR search
semantics, paging, or search-result Bundles.

### Example FHIR SPARQL
#### Find active patients: explicit triples

Each triple names its subject, predicate, and object, including the primitive
wrapper node.

```sparql
PREFIX fhir: <http://hl7.org/fhir/>

SELECT ?patient WHERE {
  ?patient a fhir:Patient .
  ?patient fhir:active ?active .
  ?active fhir:v true .
}
```

#### List patient names: RDF collections

In list-based FHIR RDF, `fhir:name` and `fhir:given` point to RDF collections.
The path `rdf:rest*/rdf:first` reaches their members; `fhir:family` is
single-valued and needs no list traversal. This example returns names with a
family name, even when no given name is present.

```sparql
PREFIX fhir: <http://hl7.org/fhir/>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?patient ?family ?given WHERE {
  ?patient a fhir:Patient ;
    fhir:name/rdf:rest*/rdf:first ?name .
  ?name fhir:family [ fhir:v ?family ] .
  OPTIONAL { ?name fhir:given/rdf:rest*/rdf:first [ fhir:v ?given ] }
}
ORDER BY ?family
```

#### Find observations by subject

On the flat, multi-valued view, match the resolved subject IRI through `fhir:l`.
The nested shape retains the code and quantity structures, without list paths
or primitive wrappers. The optional value pattern selects quantity magnitudes,
not every possible Observation value type.

```sparql
PREFIX fhir: <http://hl7.org/fhir/>

SELECT ?observation ?code ?value WHERE {
  ?observation a fhir:Observation ;
    fhir:subject [ fhir:l <http://hl7.org/fhir/Patient/example> ] ;
    fhir:code [ fhir:coding [ fhir:code ?code ] ] .
  OPTIONAL {
    ?observation fhir:value [ a fhir:Quantity ; fhir:value ?value ]
  }
}
```

#### Extract blood pressure components

Each nested `fhir:component` pattern matches a member of the repeated property.
The Coding nodes must carry the indicated LOINC concept IRI type assertions.
Both quantities are restricted to UCUM `mm[Hg]` so their magnitudes have a
known unit; no unit conversion is performed.

```sparql
PREFIX fhir:  <http://hl7.org/fhir/>
PREFIX loinc: <https://loinc.org/rdf/>
PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>

SELECT ?observation ?systolic ?diastolic WHERE {
  ?observation a fhir:Observation ;
    fhir:code [ fhir:coding [ a loinc:85354-9 ] ] ;
    fhir:component [
      fhir:code [ fhir:coding [ a loinc:8480-6 ] ] ;
      fhir:value [ a fhir:Quantity ;
        fhir:value ?systolic ;
        fhir:system "http://unitsofmeasure.org"^^xsd:anyURI ;
        fhir:code "mm[Hg]" ]
    ] ;
    fhir:component [
      fhir:code [ fhir:coding [ a loinc:8462-4 ] ] ;
      fhir:value [ a fhir:Quantity ;
        fhir:value ?diastolic ;
        fhir:system "http://unitsofmeasure.org"^^xsd:anyURI ;
        fhir:code "mm[Hg]" ]
    ] .
}
```

#### Find patients by email

The discriminator and value belong to the same telecom node, corresponding to
the FHIRPath selection `Patient.telecom.where(system='email')`. This example
filters an email domain rather than implementing the FHIR `email` token search.

```sparql
PREFIX fhir: <http://hl7.org/fhir/>

SELECT ?patient ?email WHERE {
  ?patient a fhir:Patient ;
    fhir:telecom [
      fhir:system "email" ;
      fhir:value ?email
    ] .
  FILTER(STRENDS(LCASE(STR(?email)), "@example.com"))
}
```

#### Find observations in a date range

This example covers complete `xsd:dateTime` literals, using an inclusive start
and exclusive end for the year 2012 in UTC. FHIR partial dates and Period-valued
`effective` elements require separate handling; mixing `xsd:date` bounds with
`xsd:dateTime` values is not a portable comparison.

```sparql
PREFIX fhir: <http://hl7.org/fhir/>
PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>

SELECT ?observation ?date ?code WHERE {
  ?observation a fhir:Observation ;
    fhir:effective ?date ;
    fhir:code [ fhir:coding [ fhir:code ?code ] ] .
  FILTER(isLiteral(?date) && DATATYPE(?date) = xsd:dateTime)
  FILTER(?date >= "2012-01-01T00:00:00Z"^^xsd:dateTime &&
         ?date < "2013-01-01T00:00:00Z"^^xsd:dateTime)
}
ORDER BY ?date
```

#### Chain an observation search to a patient name

Following `fhir:l` joins an Observation to its Patient, as in a chained search
such as `Observation?subject:Patient.name=...`. The patient resource must be
loaded under the linked IRI. Here the filter is a case-insensitive family-name
substring match, not the full FHIR `name` search behavior.

```sparql
PREFIX fhir: <http://hl7.org/fhir/>

SELECT ?observation ?patientName ?code WHERE {
  ?observation a fhir:Observation ;
    fhir:subject [ fhir:l ?patient ] ;
    fhir:code [ fhir:coding [ fhir:code ?code ] ] .
  ?patient a fhir:Patient ;
    fhir:name [ fhir:family ?patientName ] .
  FILTER(CONTAINS(LCASE(?patientName), "smith"))
}
```

### Relationship to SQL-on-FHIR

| Need | Prefer |
| --- | --- |
| Stable tabular projections for BI tools | [SQL-on-FHIR](https://sql-on-fhir.org/) |
| Graph traversal, ontology joins, inference, or RDF-native data | SPARQL |
| A relational source exposed as a virtual graph | R2RML/Ontop, then SPARQL |

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

### Triple stores

No product recommendation is made yet. Evaluate standards conformance,
SHACL/ShEx integration, reasoning profile, RDF 1.2 plans, named-graph
semantics, backup, access control, and realistic FHIR query performance. A
reproducible benchmark remains a project TODO.



### Query patterns
#### Example FHIR R6 RDF data
```turtle
:pat1 a fhir:Patient ;
  fhir:active [ fhir:v true ] ;
  fhir:name ( [
     fhir:use [ fhir:v "official" ] 
     fhir:family [ fhir:v "Chalmers" ] ;
     fhir:given ( [ fhir:v "Peter" ] [ fhir:v "James" ] )
  ] [
     fhir:use [ fhir:v "usual" ] ;
     fhir:given ( [ fhir:v "Jim" ] )
  ] ) ;

:pat2 a fhir:Patient ;
  fhir:active [ fhir:v false ] ;
  fhir:name ( [
    fhir:use [ fhir:v "official" ] ;
    fhir:family [ fhir:v "Bor" ] ;
  ] ) 
```

#### 1. Triple pattern (all versions)
SPARQL queries generally use [triple patterns](https://www.w3.org/TR/sparql12-query/#QSynTriples).
```sparql
SELECT ?patient WHERE {
  ?patient a fhir:Patient .
  ?patient fhir:active ?active .
  ?active fhir:v true .
}
```
Result: `:pat1`

#### 2. Blank nodes pattern (all versions)
SPARQL queries can use blank nodes with [predicate-object lists](https://www.w3.org/TR/sparql12-query/#predObjLists), which makes the queries match more closely the structure of FHIR Turtle data.
```sparql
SELECT ?patient WHERE {
  ?patient a fhir:Patient ;
      fhir:active [ fhir:v true ] .
}
```
Result: `:pat1`

#### 3. RDF collections (R5+)
FHIR R5+ RDF uses [RDF collections](https://www.w3.org/TR/sparql12-query/#collections), which are linked lists.
The query below shows how to traverse these collections using [property paths](https://www.w3.org/TR/sparql12-query/#propertypaths). It also shows the use of [OPTIONAL](https://www.w3.org/TR/sparql12-query/#optionals) to include `fhir:given` name values only when they are present.
```sparql
SELECT ?patient ?family ?given WHERE {
  ?patient a fhir:Patient ;
      fhir:name/rdf:rest*/rdf:first ?name .
  ?name fhir:family [ fhir:v ?family ] .
  OPTIONAL { ?name fhir:given/rdf:rest*/rdf:first [ fhir:v ?given ] }
}
```
Result:

| ?patient | ?family | ?given |
| --- | --- | --- |
| `:pat1` | `"Chalmers"` | `"Peter"` |
| `:pat1` | `"Chalmers"` | `"James"` |
| `:pat2` | `"Bor"` | |

#### 4. Indexed triples
FHIR R4 and STU3 RDF use indexed triples. 
RDF collections in FHIR R5+ RDF may be converted to indexed triples for internal usage only, not for conformant data exchange.
This allows SPARQL queries to simply omit the `rdf:rest*/rdf:first` property path expressions.
```sparql
SELECT ?patient ?family ?given WHERE {
  ?patient a fhir:Patient ;
      fhir:name ?name .
  ?name fhir:family [ fhir:v ?family ] .
  OPTIONAL { ?name fhir:given [ fhir:v ?given ] }
}
```
Result:

| ?patient | ?family | ?given |
| --- | --- | --- |
| `:pat1` | `"Chalmers"` | `"Peter"` |
| `:pat1` | `"Chalmers"` | `"James"` |
| `:pat2` | `"Bor"` |  |

#### 5. Unwrapped primitive nodes
The extra `fhir:v` attached to primitive element nodes can be eliminated, for internal usage only, if no extensions are present on any primitive element values. Note: this also removes type information from choice types.
```sparql
SELECT ?patient ?family ?given WHERE {
  ?patient a fhir:Patient ;
      fhir:name ?name .
  ?name fhir:family ?family .
  OPTIONAL { ?name fhir:given ?given }
}
```
Result:

| ?patient | ?family | ?given |
| --- | --- | --- |
| `:pat1` | `"Chalmers"` | `"Peter"` |
| `:pat1` | `"Chalmers"` | `"James"` |
| `:pat2` | `"Bor"` | |
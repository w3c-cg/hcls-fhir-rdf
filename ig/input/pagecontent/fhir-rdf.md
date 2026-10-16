[FHIR RDF](https://www.hl7.org/fhir/rdf.html) is one of three formats defined in the core FHIR specification. 

FHIR RDF represents FHIR resources as a graph. It does not replace the FHIR resource model, introduce a new clinical model, or require the use of an ontology reasoner. 
This guide offers optional techniques for querying, validating, and automated reasoning with FHIR RDF data.

### Getting FHIR RDF data

Any FHIR JSON or FHIR XML resource can be converted to FHIR RDF Turtle with the
[HL7 FHIR Validator](https://confluence.hl7.org/display/FHIR/Using+the+FHIR+Validator).
It can be used as a command-line tool (see below) or as a Java library from the [org.hl7.fhir.core](https://github.com/hapifhir/org.hl7.fhir.core) project.

#### Choose a FHIR version

The Turtle syntax differs between releases. FHIR R4 RDF uses long property names like
`fhir:Observation.status [ fhir:value "final" ]` and `fhir:index` for tracking element order. 
FHIR R5+ RDF uses short propert names like `fhir:status [ fhir:v "final" ]` and [RDF collections](https://www.w3.org/TR/rdf11-mt/#rdf-collections) for tracking element order.

#### Convert between FHIR formats

1. Install Java (see the validator docs for the minimum version).
2. Download the latest
   [`validator_cli.jar`](https://github.com/hapifhir/org.hl7.fhir.core/releases/latest/download/validator_cli.jar).
3. Convert one file. The `.ttl` extension of the output file selects Turtle;
   the input can be JSON or XML:

   ```sh
   java -jar validator_cli.jar convert -version 4.0.1 -tx n/a -output observation.ttl observation.json
   ```

4. Convert many files at once with `-outputSuffix`. Each output is written
   next to its input with the suffix appended, such as `observation.json.ttl`:

   ```sh
   java -jar validator_cli.jar convert -version 4.0.1 -tx n/a -outputSuffix ttl data/*.json
   ```

On first use, the validator downloads the FHIR core package for the requested
release and caches it in `~/.fhir/packages`, so it needs network access once.
Conversion does not validate the data. No terminology server is needed.


### Key features of FHIR RDF

#### **Context**
The FHIR vocabulary is declared in an RDF document with `@prefix fhir: <http://hl7.org/fhir/>`, allowing RDF applications to process it agnostically and combined with data from other vocabularies.

#### **Typed graph nodes**
Every resource has an explicit type, and every node has an implicit type, indicated by `a` or `rdf:type` and represented as classes in the FHIR OWL ontology. Choice types are declared explicitly.

#### **Resource & reference links**
URIs and references are rendered with `fhir:l` links that are directly dereferenceable by RDF applications. FHIR resources are just one kind of resource in the broader sense of the web (as in "Uniform Resource Identifier").

#### **Concept IRIs**
Terminology and ontology codes can include links to the RDF/OWL resources that define them, named by IRIs (International Resource Identifiers), a superset of URIs. By declaring a prefix like `@prefix sct: <http://snomed.info/id/>` one can use a shorthand [CURIE (Compact URI Expression)](https://www.w3.org/TR/curie/) like `sct:368209003`.


#### Example FHIR RDF
```turtle
@prefix fhir: <http://hl7.org/fhir/> .

<http://hl7.org/fhir/Observation/blood-pressure> a fhir:Observation ;
  fhir:id [ fhir:v "blood-pressure"] ;
  fhir:code [
     a loinc:85354-9 ; # OPTIONAL concept IRI
     fhir:coding ( [
       fhir:system [
         fhir:l <http://loinc.org> # OPTIONAL resource link
         fhir:v "http://loinc.org"^^xsd:anyURI ;
       ] ;
       fhir:code [ fhir:v "85354-9" ] ;
       fhir:display [ fhir:v "Blood pressure panel with all children optional" ]
     ] ) ;
     fhir:text [ fhir:v "Blood pressure systolic & diastolic" ]
  ] ;
  fhir:subject [
     fhir:l <http://hl7.org/fhir/Patient/example> ;
     fhir:reference [ fhir:v "Patient/example" ]
  ] ;
  fhir:effective [
     a fhir:DateTime ;
     fhir:v "2012-09-17"^^xsd:date
  ] . 

<http://hl7.org/fhir/Patient/example> a fhir:Patient .
```

Features seen in the example above:
* Context: `@prefix` declares an abbreviation. `fhir:Observation` expands to
  `http://hl7.org/fhir/Observation`; the prefix label itself has no special meaning.

* Graph identifiers: `<...>` encloses an IRI which uniquely identifies this resource in a graph. This is similiar to `fhir:id` or a canonical URL.

* Typing resources and elements: `a` is Turtle shorthand for [`rdf:type`](https://www.w3.org/TR/rdf-schema/#ch_type). The first statement
  corresponds to `"resourceType": "Observation"` in FHIR JSON.

* [Triples](https://www.w3.org/TR/rdf-primer/#section-triple) ending with `;` are [predicate lists](https://www.w3.org/TR/turtle/#predicate-lists) that share a subject. `.` ends the group of triples for a subject. Whitespace and indentation do not define the graph.

* [Blank nodes](https://www.w3.org/TR/rdf11-primer/#section-blank-node) are shown with `[ ... ]`. For example, the object of the `fhir:id` triple is a blank node, which in turn is the subject of a `fhir:v` triple, whose object is the literal value "blood-pressure".

* Resource links: A `fhir:l` triple is a resource link. These are most useful for references.

* Concept IRIs: `loinc:85354-9` is a concept IRI, which can also be encoded using an [RDF Concept IRI Extension](https://build.fhir.org/ig/HL7/fhir-extensions/StructureDefinition-rdf-concept-iri.html).

#### Primitive elements
FHIR RDF primitive elements have extra, wrapper `fhir:v` nodes for two reasons:
1. Primitive elements in FHIR can have extensions and also have 0 min cardinality. FHIR JSON represents extended primitive elements with underscores, like "_code". FHIR RDF represents extended primitives with `fhir:extension` added to the original primitive element node, instead.

```turtle
:pat1 a fhir:Patient ;
  fhir:active [ 
    # a fhir:Boolean ;
    fhir:v true 
  ] .

# Missing fhir:v but cardinality for fhir:Boolean is 0..1
:pat2 a fhir:Patient ;
  fhir:active [
    # a fhir:Boolean ;
    fhir:extension ( [
       fhir:url [ fhir:v "http://hl7.org/fhir/StructureDefinition/data-absent-reason"^^xsd:anyURI ] ;
       fhir:value [
         a fhir:Code ; # choice type indicator
         fhir:v "unknown"
       ]
     ] )
  ] .
```

2. Choice type elements assert what type they are on the primitive element node. FHIR JSON represents these with suffixes added to the element name, like "effectiveDateTime". Some choice types can either be complex or primitive elements, which correspond to [object properties](https://www.w3.org/TR/owl2-syntax/#Object_Properties) (having RDF resource values) and [data properties](https://www.w3.org/TR/owl2-syntax/#Data_Properties) (having literal values), respectively, and cannot be mixed in OWL. 


#### Repeating elements: RDF collections and indexed triples

RDF graphs are sets of [triples](https://www.w3.org/TR/rdf-primer/#section-triple): reordering triples does not change the graph, and writing the same triple twice does not create two occurrences.
However, FHIR elements with >1 max cardinality can have repeated values whose order must be preserved.
FHIR R5+ uses **[RDF collections](https://www.w3.org/TR/rdf11-mt/#rdf-collections)** for repeating, ordered elements. In Turtle, parentheses write
a list, as in this separate, abbreviated R5 Patient fragment:

```turtle
@prefix fhir: <http://hl7.org/fhir/> .

<https://example.org/fhir/Patient/p001> a fhir:Patient ;
  fhir:name ( [
    fhir:given ( [ fhir:v "Alex" ] [ fhir:v "Morgan" ] )
  ] ) .
```

This example shows `fhir:name` with two given names in order. Parentheses encode
RDF collections, which translate to a linked list: `rdf:first`, `rdf:rest`, and `rdf:nil`.

FHIR R4 RDF does not use RDF collections, but instead uses repeated **indexed triples** with `fhir:index` to track the order instead.
RDF applications can use indexed triples internally, provided that R5+ data is serialized with RDF collections for conformant exchange.
<a href="query-artifacts.html">SPARQL queries</a> can translate between RDF collections and indexed triples.


### FHIR RDF Versions

FHIR RDF has evolved with FHIR since version STU3. The main differences between each version are:

Versions R5 and above:
- Use short property names like `fhir:name` instead of `fhir:Patient.name`
- Use `fhir:v` for primitive values and `fhir:l` for links instead of `fhir:value` and `fhir:link` 
- Use RDF collections for repeated elements instead of indexed triples

Versions R4 and below:
- Use fully qualified property names like `fhir:Patient.name` instead of `fhir:name`
- Use `fhir:value` and `fhir:l` instead of `fhir:v` and `fhir:l`
- Use indexed triples for repeated elements instead of RDF collections

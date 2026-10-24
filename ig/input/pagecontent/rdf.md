[RDF (Resource Description Framework)](https://www.w3.org/TR/rdf-primer/) is a W3C standard data model for representing information as a graph of linked statements. 
[FHIR RDF](fhir-rdf.html) represents FHIR resources in RDF, with Turtle as the syntax for conformant exchange.
RDF can be used to link FHIR data with other data, ontologies, and terminologies, and to query, validate, and reason over it with SPARQL, ShEx, and OWL.

### RDF Triples

In FHIR JSON, you navigate resource objects, nested elements, and references.
In FHIR RDF, you work with statements called **triples**. Each triple has three parts:

| Part | Meaning | FHIR example |
| --- | --- | --- |
| Subject | The thing the statement is about | A particular Patient resource `<Patient/pat1>` |
| Predicate | The property or relationship being asserted | Its resource type `a` (short for `rdf:type`) |
| Object | The value or thing at the other end | The FHIR Patient class `fhir:Patient` |

A set of triples forms an **RDF graph**. Subjects and objects connect statements
to one another, so a nested element or a linked resource can be reached by
following edges. A graph is not necessarily a picture or a particular storage
technology; it can be held in memory, saved in a file, or queried in an RDF
database, often called a **triple store**.

RDF uses three kinds of terms:

* An **IRI** (Internationalized Resource Identifier) identifies a resource,
  property, or other named thing. For example, `http://hl7.org/fhir/Patient`
  identifies the Patient class, whereas a resource IRI identifies one Patient resource instance.
  An IRI can look like a web address without guaranteeing that it can be
  retrieved. Using it does not automatically fetch data from that address.
* A **literal** is a value, such as a string, boolean, or date, with a datatype
  or, for language-tagged text, a language tag. The string `"Patient/p001"`
  is not itself an RDF link to a Patient resource.
* A **blank node** represents a thing without assigning it an IRI. FHIR RDF
  commonly uses blank nodes for nested elements. A blank node is not a missing
  value, and its label is not a stable identifier across files or imports.

Subjects can be IRIs or blank nodes. Predicates are IRIs. Objects can be any
of these three kinds of terms. Shared IRIs let independently produced graphs
refer to the same things, but applications still need an explicit policy for
resolving FHIR references and reconciling resource identities.

### RDF is a data model and Turtle is a syntax

RDF is an abstract data model, not a file format. [RDF Turtle](https://www.w3.org/TR/turtle/) is a syntax for writing RDF and is the serialization required for conformant exchange of FHIR RDF data. "FHIR RDF" is conventionally used when discussing semantic aspects of FHIR RDF, or features that aren't dependent on specific syntax. "FHIR Turtle" or "FHIR RDF Turtle" is conventionally used when discussing specific syntactic features, but these terms are often interchangeable in most contexts.

Other RDF syntaxes include [JSON-LD](https://www.w3.org/TR/json-ld/) and [RDF/XML](https://www.w3.org/TR/rdf-syntax-grammar/). Ordinary FHIR JSON is not
JSON-LD, and FHIR XML is not RDF/XML. Changing a file extension does not convert
between these representations.

FHIR RDF preserves the FHIR resource and element structure needed for
round-trip conversion with FHIR JSON and XML. Use a FHIR-aware converter for
the relevant release. Application-specific transformations, such as removing intermediate, primitive type `fhir:v` nodes, may lose information (such as extensions or choice type assertions) and are not conformant for exchange.

### Four complementary standards

| Standard | Role |
| --- | --- |
| [RDF](https://www.w3.org/TR/rdf12-concepts/) | Represents facts as subject-predicate-object triples identified by IRIs. |
| [SPARQL](https://www.w3.org/TR/sparql12-query/) | Queries and transforms RDF datasets. |
| [ShEx](http://shex.io/) | Validates the local shape of RDF nodes. |
| [OWL](https://www.w3.org/OWL/) | Expresses ontologies and supports logical inference. |

#### SPARQL: match graph patterns ([FHIR SPARQL](sparql.html))

[SPARQL ("SPARQL Protocol and RDF Query Language")](https://www.w3.org/TR/sparql11-query/) is a query language for RDF. A `SELECT` query returns bindings for
variables, written with `?`, by matching graph patterns. No ontology or inference is needed:

```sparql
PREFIX fhir: <http://hl7.org/fhir/>

SELECT ?patient WHERE {
  ?patient a fhir:Patient ;
    fhir:active/fhir:v true .
}
```

`WHERE` describes the pattern to match, and `/` follows two properties in
sequence through the primitive wrapper. With that Patient Turtle example loaded
as the query's default graph, the result is one row binding `?patient` to
the resource IRI shown in that example.

This is a query over the supplied RDF dataset, not a FHIR REST search request.
It does not contact a FHIR server or follow web links automatically. If a
Patient has no matching active value in that dataset, it is absent from the
results. That does not establish that its active value is false.
Continue with the [SPARQL examples](sparql.html), which explicitly distinguish
source graphs from derived query views.

#### ShEx: validate any RDF ([FHIR ShEx](validation.html))

[ShEx (Shape Expressions)](https://shex.io/shex-semantics/) checks whether an RDF node matches a shape, such as
the allowed properties, value types, and cardinalities for a FHIR resource.
Use the ShEx schema for the data's FHIR release. Passing a shape check is not
by itself proof that every FHIR invariant, terminology binding, and business
rule has been satisfied. See [Validation](validation.html).

#### OWL: open-world logical inference ([FHIR OWL](owl.html))
[OWL (Web Ontology Language)](https://www.w3.org/TR/owl2-overview/) describes classes and relationships using logical
axioms. The FHIR ontology is derived from core StructureDefinitions and
ElementDefinitions. For example, if the ontology says that `fhir:Patient` is a
subclass of `fhir:DomainResource`, a reasoner can infer that a resource typed
as `fhir:Patient` is also a `fhir:DomainResource`.

OWL uses an **open-world assumption**: missing statements are not assumed to
be false. An OWL cardinality restriction does not act like a validator's
required-field check. An unrecorded value may still exist. Different IRIs are
also not automatically assumed to denote different things. Consistency with
an ontology is therefore not the same as FHIR conformance. See [FHIR OWL Ontology Guide W3C Community Group Report](https://w3c-cg.github.io/hcls-fhir-rdf/spec/ontology.html) for more, in-depth explanation.

Using RDF or SPARQL does not require OWL. Loading an ontology into a triple
store does not necessarily enable inference. The application must select
reasoning support explicitly. FHIR resource classes describe information
resources, not automatically the real-world people or diseases they describe.
See [OWL examples](owl.html) for reusable classification logic and its limits.
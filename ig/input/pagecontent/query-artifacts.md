This page provides a list of the downloadable SPARQL artifacts included in
this implementation guide. These are not normative, conformance resources.

### Pre-processing

These SPARQL updates transform working copies of FHIR RDF graphs for downstream
processing. Before running these, keep your original data in a separate graph for provenance.

<table class="grid">
  <tbody>
    <tr>
      <td><a href="common/preprocessing/add-concept-iri-extensions.sparql">Derive concept IRI extensions</a></td>
      <td>Uses NamingSystem IRI stems and Coding codes to add concept IRI extensions. 
      Only needed if a FHIR RDF serializer doesn't automatically add these. Using a cached table of NamingSystems and IRI stems is faster.
      See <a href="#derive-concept-iri-extensions">usage notes and limitations</a>.</td>
    </tr>
    <tr>
      <td><a href="common/preprocessing/interpret-concept-iris.sparql">Interpret concept IRIs extensions</a></td>
      <td>Adds type assertions to CodeableConcept nodes for each Coding that has a concept IRI extension.
      See <a href="#interpret-concept-iri-extensions">usage notes and limitations</a>.</td>
    </tr>
    <tr>
      <td><a href="common/preprocessing/rdf-collections-to-indexed-triples-blank-nodes.sparql">RDF collections to indexed triples</a></td>
      <td>Converts ("flattens") RDF collections in FHIR RDF data to indexed triples, where each triple has a <code>fhir:index</code> property for tracking its order. See <a href="#rdf-collections-to-indexed-triples">usage notes and limitations</a>.</td>
    </tr>
    <tr>
      <td><a href="common/preprocessing/long-properties-to-short.sparql">Long property names to short property names</a></td>
      <td>Converts long, qualified or namespaced property names in FHIR RDF data like <code>fhir:Patient.name</code> to short property names like <code>fhir:name</code>. 
      See <a href="#long-property-names-to-short-property-names">usage notes and limitations</a>.
      </td>
    </tr>
  </tbody>
</table>

### Usage Notes

The examples below use [Apache Jena](https://jena.apache.org/documentation/query/)'s
`update` command. Download each update and use its local filename in the command.
All updates operate on the default graph as written. For named-graph data,
adapt both the update templates and matching patterns with explicit `GRAPH`
clauses. Check your triple store's default-graph configuration if no matches are found.

```sh
# Template
update --data=patient-r6.ttl --data=<input .ttl filepath> \
  --update=<SPARQL query filepath> --dump <output .ttl filepath>

# Example
update --data=patient-r6.ttl --data=observation.ttl \
  --update=rdf-collections-to-indexed-triples-blank-nodes.sparql --dump observation-indexed-triples.ttl
```


#### Derive concept IRI extensions

[Download the SPARQL update](common/preprocessing/add-concept-iri-extensions.sparql).

Use this to attach an `rdf-concept-iri` extension to each eligible Coding.
The concept IRI is formed by concatenating a NamingSystem's IRI stem with
the `Coding.code` value. The concept IRI is represented as both a `xsd:anyURI` value of `fhir:v` and as an IRI value of `fhir:l`.

**Required Input:** FHIR RDF with RDF collections with explicit `a fhir:Coding` element type assertions. Relevant NamingSystem
resources must be in the same default graph, with `fhir:uniqueId` lists
containing a `"uri"` entry matching the Coding system and an `"iri-stem"`
entry supplying the stem. If necessary, materialize element type assertions
before running the update. The update does not infer them or fetch NamingSystems.

The update appends to an existing nonempty extension list or creates a list
when none exists. Use inputs without explicit empty `fhir:extension rdf:nil`
links, since these are not removed when a new list is inserted. Codings with
an existing concept IRI extension identified by `fhir:url/fhir:l` are
skipped.

Ensure each Coding system has one unambiguous IRI stem. If several match,
the query uses `SAMPLE`, so its selection is not deterministic. Confirm that
the terminology's IRI construction convention uses URI-encoded codes. This
update does not validate the resulting IRI or resolve it on the web.


#### Interpret concept IRI extensions

[Download the SPARQL update](common/preprocessing/interpret-concept-iris.sparql).

Use this after adding concept IRI extensions, or on data that already has
them. For each matching Coding, the update adds `rdf:type` with the concept
IRI to the enclosing CodeableConcept node. It does not type the Patient or
Observation resource, nor does it run an OWL reasoner.

**Required Input:** explicit `a fhir:CodeableConcept` assertions and
list-based `fhir:coding` values. Each relevant Coding must have a concept IRI
extension with `fhir:url/fhir:l` pointing to
`http://hl7.org/fhir/StructureDefinition/rdf-concept-iri` and
`fhir:value/fhir:l` pointing to the concept IRI. Extension values may be
list-based or directly linked. Literal-only extension values do not match.

Multiple matching codings can add multiple types. Repeating the update does
not duplicate existing triples. Treat these assertions as derived interpretation:
confirm that the concept IRIs are appropriate as classes for your downstream
ontology, and retain the source graph separately.

Run both concept updates **before** converting Coding and NamingSystem RDF collections
to repeated or indexed triples: their matching patterns require those RDF collections.


#### RDF collections to indexed triples

[Download the SPARQL update](common/preprocessing/rdf-collections-to-indexed-triples-blank-nodes.sparql).

Use this when downstream processing needs repeated FHIR properties with
zero-based `fhir:index` values on their member nodes. For example,
`fhir:given ( [ fhir:v "Alex" ] [ fhir:v "Morgan" ] )` becomes two
`fhir:given` links to the same wrapper nodes, with indices `0` and `1`.
Primitive wrappers and their other properties remain intact.

**Required Input:** well-formed RDF collections attached through FHIR
properties, with distinct blank-node or IRI members for each occurrence.
Members must not be literals or other list heads. Check malformed or cyclic
lists separately. Use member nodes that are not shared across lists and do
not already carry conflicting `fhir:index` values, since indices are attached
to members rather than to list occurrences.

The update removes empty-list property links and deletes `rdf:first` and
`rdf:rest` triples for converted cells. OWL vocabulary is skipped in this script, which requires RDF collections to serialize axioms in RDF Turtle.
It excludes list heads reachable through the OWL list properties enumerated in the query, but that is not a
general shared-list protection mechanism. Do not use it on lists whose cells
are also needed by other, unconverted links.

Repeated occurrences of the same RDF term cannot be represented independently:
their property links collapse, and the member may acquire several indices.
Distinct wrapper nodes with equal literal values remain distinct. The output
is a derived indexed view, not conformant R5/R6 exchange RDF or a complete
R4 conversion. Keep the original graph and validate the transformed view for
its intended use.

#### Long property names to short property names

[Download the SPARQL update](common/preprocessing/long-properties-to-short.sparql).

Use this to remove resource, datatype, and backbone-element qualifiers from
FHIR predicates. It keeps the part after the last dot: `fhir:Patient.name`
becomes `fhir:name`, `fhir:HumanName.given` becomes `fhir:given`, and
`fhir:Patient.contact.name` becomes `fhir:name`. Inherited properties follow
the same rule: `fhir:Resource.id`, `fhir:DomainResource.text`, and
`fhir:Element.extension` become `fhir:id`, `fhir:text`, and `fhir:extension`.

Only short property names are used in conformant exchange of FHIR R5+ RDF data. Only long property names are used in conformant FHIR R4 RDF.
Either can be used in internal applications.

**Required Input:** FHIR instance data using predicates in the exact namespace
`http://hl7.org/fhir/`, with dot-separated qualified local names. Explicit
resource or element type assertions are not required. The update works with
either RDF collections or repeated properties. No RDF collection flattening step is needed. 
Avoid applying it to ontology or schema graphs, where qualified property names may be needed to distinguish definitions.

Only predicates are rewritten. Subjects, objects, literals, blank nodes,
`rdf:type` assertions, RDF collections, and `fhir:index` values remain
unchanged. Already-short predicates and predicates outside the FHIR namespace
are left alone. Qualified names with nothing after the last dot are also
left alone. Running the update again makes no further changes. If two
triples become identical after shortening, they collapse to one RDF triple.

This does not fully transform FHIR R4 RDF data to the syntax required by FHIR R5+ RDF.
In particular, it does not change `fhir:value` to `fhir:v` or `fhir:link` to `fhir:l`, restore removed
type assertions or URI helpers, or convert indexed triples to RDF collections. Choice type
suffixes are preserved: `fhir:Patient.deceasedBoolean` becomes
`fhir:deceasedBoolean`, and `fhir:Extension.valueDateTime` becomes
`fhir:valueDateTime`. Those choices need additional transformation to R5+ syntax, which uses separate `rdf:type` assertions instead of property name suffixes.
The result is a short-property working graph, not a complete or necessarily conformant R4-to-R5/R6 conversion.
[ShEx (Shape Expressions)](https://shex.io/shex-semantics/) can validate RDF graph shapes after FHIR resources have been serialized as RDF.

### Quickstart: Validating FHIR RDF with ShEx

1. Choose your FHIR version, e.g. the latest build
2. Download the ShEx schema for that version: [https://build.fhir.org/fhir.shex](https://build.fhir.org/fhir.shex)
3. Download a supported ShEx validator, e.g. [Rudof](https://rudof-project.github.io/)
4. Load the ShEx schema to the validator
5. Validate FHIR RDF data


### What is being validated?

For a FHIR developer, a ShEx **shape** plays a role similar to the structural
constraints in a StructureDefinition: it describes the properties and values
expected at a node. The difference is that ShEx checks RDF edges and nodes,
including primitive wrappers and RDF collections, rather than a JSON or XML tree.
It is not a replacement serialization for a StructureDefinition.

| Check | What passing tells you | What it does not establish |
| --- | --- | --- |
| Turtle parsing | The document can be read as an RDF graph. | That the graph represents a valid FHIR resource. |
| ShEx validation | The selected node matches the selected shape in the supplied graph. | That every FHIR invariant, terminology binding, or profile rule has been checked. |
| FHIR validation | The resource satisfies the checks performed by the validator with its configured profiles and terminology support. | That a later RDF conversion or graph transformation preserved that resource correctly. |
| OWL consistency | Nothing validated. The supplied assertions and axioms are logically consistent under the chosen reasoning semantics. | That required fields are present or that the resource conforms to FHIR. |

### Interpreting failures

A completed ShEx validation reporting nonconformance means that the tested
node does not match the selected shape in the supplied graph. A parse error,
missing schema import, timeout, or unsupported schema feature is instead a
processing failure, not a conformance verdict.

Neither result by itself proves that the original FHIR resource is invalid.
Check the FHIR release, RDF conversion, focus node, schema version, and any
inference applied before classifying the error. For an apparent missing value,
also check whether the input graph is incomplete or the relevant triples were
loaded into a different named graph.

Keep the input resource identifier, selected shape, schema and tool versions,
and diagnostic path with the report. Distinguish a definite mismatch from an
unperformed check so that downstream users do not mistake a partial validation
for a complete FHIR conformance assessment.

### Tool compatibility

Testing and documenting ShEx 2.2-compatible tools remains a project TODO. Until
that work is complete, examples in this guide must identify the implementation
and version against which they were tested.


### ShEx and SHACL for FHIR RDF

Both languages can check node kinds, datatypes, allowed values, cardinalities,
and nested structures. ShEx describes a graph structure through a grammar of
triple constraints; SHACL applies a collection of constraints to targeted nodes.
For this guide, start with the release-matched FHIR ShEx schema when checking
the source RDF representation. SHACL can also describe that structure, or add
application-specific checks over a combined graph or a transformed query view.
Neither language automatically translates or enforces a StructureDefinition.

The distinctions below draw on [Comparing ShEx and SHACL, Chapter 7 of
*Validating RDF Data*](https://book.validatingrdf.com/bookHtml013.html).
That chapter describes the 2017 specifications. In particular, the recursion
comparison below refers to the 2017 SHACL Recommendation, not every later
extension or implementation.

| FHIR RDF concern | ShEx | SHACL |
| --- | --- | --- |
| Primitive wrappers and nested elements | Nested shapes describe each edge and intermediate node, such as `fhir:active` followed by `fhir:v`. | Nested node shapes can do the same; property paths can also reach a value directly, for example `sh:path ( fhir:active fhir:v )`. A path constraint alone does not validate the intermediate wrapper's structure. |
| Cardinalities | A triple constraint without a cardinality suffix requires exactly one matching triple. Optional and repeated elements need explicit cardinalities. | A property shape has no minimum or maximum count unless specified with `sh:minCount` and `sh:maxCount`. A datatype constraint alone does not require a value. |
| Recursive structures | Recursive shapes have defined semantics, useful for nested extensions and recursive list-cell shapes. | Recursive shape evaluation is unspecified in the 2017 Recommendation. Property paths cover some traversal needs, but recursive shape support and semantics must be checked for the chosen tool. |
| Repeated elements and slicing | Multiple triple constraints on the same predicate partition matching triples among those constraints. | Multiple property shapes on the same path apply conjunctively, not as separate slices. Qualified value shapes can count subsets, with explicit attention to overlap and disjointness. |
| Choice elements | Triple-expression alternatives can describe alternative graph structures. | `sh:xone` requires exactly one conforming alternative, but does not itself forbid partial data from another alternative. FHIR choice rules need explicit exclusions or other constraints matching the release's RDF encoding. |
| Additional triples | `CLOSED` rejects unlisted outgoing predicates; open shapes still constrain values of predicates they mention. | `sh:closed` allows predicates declared by directly attached property shapes with simple predicate paths, plus `sh:ignoredProperties`. Predicates mentioned only inside `sh:and` or complex paths are not automatically allowed. |
| Cross-field checks and reports | Structural matching is central; additional checks may require semantic actions or a separate validation step. Diagnostic detail depends on the implementation. | SHACL Core includes property-pair comparisons; SHACL-SPARQL supports custom checks and reusable constraint components. Standard RDF validation reports support diagnostic processing, though nested failure detail varies by tool. |

For R5 list-valued elements, distinguish the link to the list head from the
members reached through `rdf:rest*/rdf:first`. Counting the head does not count
FHIR repetitions. SHACL path counts count distinct value nodes, so a member
repeated in two list positions is still one path value. Checking member values
does not by itself establish list length, ordering, or a well-formed, terminating
list. Slicing checks in either language must operate on the correct members and
explicitly encode the profile's discriminators and slicing rules.

Target selection is also part of the validation contract. ShEx uses externally
selected node/shape pairs, often supplied as a shape map. SHACL can declare
`sh:targetClass fhir:Patient`, explicit target nodes, or targets based on subjects
or objects of a predicate. Class targets include instances of subclasses via
`rdfs:subClassOf` in the data graph; this does not imply full RDFS or OWL inference.
A conforming SHACL report can result when no intended resources were targeted,
so retain evidence of which resources were actually checked. For either language,
record the graph scope and any inference performed before validation.

In practice, use either or both languages according to the checks needed and
the schemas and tools available. For example, a SHACL-SPARQL rule could check
identifier uniqueness within a declared dataset scope, but that is an application
rule, not a universal FHIR requirement. Comparisons must reach primitive values
rather than compare wrapper nodes. Neither such a rule nor a structural shape
replaces FHIRPath invariant evaluation or terminology validation; keep those
checks explicit in the FHIR validation workflow.
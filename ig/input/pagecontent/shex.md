[ShEx (Shape Expressions)](https://shex.io/shex-semantics/) describes the RDF
properties and values expected at a node. Use it to check the graph structure
of FHIR RDF, including primitive wrappers, nested elements, and RDF collections.
See [Validation](validation.html) for the relationship to FHIR validation,
SHACL, and OWL consistency checking.

## Choose your FHIR version and syntax

These examples use FHIR R6 RDF and excerpts from the
[continuous-build FHIR ShEx schema](https://build.fhir.org/fhir.shex).
That schema identifies itself as ShEx 2.2 and uses `EXTENDS`; confirm that your
validator supports those features. The continuous build changes over time, so
retain the exact schema used for a validation run. For released data, use the
schema distributed with the matching FHIR release instead.

Validate the source RDF representation, retaining `fhir:v` primitive wrappers
and RDF collections. The flat, multi-valued derived graph used in later
[SPARQL examples](sparql.html) has a different structure and needs different
shapes. STU3 and R4 RDF also require release-matched schemas because their
property names and repeated-element encoding differ.

## Example FHIR ShEx

The excerpts below illustrate how the generated schema is organized; they are
**not a standalone schema**. Load the complete release-matched schema for
validation, including the referenced resource, datatype, and list shapes.
Relative shape labels such as `<Patient>` resolve against the schema's base
IRI, not the FHIR vocabulary namespace.

These prefix declarations apply to the excerpts:

```shex
PREFIX fhir:   <http://hl7.org/fhir/>
PREFIX fhirvs: <http://hl7.org/fhir/ValueSet/>
PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>
```

### Resource structure and inheritance

`<DomainResource>` extends `<Resource>` with narrative, contained resources,
and extensions. `EXTENDS` incorporates the base shape's constraints under the
schema's inheritance semantics; it is not an RDF subclass assertion.

```shex
<DomainResource> EXTENDS @<Resource> CLOSED {
  a [fhir:DomainResource]?;
  fhir:nodeRole [fhir:treeRoot]?;

  fhir:text @<Narrative>?;
  fhir:contained @<OneOrMore_Resource>?;
  fhir:extension @<OneOrMore_Extension>?;
  fhir:modifierExtension @<OneOrMore_Extension>?;
}
```

`@<Narrative>` refers to another shape. Square brackets describe a value set,
so `[fhir:treeRoot]` permits that IRI as a value. A `?` makes a triple constraint
optional, with at most one matching triple; without a cardinality suffix, a
triple constraint requires exactly one. `CLOSED` disallows outgoing predicates
not permitted by the shape, accounting for inherited constraints.

### Patient elements

The Patient shape combines inherited resource structure with patient-specific
elements. Primitive properties such as `fhir:active` point to wrapper nodes
validated by datatype shapes, not directly to literals. `OR` allows alternative
value shapes for choice elements such as `fhir:deceased`.

```shex
<Patient> EXTENDS @<DomainResource> CLOSED {
  a [fhir:Patient]?;
  fhir:nodeRole [fhir:treeRoot]?;

  fhir:identifier @<OneOrMore_Identifier>?;
  fhir:active @<Boolean>?;
  fhir:name @<OneOrMore_HumanName>?;
  fhir:telecom @<OneOrMore_ContactPoint>?;
  fhir:gender @<Code> AND { fhir:v @fhirvs:administrative-gender }?;
  fhir:birthDate @<Date>?;
  fhir:deceased @<Boolean> OR @<DateTime> ?;
  fhir:address @<OneOrMore_Address>?;
  fhir:maritalStatus @<CodeableConcept>?;
  fhir:multipleBirth @<Boolean> OR @<Integer> ?;
  fhir:photo @<OneOrMore_Attachment>?;
  fhir:contact @<OneOrMore_Patient.contact>?;
  fhir:communication @<OneOrMore_Patient.communication>?;
  fhir:generalPractitioner
    @<OneOrMore_Reference_Organization_OR_Practitioner_OR_PractitionerRole>?;
  fhir:managingOrganization @<Reference> AND { fhir:l @<Organization>? }?;
  fhir:link @<OneOrMore_Patient.link>?;
}
```

`fhir:name @<OneOrMore_HumanName>?` permits an absent name property or one link
to a nonempty RDF collection. The list shape checks the collection cells and
their HumanName members. The `?` counts links to the list head, not names in
the collection.

The optional `a [fhir:Patient]?` constraint is not a target-selection rule.
Select the intended patient node and the Patient shape explicitly; do not
assume that loading the schema validates every resource in the graph.

### Nested contact elements

Each member of the patient's contact collection is checked against
`<Patient.contact>`. This shape extends `<BackboneElement>` and describes the
contact's names, addresses, telecom details, and organization reference.

```shex
<Patient.contact> EXTENDS @<BackboneElement> CLOSED {
  fhir:relationship @<OneOrMore_CodeableConcept>?;
  fhir:role @<OneOrMore_CodeableConcept>?;
  fhir:name @<HumanName>?;
  fhir:additionalName @<OneOrMore_HumanName>?;
  fhir:telecom @<OneOrMore_ContactPoint>?;
  fhir:address @<Address>?;
  fhir:additionalAddress @<OneOrMore_Address>?;
  fhir:gender @<Code> AND { fhir:v @fhirvs:administrative-gender }?;
  fhir:organization @<Reference> AND { fhir:l @<Organization>? }?;
  fhir:period @<Period>?;
}
```

`AND` requires the same value node to satisfy both expressions. For
`fhir:organization`, the value must match `<Reference>` and the inline shape
constraining `fhir:l`. When that link is present, its target must match
`<Organization>` in the supplied graph. An IRI alone does not load the target
resource; make the required linked data available to the validator.

### Allowed code values

These named node constraints enumerate literal codes. In the gender
constraint above, `fhir:v` reaches the primitive value before applying the
administrative-gender value set. The link-type constraint is used by the
Patient link shape in the complete schema.

```shex
fhirvs:administrative-gender ["male" "female" "other" "unknown"]
fhirvs:link-type ["replaced-by" "replaces" "refer" "seealso"]
```

An enumerated value set checks the listed RDF terms. It does not provide
general terminology-service validation, subsumption testing, or automatic
enforcement of every FHIR terminology binding.

## How to use ShEx

### Prepare the graph and schema

1. Identify the FHIR release used to produce the RDF and obtain its complete
   ShEx schema. The [continuous-build schema](https://build.fhir.org/fhir.shex)
   is appropriate only when deliberately working with that build.
2. Choose a validator, for example [Rudof](https://rudof-project.github.io/),
   and check its support for the schema's ShEx version and features before
   attempting resource validation.
3. Parse the source Turtle and load the intended graph. Keep transformed query
   views separate, and record any inference or graph merging performed.
4. Load the schema with its original base IRI, or configure an explicit base
   consistently. Moving a schema to a local file can change how relative shape
   labels resolve.

### Select a node and shape

Supply a focus node and the resource shape to test, using the validator's
node/shape options or a [shape map](https://shex.io/shape-map/).
For a Patient, use the actual resource subject IRI from the data and the
resolved IRI of `<Patient>` from the loaded schema. The shape label is not
necessarily `http://hl7.org/fhir/Patient`.

For a dataset, enumerate the intended resource nodes and their corresponding
shapes. Record which node/shape pairs were evaluated, not just whether the
overall command succeeded. Confirm how the validator selects a named graph
or constructs a union graph; triples in another graph may otherwise be
unavailable to the check.

### Run validation and retain the result

Run the selected node/shape checks and retain the diagnostics. As a check of
the validation setup, test both a known conforming fixture and a deliberately
nonconforming variant, such as a boolean wrapper whose `fhir:v` is the string
`"not-a-boolean"`. Confirm that the latter fails at the expected constraint.

A passing result applies to the selected nodes, shapes, and graph only. Keep
FHIRPath invariants, profile-specific rules, and terminology checks explicit
in the wider [FHIR validation workflow](validation.html).

## Interpreting failures

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

## Tool compatibility

Testing and documenting ShEx 2.2-compatible tools remains a project TODO. The
excerpts on this page are explanatory, not a tested validator recipe. Before
adopting a tool, check schema parsing, inheritance, recursive list shapes,
node/shape selection, and diagnostic reporting against the chosen FHIR schema.
Any executable recipes added to this guide should identify the implementation
and version against which they were tested.

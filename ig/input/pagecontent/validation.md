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
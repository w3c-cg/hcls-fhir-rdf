FHIR RDF supports graph queries and ontology-based analysis while preserving links between data and terminologies.
It is especially useful for data analysis involving complex relationships, combinations, aggregates, external data or terminologies.

SPARQL expresses patterns to find in a graph. OWL defines reusable logic that reasoners can use to derive additional classifications.
You can use SPARQL without OWL and vice versa.

| Goal | Starting point |
| --- | --- |
| Query resources by their values or follow references between them | [SPARQL](sparql.html) |
| Classify resources using reusable logical definitions and terminology hierarchies | [OWL](owl.html) |
| Check whether a resource has the required RDF structure | [ShEx](validation.html) |


### SPARQL

[SPARQL examples](sparql.html) cover patient names, observations, blood pressure components, data filters, date ranges, and reference traversal.
These also introduce context graphs and compare SPARQL with SQL-on-FHIR.

### OWL

[OWL examples](owl.html) show how to define and reuse query logic through classification. 
These also explain expected graph shapes, terminology integrations, and levels of reasoning support.

### Pre-processing

See [SPARQL Query Artifacts](query-artifacts.html) for downloadable SPARQL queries/updates that can be used to pre-process FHIR RDF data to make it easier to query and reason over.

### Relationship to the FHIR REST API

FHIR RDF is a representation of resources, not a replacement for the [FHIR REST API](https://hl7.org/fhirhttp.html). 
Its strongest feature is making data relationships and terminology knowledge queryable together.

There are three ways to use FHIR RDF with FHIR REST APIs:

| Approach | What RDF/Turtle contributes |
| --- | --- |
| Exchange FHIR Turtle through REST | A REST API server that supports Turtle can return the same resource using `Accept: application/fhir+turtle`. This changes the representation, not the interaction or search semantics. |
| Maintain an analytical graph alongside a FHIR server | Convert FHIR data to Turtle and store in a triple store for SPARQL querying separately from a REST API.
| Implement a FHIR server using a graph backend | Translate supported FHIR REST searches and operations into SPARQL and reasoning tasks internally, then return standard FHIR responses. Clients can continue to use JSON or XML without knowing the backend uses RDF. |

For an existing deployment, a separate analytical graph is the simplest way to start.
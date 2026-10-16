Semantic Web for FHIR collects practical patterns, queries, scripts, and techniques for using [FHIR RDF](https://hl7.org/fhir/rdf.html).

This implementation guide is developed by the RDF subgroup of the [HL7 ITS Working Group](https://www.hl7.org/Special/committees/xml/index.cfm) and the [W3C Semantic Web in Health Care and Life Sciences (HCLS) Community Group](https://www.w3.org/community/hclscg/).

<div class="stu-note" markdown="1">
This guide is entirely optional and informative (non-normative). FHIR RDF is defined by the core
FHIR specification and this guide is not a prerequisite for using it.
</div>

### Purpose

[FHIR RDF](http://hl7.org/fhir/rdf.html) is designed for using FHIR as a graph data model, especially to better integrate with non-FHIR data and systems.
FHIR is based on web technologies like URIs, HTTP, OAuth, XML, JSON, and [RDF](https://www.w3.org/TR/rdf-primer/). 
[Semantic Web](https://www.w3.org/RDF/FAQ) technologies like RDF enable FHIR data to be further operationalized with less dependencies on FHIR-specific software.

This implementation guide helps FHIR developers apply [RDF](rdf.html), [SPARQL](sparql.html), [ShEx](validation.html), [OWL](owl.html), and
related Semantic Web technologies to FHIR data. It complements the core specification with suggested application patterns, pre-processing arifacts, and examples.
No specific profiles are included. FHIR RDF works with any FHIR profiles.

Users who are familiar with RDF and OWL, but maybe not familiar with FHIR, are encouraged to read [FHIR OWL Ontology Guide W3C Community Group Report](https://w3c-cg.github.io/hcls-fhir-rdf/spec/ontology.html).

### Quick Start

FHIR RDF uses [RDF Turtle](https://www.w3.org/TR/turtle/) (Terse Triple Language) as its canonical serialization format, sometimes referred to as "FHIR Turtle".

1. **Pick your FHIR version** FHIR RDF is defined for FHIR versions STU3, R4, R4B, R5, and R6.
	Make sure your FHIR data version matches the tools, schemas, and ontologies you want to use.
2. **Get FHIR RDF.** Convert FHIR data 
	with the [HAPI/HL7 FHIR Java reference libraries](https://github.com/hapifhir/org.hl7.fhir.core) to Turtle, 
	or [download examples](https://hl7.org/fhir/downloads.html) from the core spec.
	See [FHIR RDF](fhir-rdf.html) for an introduction to FHIR RDF.
3. **Query with SPARQL.** Load the FHIR RDF data into [Apache Jena ARQ](https://jena.apache.org/documentation/query/) 
	and run the [example queries](rdf.html#sparql-match-graph-patterns).
	No server or OWL reasoner is required. See [Analytics](analytics.html) for more query patterns.
4. **Validate with ShEx.**  [Download the FHIR ShEx schemas](https://hl7.org/fhir/fhir.shex) and use
	[Apache Jena](https://jena.apache.org/documentation/shex/) or [Rudof](https://rudof-project.github.io/) to validate the FHIR RDF data.
	Check schema and tool feature compatibility. ShEx does not implement all [FHIR validation tasks](http://hl7.org/fhir/validation.html).
	See [Validation](validation.html#quickstart-validating-fhir-rdf-with-shex) for recommended workflows.
5. **Reason with OWL.** [Download the FHIR OWL ontology](https://hl7.org/fhir/fhir.ttl) 
	and [prepare](owl.html#prerequisites-for-reasoning) your data for use with an OWL reasoner like [HermiT](https://github.com/owlcs/hermit-reasoner) or [ELK](https://github.com/liveontologies/elk-reasoner).


### Scope

This guide focuses on application patterns. It does not redefine the FHIR RDF
serialization, introduce profiles, or replace the FHIR validator. Examples are
designed to be copied, tested, and adapted.

| In scope | Out of scope |
| --- | --- |
| SPARQL, graph modeling, ShEx, OWL, R2RML, and operational guidance | New FHIR resources, profiles, extensions, or normative serialization rules |

### Using this guide

Start with [FHIR RDF](fhir-rdf.html), or [RDF](rdf.html) for more background, use the [Analytics](analytics.html) examples
to query graphs, and consult pages like [Validation](validation.html) for specialized workflows.

---

HL7®, and FHIR® are the registered trademarks of Health Level Seven International and their use of these trademarks does not constitute an endorsement by HL7.

This IG is published by the W3C Semantic Web in Health Care and Life Sciences Community Group. It is not a W3C Standard nor is it on the W3C Standards Track. 

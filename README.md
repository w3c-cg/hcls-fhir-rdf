# hcls-fhir-rdf
This repo is maintained by the [RDF subgroup of the HL7 ITS group](https://confluence.hl7.org/pages/viewpage.action?pageId=66922543), and is mostly used for tracking FHIR RDF [issues](https://github.com/w3c-cg/hcls-fhir-rdf/issues).

## State of affairs
RDF (Turtle) is one of three official FHIR serialization formats, along with JSON and XML.  
Both JSON and RDF (Turtle)
examples are generated from XML master examples during the FHIR spec build process.

The [FHIR publishing process on github](https://github.com/HL7/fhir) generates:
* a ShEx schema for all core FHIR resources,
* a fairly minimal FHIR ontology; and
* RDF serializations for all of the FHIR examples in the FHIR specification.   

<span color="red">**The archived RDF and ShEx generation code in this repository is no longer maintained. See documentation below for an overview of the current generation process.**</span>

## Resources
* [FHIR RDF Overview](https://www.hl7.org/fhir/rdf.html)
* [Ontology](https://www.hl7.org/fhir/fhir.ttl)
* [RDF Downloads](https://www.hl7.org/fhir/downloads.html)

---------


## Directories of this repo
Still in use:
* scripts -- Convenience scripts for transforming FHIR RDF data
* docs/utg -- IRI stems
* spec -- W3C report documents
* ig -- Supplemental Implementation Guide

## RDF, ShEx, and OWL Generation
* [HL7/fhir: Official source for the HL7 FHIR Specification](https://github.com/HL7/fhir)
  * Publishes the FHIR specification and its artifacts
  * Uses Kindling for generating all artifact serializations

* [HL7/kindling: FHIR Publisher](https://github.com/HL7/kindling)
  * Uses org.hl7.fhir.core for parsing and serialization
  * [Publisher.java](https://github.com/HL7/kindling/blob/main/src/main/java/org/hl7/fhir/tools/publisher/Publisher.java) - CLI entry point, builds artifacts for publishing
  * [FhirTurtleGenerator.java](https://github.com/HL7/kindling/blob/main/src/main/java/org/hl7/fhir/definitions/generators/specification/FhirTurtleGenerator.java) - builds the ontology
  * [TurtleSpecGenerator.java](https://github.com/HL7/kindling/blob/main/src/main/java/org/hl7/fhir/definitions/generators/specification/TurtleSpecGenerator.java) - builds pseudo-turtle templates for each resource, e.g [Patient - FHIR v5.0.0](https://www.hl7.org/fhir/patient.html#tabs-ttl)

* [org.hl7.fhir.core](https://github.com/hapifhir/org.hl7.fhir.core)
  * [TurtleParser.java](https://github.com/hapifhir/org.hl7.fhir.core/blob/master/org.hl7.fhir.r5/src/main/java/org/hl7/fhir/r5/elementmodel/TurtleParser.java) - serializes R5+ resources
  * [RdfParser.java](https://github.com/hapifhir/org.hl7.fhir.core/blob/master/org.hl7.fhir.r4/src/main/java/org/hl7/fhir/r4/formats/RdfParser.java) - serializes resource versions prior to R5
  * [ShExGenerator.java](https://github.com/hapifhir/org.hl7.fhir.core/blob/master/org.hl7.fhir.r5/src/main/java/org/hl7/fhir/r5/conformance/ShExGenerator.java) - serializes ShEx schemas
  * [TurtleGeneratorTests.java](https://github.com/hapifhir/org.hl7.fhir.core/blob/master/org.hl7.fhir.r5/src/test/java/org/hl7/fhir/r5/test/TurtleGeneratorTests.java) - Unit tests and convenience methods for serializing R5+ resources
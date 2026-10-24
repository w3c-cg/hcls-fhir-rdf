[OWL (Web Ontology Language)](https://www.w3.org/TR/owl2-overview/) is a W3C standard language designed for knowledge representation. 
The FHIR OWL Ontology defines the logical vocabulary of terms used in FHIR RDF data and functions as a descriptive, axiomatic model of FHIR resources.
OWL can be used with or without the FHIR OWL ontology for automated inference on FHIR data or concepts, such as for defining reusable query logic.

For extended documentation on the [FHIR OWL Ontology](https://build.fhir.org/rdf.html#owl-shex-rim), see the [Guide to the FHIR OWL Ontology W3C Community Group Report](https://w3c-cg.github.io/hcls-fhir-rdf/spec/ontology.html).

## Prerequisites for reasoning with FHIR RDF
1. (Required) Convert [RDF collections](https://www.w3.org/TR/rdf11-mt/#rdf-collections) to indexed triples (extra `fhir:index` properties to track order) for internal use with OWL 2 reasoners. OWL 2 does not generally support RDF collections because these are reserved for encoding OWL axioms in RDF. FHIR R5+ still requires RDF collections for conformant data exchange.

2. (Required) Convert unsupported data types like `xsd:date` to supported data types like `xsd:dateTime` or `time:DateTimeDescription`, but avoid relying on any invented precision.

3. (Optional) Convert blank nodes to [named](https://www.w3.org/TR/rdf11-concepts/#section-skolemization) IRIs to make OWL reasoning easier to follow.


### Examples

See [Guide to the FHIR OWL Ontology - Analytics](https://w3c-cg.github.io/hcls-fhir-rdf/spec/ontology.html#analytics) for examples for now.

## FHIR and Ontologies
FHIR can generally be used with ontologies independently of FHIR RDF. 
FHIR data coded with ontology terms might be used with a [FHIR terminology service](https://hl7.org/fhir/terminology-service.html) to perform operations with the terms.
The terminology service could use either OWL, FHIR CodeSystems, or other technologies behind-the-scenes.

## OWL on FHIR
OWL ontogies can also be transformed to FHIR CodeSystems, for use in terminology services. See [OWL to FHIR Transformer](https://github.com/aehrc/fhir-owl).
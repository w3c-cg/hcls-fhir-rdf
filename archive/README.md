# HCLS-FHIR-RDF Archive
This directory contains obsolete data and code from previous FHIR RDF versions.
* archive/data - old version of FHIR specification
  * examples -- XML examples from specification
  * site -- FHIR definitions (we use the json format)
  * rdf -- RDF representation of examples
  * definitions.shex -- shex definitions of FHIR content
  * definitions.xml -- XML definitions used in xslt transformation
  * extract.log -- log of build for the data directory
* archive/python/hcls_fhir_rdf -- python 3 modules for building data directory
* archive/python/hcls_fhir_rdf/tests -- python unit tests (not a lot at the moment)
* archive/owl/ontology -- early work on modeling FHIR definitions in OWL
* archive/xsl -- XSLT 2.0 transform for converting FHIR instances from XML to RDF
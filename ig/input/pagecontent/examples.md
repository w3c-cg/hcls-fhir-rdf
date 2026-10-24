Example implementations of FHIR RDF applications with SPARQL, ShEx, and OWL.

### Demos
[FHIR-Ontop-OMOP](https://github.com/fhircat/FHIROntopOMOP/) is an example application which dynamically rewrites FHIR SPARQL queries to SQL queries on OMOP relational data.

[FHIR RDF Playground](https://fhircat.github.io/fhir-rdf-playground/) is handy browser-based tool for experimenting with conversion of FHIR JSON to FHIR RDF. It is not for production use.


### Presentations

[Slides by Eric Prud'hommeaux](https://www.w3.org/2016/Talks/1205-swat4ls-egp/reasoning) illustrate how inference can be used on FHIR RDF data to perform a query for FHIR Observations of rheumatoid arthritis, using the [SNOMED-CT ontology](https://www.nlm.nih.gov/healthit/snomedct/international.html). Caveat: These slides were based on an earlier version of FHIR, so some specifics might have changed.

A [webinar by Harold Solbrig](https://yosemiteproject.org/fhir-rdf-as-a-bridge-to-the-semantic-web-in-healthcare/) shows how FHIR RDF data can be used with the [SNOMED-CT ontology](https://www.nlm.nih.gov/healthit/snomedct/international.html) to recognize a cancer diagnosis.  An [associated tutorial](https://yosemiteproject.org/tutorial-fhir-rdf-as-a-bridge-to-the-semantic-web-in-healthcare/) details the steps used. Caveat: This webinar and tutorial were based on a previous version of FHIR RDF, so some specifics might have changed.


### Selected publications about FHIR & RDF

#### 2026
Platt, S. K., Hier, D. B., Yadamsuren, B., Nguyen, A. N., & Hartzell, V. (2026). Transforming FHIR into an OWL Knowledge Graph for Schema-Grounded Natural-Language Querying and Exploratory Data Analysis. Applied Sciences, 16(8), 3936. [https://doi.org/10.3390/app16083936](https://doi.org/10.3390/app16083936)

Prudhomme, T. (2026). Guide to the FHIR OWL Ontology. W3C Community Group Report. [https://w3c-cg.github.io/hcls-fhir-rdf/spec/ontology.html](https://w3c-cg.github.io/hcls-fhir-rdf/spec/ontology.html)

#### 2025
Ansari, A., Conte, M., Flynn, A., & Paturkar, A. (2025). A prototype ETL pipeline that uses HL7 FHIR RDF resources when deploying pure functions to enrich knowledge graph patient data. *Journal of biomedical semantics*, *16*(1), 16. [https://doi.org/10.1186/s13326-025-00335-4](https://doi.org/10.1186/s13326-025-00335-4)

Prud'hommeaux, E., Bolleman, J. T., & Nanjo, C. (2025). Treating Query Graphs As Connecting Trees Mapping SPARQL Queries to FHIR API. In SWAT4HCLS (pp. 67-74). [https://ceur-ws.org/Vol-4196/paper_38.pdf](https://ceur-ws.org/Vol-4196/paper_38.pdf)

#### 2024
Zhang, S., Cornet, R. & Benis, N. Cross-Standard Health Data Harmonization using Semantics of Data Elements. *Sci Data* **11**, 1407 (2024). [https://doi.org/10.1038/s41597-024-04168-1](https://doi.org/10.1038/s41597-024-04168-1)

#### 2023
Sharma, D. K., Prud'hommeaux, E., Booth, D., Nanjo, C., & Jiang, G. (2023). Shape expressions (ShEx) schemas for the FHIR R5 specification. Journal of biomedical informatics, 148, 104534. [https://doi.org/10.1016/j.jbi.2023.104534](https://doi.org/10.1016/j.jbi.2023.104534)

#### 2022
Xiao, G., Pfaff, E., Prud'hommeaux, E., Booth, D., Sharma, D. K., Huo, N., Yu, Y., Zong, N., Ruddy, K. J., Chute, C. G., & Jiang, G. (2022). FHIR-Ontop-OMOP: Building clinical knowledge graphs in FHIR RDF with the OMOP Common data Model. *Journal of biomedical informatics*, *134*, 104201. [https://doi.org/10.1016/j.jbi.2022.104201](https://doi.org/10.1016/j.jbi.2022.104201)

Sharma, D. K., Prud'Hommeaux, E., Booth, D., Peterson, K. J., Stone, D. J., Solbrig, H., ... & Jiang, G. (2022). Implementing a New FHIR RDF Specification for Semantic Clinical Data Using a JSON-LD-based Approach. In SWAT4HCLS (pp. 82-86). [https://ceur-ws.org/Vol-3127/paper-10.pdf](https://ceur-ws.org/Vol-3127/paper-10.pdf)

#### 2021
Prud'hommeaux, E., Collins, J., Booth, D., Peterson, K. J., Solbrig, H. R., & Jiang, G. (2021). Development of a FHIR RDF data transformation and validation framework and its evaluation. *Journal of biomedical informatics*, *117*, 103755. [https://doi.org/10.1016/j.jbi.2021.103755](https://doi.org/10.1016/j.jbi.2021.103755)

#### 2019
Arguello-Casteleiro M, Martinez-Costa C, Des-Diz J, Maroto N, Fernandez-Prieto MJ, Stevens R. From SNOMED CT expressions to an FHIR RDF representation: exploring the benefits of an ontology-based approach. Proceedings of the Joint Ontology Workshops 2019 Episode V: The Styrian Autumn of Ontology; CEUR '19; September 23-25, 2019; Graz, Austria. 2019. pp. 1–13. [https://ceur-ws.org/Vol-2518/paper-ODLS1.pdf](https://ceur-ws.org/Vol-2518/paper-ODLS1.pdf)

#### 2017
Solbrig, H. R., Prud’hommeaux, E., & Jiang, G. (2017). Blending FHIR RDF and OWL. *CEUR Workshop Proceedings*, *2042*. [https://ceur-ws.org/Vol-2042/paper28.pdf](https://ceur-ws.org/Vol-2042/paper28.pdf)

Solbrig, H. R., Prud'hommeaux, E., Grieve, G., McKenzie, L., Mandel, J. C., Sharma, D. K., & Jiang, G. (2017). Modeling and validating HL7 FHIR profiles using semantic web Shape Expressions (ShEx). *Journal of biomedical informatics*, *67*, 90–100. [https://doi.org/10.1016/j.jbi.2017.02.009](https://doi.org/10.1016/j.jbi.2017.02.009)

Jiang, G., Xiao, G., Kiefer, R.C., Prud'hommeaux, E., & Solbrig, H.R. (2017). Building an FHIR Ontology based Data Access Framework with the OHDSI Data Repositories. *American Medical Informatics Association Annual Symposium*. [https://ghxiao.org/publications/2017-aiam-obda-fhir-ohdsi.pdf](https://ghxiao.org/publications/2017-aiam-obda-fhir-ohdsi.pdf)

#### 2013
Booth, D. (2013). RDF as a Universal Healthcare Exchange Language: Realistically Achieving Semantic Interoperability. [http://dbooth.org/2013/rdf-as-univ/rdf-as-univ.pdf](http://dbooth.org/2013/rdf-as-univ/rdf-as-univ.pdf)
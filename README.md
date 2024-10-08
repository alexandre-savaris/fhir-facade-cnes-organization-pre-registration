# fhir-facade-cnes-organization-pre-registration
A [FHIR®](https://hl7.org/fhir/R4/index.html) facade for accessing pre-registration data from Brazil's National Registry of Health Facilities ([CNES](https://cnes.datasus.gov.br/) - Cadastro Nacional de Estabelecimentos de Saúde).

The facade accesses a [SOAP webservice](https://datasus.saude.gov.br/wp-content/uploads/2019/12/Especificacao-Tecnica-para-Integracao-com-o-Cadastro-Nacional-de-Estabelecimentos-de-Sa%C3%BAde.pdf) provided by the Department of Informatics of the Brazilian National Health System ([DATASUS](https://datasus.saude.gov.br/)), using its responses to build FHIR® R4 instances and bundles for the [`Organization`](https://hl7.org/fhir/r4/organization.html) resource.

The complementary Extensions and NamingSystems used by the project can be found in the [related project](https://simplifier.net/fhir-facade-cnes-organization/~introduction) on [SIMPLIFIER.NET](https://simplifier.net/).

[Java](https://www.oracle.com/br/java/) 17  
[HAPI FHIR](https://hapifhir.io/hapi-fhir/) 7.4.0 (released under the [Apache 2.0](https://hapifhir.io/hapi-fhir/license.html) Licence)  
[Apache Maven](https://maven.apache.org/) 3.9.6

## Usage

To run the REST server using the Jetty Maven Plugin (from the project's root directory):
1. Create dirs `/data` and `/logs` (`C:\data` and `C:\logs` on Windows)
2. `mvn jetty:run`

To run the REST server using Docker (from the project's root directory):
1. `mvn clean package`
2. `docker build -t fhir-facade-cnes-organization-pre-registration .`
3. `docker run -p 8080:8080 fhir-facade-cnes-organization-pre-registration`

Using a REST client, execute a GET request to the following URL: http://localhost:8080/fhir/Organization/{9999999}
- Replace the placeholder `{9999999}` with a valid CNES number. (You can search for valid CNES numbers [here](https://cnes.datasus.gov.br/pages/estabelecimentos/consulta.jsp)).
- Use the Bearer token `0191bd3a-bd52-7f78-918f-8492aeace62e` for authentication.

Using a REST client, execute a GET request to the following URL: http://localhost:8080/fhir/Organization?identifier=urn:oid:2.16.840.1.113883.13.36|{9999999}&preRegistrationSituation=https://alexandresavaris.org/fhir/r4/NamingSystem/cnes/SituacaoPreCadastro|{?}
- Replace the placeholder `{9999999}` with a valid CNES number. (You can search for valid CNES numbers [here](https://cnes.datasus.gov.br/pages/estabelecimentos/consulta.jsp)).
- Replace the placeholder `{?}` with a valid code for the Organization's Pre-registration Situation. (The expected codes can be found in the [Official documentation](https://datasus.saude.gov.br/wp-content/uploads/2019/12/Especificacao-Tecnica-para-Integracao-com-o-Cadastro-Nacional-de-Estabelecimentos-de-Sa%C3%BAde.pdf)).
- Use the Bearer token `0191bd3a-bd52-7f78-918f-8492aeace62e` for authentication.

## Notes
To access the `search` endpoint running on Tomcat, the server must be configured to relax the use of specific characters on URLs, as follows:
1. Edit the `$CATALINA_HOME/conf/server.xml`;
2. on the active `Connector`, add the following attribute:  
`relaxedQueryChars='^{}[]|&quot;'`  

(Source: https://stackoverflow.com/questions/50361171/how-to-allow-character-in-urls-for-tomcat-8-5)

The facade accesses the homologation instance of the SOAP webservice, whose data are available for __tests use only__. 

## Release Notes

### 1.1.0

Added `SearchParameter` for Pre-registration situation.

### 1.0.0

Initial release.

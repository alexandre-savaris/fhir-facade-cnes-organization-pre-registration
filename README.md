# fhir-facade-cnes-organization-pre-registration
A [FHIR®](https://hl7.org/fhir/R4/index.html) facade for accessing pre-registration data from Brazil's National Registry of Health Facilities ([CNES](https://cnes.datasus.gov.br/) - Cadastro Nacional de Estabelecimentos de Saúde).

## Notes
To access the `search` endpoint running on Tomcat, the server must be configured to relax the use of specific chars on URLs, as follows:
1. Edit the `$CATALINA_HOME/conf/server.xml`;
2. on the active `Connector`, add the following attribute:  
`relaxedQueryChars='^{}[]|&quot;'`  

(Source: https://stackoverflow.com/questions/50361171/how-to-allow-character-in-urls-for-tomcat-8-5)
package org.alexandresavaris.fhir.facade.cnes.organization.preregistration.provider;

import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.interceptor.ServerInterceptorUtil;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.model.OrganizationCnesPreRegistration;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.util.NamespaceContextMap;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.util.Utils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Definition of a ResourceProvider for managing instances of the
 * <code>OrganizationCnesPreRegistration</code> class.
 */
public class OrganizationCnesPreRegistrationResourceProvider
    implements IResourceProvider {
    // Endpoint for accessing the SOAP webservice.
    private String endpointEstabelecimentoSaudeService = null;
    // Username and password for the service.
    private String username;
    private String password;
    // The content of the SOAP envelope to be sent to the webservice.
    private String contentOfSoapEnvelope = null;
    // Code snippets for filtering.
    private String codeSnippetForFilteringByCnes = null;

    // The empty constructor.
    public OrganizationCnesPreRegistrationResourceProvider() {
    }
    
    // The parameterized constructor.
    public OrganizationCnesPreRegistrationResourceProvider(
        String endpointEstabelecimentoSaudeService,
        String username,
        String password,
        String soapEnvelopeContent,
        String cnesFilter) {
        
        this.endpointEstabelecimentoSaudeService
            = endpointEstabelecimentoSaudeService;
        this.username = username;
        this.password = password;
        this.contentOfSoapEnvelope = soapEnvelopeContent;
        this.codeSnippetForFilteringByCnes = cnesFilter;
    }

    /**
     * The getResourceType method comes from IResourceProvider, and must be
     * overridden to indicate what type of resource this provider supplies.
     * @return class representation for OrganizationCnesPreRegistration.
     */
    @Override
    public Class<OrganizationCnesPreRegistration> getResourceType() {
       
        return OrganizationCnesPreRegistration.class;
    }

    /**
     * The "@Read" annotation indicates that this method supports the
     * read operation. It takes one argument, the Resource type being returned.
     *
     * @param theId The read operation takes one parameter, which must be
     * of type IdDt and must be annotated with the "@Read.IdParam" annotation.
     * @param theRequestDetails Details from the performed request.
     * @param theInterceptorBroadcaster For broadcasting the related event to
     * known interceptors.
     * @return Returns a resource matching this identifier, or null if none
     * exists.
     */
    @Read(type = OrganizationCnesPreRegistration.class)
    public OrganizationCnesPreRegistration getResourceById(
        @IdParam IdType theId,
        RequestDetails theRequestDetails,
        IInterceptorBroadcaster theInterceptorBroadcaster) {
        // The resource instance to be returned.
        OrganizationCnesPreRegistration retVal = null;
        
        try {

            String snippetFilter = MessageFormat.format(
                this.codeSnippetForFilteringByCnes, theId.getIdPart()
            );
            String updatedContentOfSoapEnvelope = MessageFormat.format(
                this.contentOfSoapEnvelope, username, password, snippetFilter, ""
            );
            
            // Access the SOAP webservice.
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(this.endpointEstabelecimentoSaudeService))
                .version(HttpClient.Version.HTTP_2)
                .POST(HttpRequest.BodyPublishers.ofString(
                    updatedContentOfSoapEnvelope)
                )
                .header("Content-Type", "application/soap+xml")
                .header("charset", "UTF-8")
                .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
            
            // To guarantee that namespace prefixes are unique throughout the
            // whole document, replace known duplicate values with arbitrary
            // ones.
            String responseBody = response.body()
                .replace(
                    "tip:codigoTipoTelefone xmlns:tip",
                    "tipTel:codigoTipoTelefone xmlns:tipTel"
                )
                .replace(
                    "tip:descricaoTipoTelefone xmlns:tip",
                    "tipTel:descricaoTipoTelefone xmlns:tipTel"
                )
                .replace(
                    "tip:codigoTipoTelefone",
                    "tipTel:codigoTipoTelefone"
                )
                .replace(
                    "tip:descricaoTipoTelefone",
                    "tipTel:descricaoTipoTelefone"
                );
            
            // For debugging purposes.
            // Uncomment to see the response body from the SOAP Webservice.
            System.out.println(
                "----------------------------------------------------------"
            );
            System.out.println(response.statusCode());
            System.out.println(responseBody);
            System.out.println(
                "----------------------------------------------------------"
            );

            int responseStatusCode = response.statusCode();
            if (responseStatusCode != 200) {
                // TODO: verify why the message used for the exception doesn't
                // appear in the OperationOutcome instance.
                throw new InternalErrorException(
                    "O webservice SOAP retornou o código de status HTTP "
                        + responseStatusCode
                        + " ao acessar os dados do estabelecimento de saúde com o Id: "
                        + theId);
            }
            
            // Fill in the resource with data from the response.
            retVal = new OrganizationCnesPreRegistration();
            
            // The logical ID replicates the Organization's CNES.
            retVal.setId(theId);
            
            // XML parsing for content extraction.
            DocumentBuilderFactory factory
                = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document
                = builder.parse(
                    new InputSource(new StringReader(responseBody))
                );
            
            // Setting namespaces for using XPath.
            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();
            NamespaceContext context = new NamespaceContextMap(
                Utils.xmlNamespaces);
            xpath.setNamespaceContext(context);

            // CodigoCNES -> Identifier: CNES.
            String cnes
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("cnes"),
                    0
                );
            if (cnes != null) {
                retVal.addIdentifier()
                    .setSystem("urn:oid:" + Utils.oids.get("cnes"))
                    .setValue(cnes);
            }

            // numeroCNPJ -> Identifier: CNPJ.
            String cnpj
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("cnpj"),
                    0
                );
            if (cnpj != null) {
                retVal.addIdentifier()
                    .setSystem(Utils.namingSystems.get("cnpj"))
                    .setValue(cnpj);
            }

            // nomeFantasia -> name.
            String name
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("name"),
                    0
                );
            if (name != null) {
                retVal.setName(name);
            }

            // nomeEmpresarial -> alias.
            String alias
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("alias"),
                    0
                );
            if (alias != null) {
                retVal.addAlias(alias);
            }
            
            // Endereco -> Address.
            String street = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get("street"),
                0
            );
            String number = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get("number"),
                0
            );
            String neighborhood = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get("neighborhood"),
                0
            );
            String city = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get("city"),
                0
            );
            String state = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get("state"),
                0
            );
            String postalCode = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get("postalCode"),
                0
            );
            String cityCodeIbge
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("cityCodeIbge"),
                    0
                );
            if (street != null || number != null || neighborhood != null
                    || city != null || state != null || postalCode != null
                    || cityCodeIbge != null) {

                String addressTextTemplate = "{0}, {1} - {2} - {3} - {4}";
                String addressText = java.text.MessageFormat.format(
                    addressTextTemplate,
                    (street != null ? street : "<SEM LOGRADOURO>"),
                    (number != null
                        ? number
                        : new String(
                            "<SEM NÙMERO>".getBytes("ISO-8859-1"), "UTF-8")
                        ),
                    (neighborhood != null ? neighborhood : "<SEM BAIRRO>"),
                    (city != null ? city : "<SEM CIDADE>"),
                    (state != null ? state : "<SEM UF>")
                );
                
                Address address = new Address()
                    .setUse(Address.AddressUse.WORK)
                    .setType(Address.AddressType.BOTH)
                    .setText(addressText)
                    .setCity(city)
                    .setState(state)
                    .setPostalCode(postalCode)
                    .setCountry("BRA");

                // Extensions for IBGE codes.
                if (cityCodeIbge != null) {
                    Extension cityCodeIbgeExtension
                        = new Extension(Utils.extensions.get("cityCodeIbge"));
                    cityCodeIbgeExtension.setValue(
                        new Coding()
                            .setSystem("urn:oid:" + Utils.oids.get("ibgeCode"))
                            .setCode(cityCodeIbge)
                            .setDisplay(
                                new String(
                                    "Código do município no IBGE"
                                        .getBytes("ISO-8859-1"),
                                    "UTF-8"
                                )
                            )
                    );
                    address.addExtension(cityCodeIbgeExtension);
                }

                retVal.addAddress(address);
                
            }
            
            // dataAtualizacao -> Extension (update date).
            String updateDate
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("updateDate"),
                    0
                );
            if (updateDate != null) {
                retVal.setUpdateDate(
                    new DateType(updateDate)
                );
            }

            // tipoNaturezaJuridica -> Extension (Legal Nature Category).
            String legalNatureCategory
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("legalNatureCategory"),
                    0
                );
            String legalNatureCategoryDisplay
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("legalNatureCategoryDisplay"),
                    0
                );
            if (legalNatureCategory != null
                || legalNatureCategoryDisplay != null) {
                retVal.setLegalNatureCategory(
                    new Coding()
                        .setSystem(
                            Utils.namingSystems.get("categoriaNaturezaJuridica")
                        )
                        .setCode(legalNatureCategory)
                        .setDisplay(legalNatureCategoryDisplay)
                );
            }

            // codigoNaturezaJuridicaConcla -> Extension (Legal Nature Code).
            String legalNatureCode
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("legalNatureCode"),
                    0
                );
            String legalNatureCodeDisplay
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("legalNatureCodeDisplay"),
                    0
                );
            if (legalNatureCode != null
                || legalNatureCodeDisplay != null) {
                
                // The correct format for the Legal Nature Code, according to
                // the Brazilian Institute of Geography and Statistics (IBGE).
                String formattedLegalNatureCode
                    = (legalNatureCode != null
                        ? legalNatureCode.substring(0, 3)
                            + "-"
                            + legalNatureCode.substring(3)
                        : legalNatureCode);
                
                retVal.setLegalNatureCode(
                    new Coding()
                        .setSystem(
                            Utils.namingSystems.get("codigoNaturezaJuridica")
                        )
                        .setCode(formattedLegalNatureCode)
                        .setDisplay(legalNatureCodeDisplay)
                );
            }
            
            // CNPJMantenedora -> Extension (Maintainer's CNPJ).
            String maintainerCnpj
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("maintainerCnpj"),
                    0
                );
            if (maintainerCnpj != null) {
                retVal.setMaintainerCnpj(
                    new Coding()
                        .setSystem(Utils.namingSystems.get("cnpj"))
                        .setCode(maintainerCnpj)
                        .setDisplay(
                            new String(
                                "Número do CNPJ da mantenedora"
                                    .getBytes("ISO-8859-1"),
                                "UTF-8"
                            )
                        )
                );
            }

            // Phones -> contact
            int phoneCount = countNodesFromXml(document, xpath,
                Utils.xpathExpressions.get("phones"));
            for (int i = 0; i < phoneCount; i++) {
                String phoneNumber
                    = extractSingleValueFromXml(document, xpath,
                        Utils.xpathExpressions.get("phoneNumber"),
                        i
                    );
                if (phoneNumber != null) {
                    String phoneTemplate = "{0} {1}";
                    String phone = java.text.MessageFormat.format(
                        phoneTemplate,
                        extractSingleValueFromXml(document, xpath,
                            Utils.xpathExpressions.get("phoneAreaCode"),
                            i
                        ),
                        phoneNumber
                    );
                    retVal.addContact()
                        .addTelecom(
                            new ContactPoint()
                                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                                .setValue(phone)
                                .setUse(ContactPoint.ContactPointUse.WORK)
                        )
                        .setPurpose(
                            new CodeableConcept(
                                new Coding()
                                    .setSystem(Utils.namingSystems.get("phoneType"))
                                    .setCode(
                                        extractSingleValueFromXml(document, xpath,
                                            Utils.xpathExpressions.get("phoneType"),
                                            i
                                        )
                                    )
                                    .setDisplay(
                                        extractSingleValueFromXml(document, xpath,
                                            Utils.xpathExpressions.get(
                                                "phoneDescription"
                                            ),
                                            i
                                        )
                                    )
                            )
                        );
                }
            }
            
            // Email -> contact
            String email
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("email"),
                    0
                );
            if (email != null) {
                retVal.addContact()
                    .addTelecom(
                        new ContactPoint()
                            .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                            .setValue(email)
                            .setUse(ContactPoint.ContactPointUse.WORK)
                    )
                    .setPurpose(
                        new CodeableConcept(
                            new Coding()
                                .setSystem(Utils.namingSystems.get("emailType"))
                                .setCode(
                                    extractSingleValueFromXml(document, xpath,
                                        Utils.xpathExpressions.get("emailType"),
                                        0
                                    )
                                )
                        )
                    );
            }
            
        } catch (URISyntaxException
                    | IOException
                    | InterruptedException
                    | ParserConfigurationException
                    | SAXException
                    | XPathExpressionException ex) {
            Logger.getLogger(OrganizationCnesPreRegistrationResourceProvider.class.getName())
                    .log(Level.SEVERE, null, ex);
        } 
        
        // Fire the event for the STORAGE_PRESHOW_RESOURCES Pointcut.
        // TODO: for some requests it's generating duplicated AuditEvent
        // instances. Review and correct.
        List<IBaseResource> fireStoragePreshowResource
            = ServerInterceptorUtil.fireStoragePreshowResource(
                Arrays.asList(retVal),
                theRequestDetails,
                theInterceptorBroadcaster
            );
        
        return
            (OrganizationCnesPreRegistration)fireStoragePreshowResource.get(0);
    }

    // Extract a single value from the XML document based on an XPath expression.
    private String extractSingleValueFromXml(Document document, XPath xpath,
        String xpathExpression, int index) throws XPathExpressionException {
        
        XPathExpression expr = xpath.compile(xpathExpression);
        Object result = expr.evaluate(document, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        
        if (nodes == null || nodes.item(index) == null) {
            return null;
        }
        return nodes.item(index).getNodeValue();
    }
    
    // Count the number of nodes returned by an XPath expression.
    private int countNodesFromXml(Document document, XPath xpath,
        String xpathExpression) throws XPathExpressionException {

        XPathExpression expr = xpath.compile(xpathExpression);
        Object result = expr.evaluate(document, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        
        return nodes.getLength();
    }
    
    // For debugging purposes.
    // Uncomment to see the nodes extracted from the XML received from the
    // SOAP Webservice.
//    private static void printNode(NodeList nodeList, int level, String path) {
//        
//        level++;
//        
//        if (nodeList != null && nodeList.getLength() > 0) {
//            for (int i = 0; i < nodeList.getLength(); i++) {
//                Node node = nodeList.item(i);
//                path = path + "/" + node.getNodeName();
//                if (node.getNodeType() == Node.TEXT_NODE) {
//                    System.out.println(path + ": " + node.getNodeType());
//                }
//                printNode(node.getChildNodes(), level, path);
//            }
//        }
//    }
}

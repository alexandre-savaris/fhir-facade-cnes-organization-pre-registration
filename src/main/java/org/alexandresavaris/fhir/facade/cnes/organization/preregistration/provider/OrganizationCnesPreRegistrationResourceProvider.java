package org.alexandresavaris.fhir.facade.cnes.organization.preregistration.provider;

import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
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
    private String codeSnippetForFilteringBySituation = null;

    // The empty constructor.
    public OrganizationCnesPreRegistrationResourceProvider() {
    }
    
    // The parameterized constructor.
    public OrganizationCnesPreRegistrationResourceProvider(
        String endpointEstabelecimentoSaudeService,
        String username,
        String password,
        String soapEnvelopeContent,
        String cnesFilter,
        String situationFilter) {
        
        this.endpointEstabelecimentoSaudeService
            = endpointEstabelecimentoSaudeService;
        this.username = username;
        this.password = password;
        this.contentOfSoapEnvelope = soapEnvelopeContent;
        this.codeSnippetForFilteringByCnes = cnesFilter;
        this.codeSnippetForFilteringBySituation = situationFilter;
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
     * read operation.
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

            // Access the SOAP Webservice.
            String responseBody = accessSoapWebservice(theId.getIdPart(), null);
            
            // Fill in the resource instance.
            retVal = fillResourceInstance(theId, responseBody, null);
            
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

    /**
     * The "@Search" annotation indicates that this method supports the
     * search operation.
     *
     * @param theId The identifier of the resource instance to be searched for.
     * @param preRegistrationSituation The pre-registration situation for the
     * Organization.
     * @param theRequestDetails Details from the performed request.
     * @param theInterceptorBroadcaster For broadcasting the related event to
     * known interceptors.
     * @return Returns a resource list matching this identifier, or null if none
     * exists.
     */
    @Description(
        shortDefinition
            = "Pesquisa um Estabelecimento de Sa\u00fade pelo seu Identificador e c\u00f3digo de Pr\u00e9-cadastro."
    )
    @Search()
    public List<OrganizationCnesPreRegistration> search(
        @Description(
            shortDefinition
                = "Identificador do Estabelecimento de Sa\u00fade com Sistema de Terminologia = urn:oid:2.16.840.1.113883.13.36."
        )
            @RequiredParam(name = OrganizationCnesPreRegistration.SP_IDENTIFIER)
                TokenParam theId,
        @Description(
            shortDefinition
                = "C\u00f3digo da Situa\u00e7\u00e3o de Pr\u00e9-cadastro do Estabelecimento de Sa\u00fade com Sistema de Terminologia = https://alexandresavaris.org/fhir/r4/NamingSystem/cnes/SituacaoPreCadastro."
        )
            @RequiredParam(name = OrganizationCnesPreRegistration.SP_PRE_REGISTRATION_SITUATION)
                TokenParam preRegistrationSituation,
        RequestDetails theRequestDetails,
        IInterceptorBroadcaster theInterceptorBroadcaster) {
        // The resource instance to be returned.
        OrganizationCnesPreRegistration retVal = null;

        try {

            // System for the identifier.
            String identifierSystem = theId.getSystem();
            if (!identifierSystem.equals("urn:oid:" + Utils.oids.get("cnes"))) {
                // TODO: verify why the message used for the exception doesn't
                // appear in the OperationOutcome instance.
                throw new UnprocessableEntityException(
                    new String(
                        "O sistema de terminologia (terminology system) informado na requisição ("
                            .getBytes("ISO-8859-1"),
                        "UTF-8"
                    )
                    + identifierSystem
                    + new String(
                        ") não corresponde ao sistema de terminologia esperado pelo servidor."
                            .getBytes("ISO-8859-1"),
                        "UTF-8"
                    )
                );
            }

            // System for the pre-registration situation code.
            String preRegistrationSituationSystem
                = preRegistrationSituation.getSystem();
            if (!preRegistrationSituationSystem.equals(
                Utils.namingSystems.get("situacaoPreCadastro"))
                ) {
                // TODO: verify why the message used for the exception doesn't
                // appear in the OperationOutcome instance.
                throw new UnprocessableEntityException(
                    new String(
                        "O sistema de terminologia (terminology system) informado na requisição ("
                            .getBytes("ISO-8859-1"),
                        "UTF-8"
                    )
                    + preRegistrationSituationSystem
                    + new String(
                        ") não corresponde ao sistema de terminologia esperado pelo servidor."
                            .getBytes("ISO-8859-1"),
                        "UTF-8"
                    )
                );
            }

            // Value for the identifier.
            String identifier = theId.getValue();
            // System and value for the pre-registration situation code.
            String preRegistrationSituationCode
                = preRegistrationSituation.getValue();

            // Access the SOAP Webservice.
            String responseBody = accessSoapWebservice(
                identifier, preRegistrationSituationCode);

            // Fill in the resource instance.
            retVal = fillResourceInstance(
                new IdType(identifier),
                responseBody,
                preRegistrationSituationCode
            );
            
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
        
        return Arrays.asList(
            (OrganizationCnesPreRegistration) fireStoragePreshowResource.get(0)
        );
    }
    
    // Update the content of SOAP Envelope to be sent to the Webservice.
    private String updateContentOfSoapEnvelope(
        String identifier, String preRegistrationSituationCode) {
        // Code snippets to be used into the envelope.
        String snippetFilterCnes;
        String snippetFilterSituation = null;

        snippetFilterCnes = MessageFormat.format(
            this.codeSnippetForFilteringByCnes, identifier
        );
        if (preRegistrationSituationCode != null) {
            snippetFilterSituation = MessageFormat.format(
                this.codeSnippetForFilteringBySituation,
                preRegistrationSituationCode
            );
        }
        
        String updatedContent = MessageFormat.format(
            this.contentOfSoapEnvelope,
            this.username,
            this.password,
            snippetFilterCnes,
            (preRegistrationSituationCode != null ? snippetFilterSituation : "")
        );
        
        return updatedContent;
    }
    
    // Access the SOAP Webservice.
    private String accessSoapWebservice(
        String identifier, String preRegistrationSituationCode
    ) throws URISyntaxException, IOException, InterruptedException {

        // Update the content of SOAP Envelope to be sent to the Webservice.
        String updatedContentOfSoapEnvelope
            = updateContentOfSoapEnvelope(
                identifier, preRegistrationSituationCode
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
            
//        // For debugging purposes.
//        // Uncomment to see the response body from the SOAP Webservice.
//        System.out.println(
//            "----------------------------------------------------------"
//        );
//        System.out.println(response.statusCode());
//        System.out.println(responseBody);
//        System.out.println(
//            "----------------------------------------------------------"
//        );

        int responseStatusCode = response.statusCode();
        if (responseStatusCode != 200) {
            // TODO: verify why the message used for the exception doesn't
            // appear in the OperationOutcome instance.
            throw new InternalErrorException(
                new String(
                    "O webservice SOAP retornou o código de status HTTP "
                        .getBytes("ISO-8859-1"),
                    "UTF-8"
                )
                + responseStatusCode
                + new String(
                    " ao acessar os dados do estabelecimento de saúde com o Id: "
                        .getBytes("ISO-8859-1"),
                    "UTF-8"
                )
                + identifier
            );
        }
            
        return responseBody;
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
    
    // Create a Document instance from the XML content.
    private Document createDocumentFromXml(String responseBody)
        throws ParserConfigurationException, SAXException, IOException {

        // XML parsing for content extraction.
        DocumentBuilderFactory factory
            = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document
            = builder.parse(
                new InputSource(new StringReader(responseBody))
            );
        
        return document;
    }
    
    // Set namespaces for XPath use.
    private XPath setNamespacesForXpath() {
        
        // Setting namespaces for using XPath.
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        NamespaceContext context = new NamespaceContextMap(
            Utils.xmlNamespaces);
        xpath.setNamespaceContext(context);
        
        return xpath;
    }
    
    // Fill in the resource instance.
    private OrganizationCnesPreRegistration fillResourceInstance(
        IdType theId,
        String responseBody,
        String preRegistrationSituationCode)
        throws ParserConfigurationException,
               SAXException,
               IOException,
               XPathExpressionException {
        
        // The resource instance to be returned.
        OrganizationCnesPreRegistration retVal
            = new OrganizationCnesPreRegistration();
            
        // The logical ID replicates the Organization's CNES.
        retVal.setId(theId);
        
        // Document representing the XML content.
        Document document = createDocumentFromXml(responseBody);
        
        // The instance providing access to the XPath evaluation environment
        // and expressions.
        XPath xpath = setNamespacesForXpath();
            
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

        // tipoNaturezaJuridicaMantenedora
        //     -> Extension (Maintainer's Legal Nature Category).
        String maintainerLegalNatureCategory
            = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get("maintainerLegalNatureCategory"),
                0
            );
        String maintainerLegalNatureCategoryDisplay
            = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get(
                    "maintainerLegalNatureCategoryDisplay"
                ),
                0
            );
        if (maintainerLegalNatureCategory != null
            || maintainerLegalNatureCategoryDisplay != null) {
            retVal.setMaintainerLegalNatureCategory(
                new Coding()
                    .setSystem(
                        Utils.namingSystems.get("categoriaNaturezaJuridica")
                    )
                    .setCode(maintainerLegalNatureCategory)
                    .setDisplay(maintainerLegalNatureCategoryDisplay)
            );
        }

        // codigoNaturezaJuridicaConclaMantenedora
        //     -> Extension (Maintainer's Legal Nature Code).
        String maintainerLegalNatureCode
            = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get("maintainerLegalNatureCode"),
                0
            );
        String maintainerLegalNatureCodeDisplay
            = extractSingleValueFromXml(document, xpath,
                Utils.xpathExpressions.get(
                    "maintainerLegalNatureCodeDisplay"
                ),
                0
            );
        if (maintainerLegalNatureCode != null
            || maintainerLegalNatureCodeDisplay != null) {
                
            // The correct format for the Legal Nature Code, according to
            // the Brazilian Institute of Geography and Statistics (IBGE).
            String formattedMaintainerLegalNatureCode
                = (maintainerLegalNatureCode != null
                    ? maintainerLegalNatureCode.substring(0, 3)
                        + "-"
                        + maintainerLegalNatureCode.substring(3)
                    : maintainerLegalNatureCode);
                
            retVal.setMaintainerLegalNatureCode(
                new Coding()
                    .setSystem(
                        Utils.namingSystems.get("codigoNaturezaJuridica")
                    )
                    .setCode(formattedMaintainerLegalNatureCode)
                    .setDisplay(maintainerLegalNatureCodeDisplay)
            );
        }

        // Situação do Pré-cadastro
        //     -> Extension (The situation of the pre-registration for the
        //                   Organization).
        if (preRegistrationSituationCode != null) {
            retVal.setPreRegistrationSituation(
                new Coding()
                    .setSystem(
                        Utils.namingSystems.get("situacaoPreCadastro")
                    )
                    .setCode(preRegistrationSituationCode)
                    .setDisplay(
                        Utils.preRegistrationSituations.get(
                            preRegistrationSituationCode
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
        
        return retVal;
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

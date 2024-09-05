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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import org.w3c.dom.Node;
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
            
            // For debugging purposes.
            // Uncomment to see the response body from the SOAP Webservice.
//            System.out.println(
//                "----------------------------------------------------------"
//            );
//            System.out.println(response.statusCode());
//            System.out.println(response.body());
//            System.out.println(
//                "----------------------------------------------------------"
//            );

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
                    new InputSource(new StringReader(response.body()))
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

//            // CodigoUnidade -> Identifier: Unity code.
//            String unityCode =
//                extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("unityCode"),
//                    0
//                );
//            if (unityCode != null) {
//                retVal.addIdentifier()
//                    .setSystem(Utils.namingSystems.get("unityCode"))
//                    .setValue(unityCode);
//            }

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
            String addressTextTemplate = "{0}, {1} - {2} - {3} - {4}";
            String addressText = java.text.MessageFormat.format(
                addressTextTemplate, street, number, neighborhood, city, state);
            
            Address address = new Address()
                .setUse(Address.AddressUse.WORK)
                .setType(Address.AddressType.BOTH)
                .setText(addressText)
                .setCity(city)
                .setState(state)
                .setPostalCode(
                    extractSingleValueFromXml(document, xpath,
                        Utils.xpathExpressions.get("postalCode"),
                        0
                    )
                )
                .setCountry("BRA");
            
            // Extensions for IBGE codes.
            String cityCodeIbge
                = extractSingleValueFromXml(document, xpath,
                    Utils.xpathExpressions.get("cityCodeIbge"),
                    0
                );
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
//            String stateCodeIbge
//                = extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("stateCodeIbge"),
//                    0
//                );
//            if (stateCodeIbge != null) {
//                Extension stateCodeIbgeExtension
//                    = new Extension(Utils.extensions.get("stateCodeIbge"));
//                stateCodeIbgeExtension.setValue(
//                    new Coding()
//                        .setSystem("urn:oid:" + Utils.oids.get("ibgeCode"))
//                        .setCode(stateCodeIbge)
//                        .setDisplay(
//                            new String(
//                                "Código da UF no IBGE".getBytes("ISO-8859-1"),
//                                "UTF-8"
//                            )
//                        )
//                );
//                address.addExtension(stateCodeIbgeExtension);
//            }
            
//            // Geolocation extensions.
//            String latitude
//                = extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("latitude"),
//                    0
//                );
//            String longitude
//                = extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("longitude"),
//                    0
//                );
//            if (latitude != null && longitude != null) {
//                Extension geolocationExtension
//                    = new Extension(Utils.extensions.get("geolocation"));
//                Extension latitudeExtension
//                    = new Extension(Utils.extensions.get("latitude"));
//                latitudeExtension.setValue(
//                    new DecimalType(latitude)
//                );
//                Extension longitudeExtension
//                    = new Extension(Utils.extensions.get("longitude"));
//                longitudeExtension.setValue(
//                    new DecimalType(longitude)
//                );
//                geolocationExtension.addExtension(latitudeExtension);
//                geolocationExtension.addExtension(longitudeExtension);
//                // Completing the address to be returned.
//                address.addExtension(geolocationExtension);
//            }
            
            retVal.addAddress(address);
            
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

//            // numeroCPF -> Extension (Director's CPF).
//            // NOTE: despite its availability in the original response from the
//            // SOAP Webservice, the Director's CPF is considered personal data
//            // and is replaced by a fake one.
//            retVal.setDirectorCpf(
//                new Coding()
//                    .setSystem("urn:oid:" + Utils.oids.get("cpf"))
//                    .setCode("42424242424")
//                    .setDisplay(
//                        new String(
//                            "Número do CPF do Diretor".getBytes("ISO-8859-1"),
//                            "UTF-8"
//                        )
//                    )
//            );

//            // Nome -> Extension (Director's name).
//            // NOTE: despite its availability in the original response from the
//            // SOAP Webservice, the Director's name is considered personal data
//            // and is replaced by a fake one.
//            retVal.setDirectorName(
//                new HumanName().setText("Marvin the Paranoid Android")
//            );

//            // tipoUnidade -> type.
//            String unityType
//                = extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("unityType"),
//                    0
//                );
//            if (unityType != null) {
//                retVal.addType(
//                    new CodeableConcept(
//                        new Coding()
//                            .setSystem(Utils.valueSets.get("type"))
//                            .setCode(unityType)
//                            .setDisplay(
//                                extractSingleValueFromXml(document, xpath,
//                                    Utils.xpathExpressions.get(
//                                        "unityDescription"
//                                    ),
//                                    0
//                                )
//                            )
//                    )
//                );
//            }
                
//            // Telefone -> contact
//            String phoneNumber
//                = extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("phoneNumber"),
//                    0
//                );
//            if (phoneNumber != null) {
//                String phoneTemplate = "{0} {1}";
//                String phone = java.text.MessageFormat.format(
//                    phoneTemplate,
//                    extractSingleValueFromXml(document, xpath,
//                        Utils.xpathExpressions.get("phoneAreaCode"),
//                        0
//                    ),
//                    phoneNumber
//                );
//                retVal.addContact()
//                    .addTelecom(
//                        new ContactPoint()
//                            .setSystem(ContactPoint.ContactPointSystem.PHONE)
//                            .setValue(phone)
//                            .setUse(ContactPoint.ContactPointUse.WORK)
//                    )
//                    .setPurpose(
//                        new CodeableConcept(
//                            new Coding()
//                                .setSystem(Utils.namingSystems.get("phoneType"))
//                                .setCode(
//                                    extractSingleValueFromXml(document, xpath,
//                                        Utils.xpathExpressions.get("phoneType"),
//                                        0
//                                    )
//                                )
//                                .setDisplay(
//                                    extractSingleValueFromXml(document, xpath,
//                                        Utils.xpathExpressions.get(
//                                            "phoneDescription"
//                                        ),
//                                        0
//                                    )
//                                )
//                        )
//                    );
//            }

//            // Email -> contact
//            String email
//                = extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("email"),
//                    0
//                );
//            if (email != null) {
//                retVal.addContact()
//                    .addTelecom(
//                        new ContactPoint()
//                            .setSystem(ContactPoint.ContactPointSystem.EMAIL)
//                            .setValue(email)
//                            .setUse(ContactPoint.ContactPointUse.WORK)
//                    )
//                    .setPurpose(
//                        new CodeableConcept(
//                            new Coding()
//                                .setSystem(Utils.namingSystems.get("emailType"))
//                                .setCode(
//                                    extractSingleValueFromXml(document, xpath,
//                                        Utils.xpathExpressions.get("emailType"),
//                                        0
//                                    )
//                                )
//                        )
//                    );
//            }
            
//            // perteceSistemaSUS -> Extension (Is the Organization part of SUS?).
//            String isSus
//                = extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("isSus"),
//                    0
//                );
//            if (isSus != null) {
//                retVal.setIsSus(
//                    new BooleanType(isSus)
//                );
//            }

//            // fluxoClientela -> Extension (The client flow expected for the
//            // Organization).
//            String clientFlow
//                = extractSingleValueFromXml(document, xpath,
//                    Utils.xpathExpressions.get("clientFlow"),
//                    0
//                );
//            if (clientFlow != null) {
//                retVal.setClientFlow(
//                    new CodeType(clientFlow)
//                        .setSystem(Utils.namingSystems.get("clientFlow"))
//                );
//            }
            
//            // servicoespecializados -> Extension (specializedServices).
//            XPathExpression expr
//                = xpath.compile(Utils.xpathExpressions.get("specializedService"));
//            Object result = expr.evaluate(document, XPathConstants.NODESET);
//            NodeList nodeList = (NodeList) result;
            
            // For debugging purposes.
            // Uncomment to see the nodes extracted from the XML received from the
            // SOAP Webservice.
//            printNode(nodeList, 0, "");
            
//            List<OrganizationCnesPreRegistration.SpecializedService> specializedServices
//                = new ArrayList<>();
//            fillInResourceInstanceWithSpecializedServices(nodeList, "",
//                specializedServices);
//            retVal.setSpecializedServices(specializedServices);

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
    
//    // Loop through the XML content and fill in the resource instance with
//    // Specialized Services.
//    private void fillInResourceInstanceWithSpecializedServices(
//        NodeList nodeList,
//        String path,
//        List<OrganizationCnesPreRegistration.SpecializedService> specializedServices)
//        throws UnsupportedEncodingException {
//
//        // Nothing to do here.
//        if (nodeList == null || nodeList.getLength() == 0) {
//            return;
//        }
//        
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            Node node = nodeList.item(i);
//            path = path + "/" + node.getNodeName();
//            if (node.getNodeType() == Node.TEXT_NODE) {
//                if (
//                    path.endsWith(
//                        Utils.xpathExpressionSuffixes.get(
//                            "specializedServiceCode"
//                        )
//                    )
//                ) {
//                    // Specialized service.
//                    // For the system and code values, create a new extension
//                    // instance.
//                    specializedServices.add(new OrganizationCnesPreRegistration.SpecializedService()
//                            .setSpecializedService(
//                                new Coding()
//                                    .setSystem(
//                                        Utils.namingSystems.get(
//                                            "specializedServiceType"
//                                        )
//                                    )
//                                    .setCode(node.getNodeValue())
//                            )
//                    );
//                } else if (
//                    path.endsWith(
//                        Utils.xpathExpressionSuffixes.get(
//                            "specializedServiceDescription"
//                        )
//                    )
//                ) {
//                    // Specialized service.
//                    // For the display, use the last created extension instance
//                    // of Specialized Service.
//                    OrganizationCnesPreRegistration.SpecializedService specializedService
//                        = getLastSpecializedService(specializedServices);
//                    specializedService.getSpecializedService()
//                        .setDisplay(node.getNodeValue());
//                } else if (
//                    path.endsWith(
//                        Utils.xpathExpressionSuffixes.get(
//                            "specializedServiceClassificationCode"
//                        )
//                    )
//                ) {
//                    // Specialized service classification.
//                    // For the system and code values, retrieve the last created
//                    // extension instance of the Specialized service.
//                    // Retrieve the list of Classifications, adding a new one
//                    // afterward.
//                    List<OrganizationCnesPreRegistration.SpecializedService.SpecializedServiceClassification>
//                        specializedServiceClassifications
//                        = getListOfSpecializedServiceClassifications(
//                            specializedServices);
//                    specializedServiceClassifications.add(new OrganizationCnesPreRegistration.SpecializedService.SpecializedServiceClassification()
//                            .setSpecializedServiceClassification(
//                                new Coding()
//                                    .setSystem(
//                                        Utils.namingSystems.get(
//                                            "specializedServiceClassification"
//                                        )
//                                    )
//                                    .setCode(node.getNodeValue())
//                            )
//                    );
//                } else if (
//                    path.endsWith(
//                        Utils.xpathExpressionSuffixes.get(
//                            "specializedServiceClassificationDescription"
//                        )
//                    )
//                ) {
//                    // Specialized service classification.
//                    // For the display value, retrieve the last created
//                    // extension instance of the Specialized service
//                    // classification.
//                    OrganizationCnesPreRegistration.SpecializedService.SpecializedServiceClassification
//                        specializedServiceClassification
//                            = getLastSpecializedServiceClassification(
//                                specializedServices
//                            );
//                    specializedServiceClassification.getSpecializedServiceClassification()
//                        .setDisplay(node.getNodeValue());
//                } else if (
//                    path.endsWith(
//                        Utils.xpathExpressionSuffixes.get(
//                            "specializedServiceClassificationCharacteristicCode"
//                        )
//                    )
//                ) {
//                    // Specialized service classification characteristic.
//                    // For the characteristic code, retrieve the last created
//                    // extension instance of the Specialized service
//                    // classification.
//                    OrganizationCnesPreRegistration.SpecializedService.SpecializedServiceClassification
//                        specializedServiceClassification
//                            = getLastSpecializedServiceClassification(
//                                specializedServices
//                            );
//                    specializedServiceClassification.getSpecializedServiceClassificationCharacteristic()
//                        .setSystem(
//                            Utils.namingSystems.get(
//                                "specializedServiceClassificationCharacteristic"
//                            )
//                        )
//                        .setValue(node.getNodeValue());
//                } else if (
//                    path.endsWith(
//                        Utils.xpathExpressionSuffixes.get(
//                            "specializedServiceClassificationCharacteristicCnes"
//                        )
//                    )
//                ) {
//                    // Specialized service classification characteristic.
//                    // For the CNES code, retrieve the last created
//                    // extension instance of the Specialized service
//                    // classification.
//                    OrganizationCnesPreRegistration.SpecializedService.SpecializedServiceClassification
//                        specializedServiceClassification
//                            = getLastSpecializedServiceClassification(
//                                specializedServices
//                            );
//                    specializedServiceClassification.getSpecializedServiceClassificationCnes()
//                        .setSystem("urn:oid:" + Utils.oids.get("cnes"))
//                        .setCode(node.getNodeValue())
//                        .setDisplay(
//                            new String(
//                                "Número no CNES".getBytes("ISO-8859-1"),
//                                "UTF-8"
//                            )
//                        );
//                }
//            }
//            fillInResourceInstanceWithSpecializedServices(
//                node.getChildNodes(),
//                path,
//                specializedServices);
//        }
//    }
    
//    // Get the last Specialized Service from the list.
//    private OrganizationCnesPreRegistration.SpecializedService getLastSpecializedService
//        (List<OrganizationCnesPreRegistration.SpecializedService> specializedServices) {
//            
//        return specializedServices.get(specializedServices.size() - 1);
//    }
        
//    // Get the list of Specialized Service Classifications from the last
//    // Specialized Service.
//    private List<OrganizationCnesPreRegistration.SpecializedService.SpecializedServiceClassification>
//        getListOfSpecializedServiceClassifications(
//            List<OrganizationCnesPreRegistration.SpecializedService> specializedServices) {
//            
//        OrganizationCnesPreRegistration.SpecializedService specializedService
//            = getLastSpecializedService(specializedServices);
//            
//        return specializedService.getSpecializedServiceClassifications();
//    }

//    // Get the last Specialized Service Classification from the list.
//    private OrganizationCnesPreRegistration.SpecializedService.SpecializedServiceClassification
//        getLastSpecializedServiceClassification(
//            List<OrganizationCnesPreRegistration.SpecializedService> specializedServices) {
//            
//        List<OrganizationCnesPreRegistration.SpecializedService.SpecializedServiceClassification>
//            specializedServiceClassifications
//                = getListOfSpecializedServiceClassifications(
//                    specializedServices
//                );
//            
//        return specializedServiceClassifications.get(
//            specializedServiceClassifications.size() - 1
//        );
//    }
        
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


//<soap:Envelope
//	xmlns:env="http://www.w3.org/2003/05/soap-envelope"
//	xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
//	<S:Header
//		xmlns:S="http://www.w3.org/2003/05/soap-envelope">
//		<WorkContext
//			xmlns="http://oracle.com/weblogic/soap/workarea/"
//			xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
//			xmlns:S="http://www.w3.org/2003/05/soap-envelope">rO0ABXdrACh3ZWJsb2dpYy5hcHAuY25lcy1lYXItMS4xMS4wLmNsYXNzbG9hZGVyAAAA1gAAACN3ZWJsb2dpYy53b3JrYXJlYS5TdHJpbmdXb3JrQ29udGV4dAASMS4xMS4wLmNsYXNzbG9hZGVyAAA=
//		</WorkContext>
//	</S:Header>
//	<S:Body
//		xmlns:S="http://www.w3.org/2003/05/soap-envelope">
//		<est:responseConsultarPrecadastroCNES
//			xmlns:est="http://servicos.saude.gov.br/cnes/v1r0/estabelecimentosaudeservice">
//			<dad:DadosPreCadastroCNES
//				xmlns:dad="http://servicos.saude.gov.br/schema/cnes/v1r0/dadosprecadastrocnes">
//				<nat:NaturezaJuridica
//					xmlns:nat="http://servicos.saude.gov.br/schema/cnes/v1r0/dadosprecadastrocnes">
//					<nat1:codigoNaturezaJuridica
//						xmlns:nat1="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica">02
//					</nat1:codigoNaturezaJuridica>
//					<nat1:descricaoNaturezaJuridica
//						xmlns:nat1="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica">ORGAO PUBLICO DO PODER EXECUTIVO ESTADUAL OU DO DISTRITO FEDERAL
//					</nat1:descricaoNaturezaJuridica>
//					<nat1:codigoNaturezaJuridicaConcla
//						xmlns:nat1="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica">1023
//					</nat1:codigoNaturezaJuridicaConcla>
//					<nat1:tipoNaturezaJuridica
//						xmlns:nat1="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica">
//						<tip:codigo
//							xmlns:tip="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/tiponaturezajuridica">1
//						</tip:codigo>
//						<tip:descricao
//							xmlns:tip="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/tiponaturezajuridica">ADMINISTRACAO PUBLICA
//						</tip:descricao>
//					</nat1:tipoNaturezaJuridica>
//				</nat:NaturezaJuridica>
//				<ns29:CNPJMantenedora
//					xmlns:ns0="http://servicos.saude.gov.br/cnes/v1r0/estabelecimentosaudeservice"
//					xmlns:ns2="http://servicos.saude.gov.br/wsdl/mensageria/v1r0/filtropesquisaestabelecimentosaude"
//					xmlns:ns4="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/cnpj"
//					xmlns:ns3="http://servicos.saude.gov.br/schema/cnes/v1r0/codigocnes"
//					xmlns:ns6="http://servicos.saude.gov.br/schema/cnes/v1r0/codigounidade"
//					xmlns:ns31="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/tiponaturezajuridica"
//					xmlns:ns5="http://servicos.saude.gov.br/schema/cnes/v1r0/dadosgeraiscnes"
//					xmlns:ns30="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica"
//					xmlns:ns8="http://servicos.saude.gov.br/schema/corporativo/endereco/v1r2/endereco"
//					xmlns:ns7="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/nomejuridico"
//					xmlns:ns35="http://servicos.saude.gov.br/schema/cnes/v1r0/listatipounidade"
//					xmlns:ns13="http://servicos.saude.gov.br/schema/corporativo/v1r1/uf"
//					xmlns:ns34="http://servicos.saude.gov.br/wsdl/mensageria/estabelecimentosaudeservice/v2r0/resultadopesquisaestabelecimento.v1r0"
//					xmlns:ns9="http://servicos.saude.gov.br/schema/corporativo/endereco/v1r1/tipologradouro"
//					xmlns:ns12="http://servicos.saude.gov.br/schema/corporativo/v1r2/municipio"
//					xmlns:ns33="http://servicos.saude.gov.br/wsdl/mensageria/v1/paginacao"
//					xmlns:ns11="http://servicos.saude.gov.br/schema/corporativo/endereco/v1r1/cep"
//					xmlns:ns32="http://servicos.saude.gov.br/wsdl/mensageria/estabelecimentosaudeservice/v2r0/filtropesquisaestabelecimento.v1r0"
//					xmlns:ns10="http://servicos.saude.gov.br/schema/corporativo/endereco/v1r1/bairro"
//					xmlns:ns17="http://servicos.saude.gov.br/schema/corporativo/pessoafisica/v1r2/nomecompleto"
//					xmlns:ns38="http://servicos.saude.gov.br/schema/corporativo/v1r3/municipio"
//					xmlns:ns16="http://servicos.saude.gov.br/schema/corporativo/documento/v1r2/cpf"
//					xmlns:ns37="http://servicos.saude.gov.br/schema/corporativo/v1r2/uf"
//					xmlns:ns15="http://servicos.saude.gov.br/schema/cnes/v1r0/diretor"
//					xmlns:ns14="http://servicos.saude.gov.br/schema/corporativo/v1r2/pais"
//					xmlns:ns19="http://servicos.saude.gov.br/schema/cnes/v1r0/esferaadministrativa"
//					xmlns:ns18="http://servicos.saude.gov.br/schema/cnes/v1r0/tipounidade"
//					xmlns:ns20="http://servicos.saude.gov.br/schema/corporativo/telefone/v1r2/telefone"
//					xmlns:ns24="http://servicos.saude.gov.br/schema/cnes/v1r0/servicoespecializados"
//					xmlns:ns23="http://servicos.saude.gov.br/schema/cnes/v1r0/localizacao"
//					xmlns:ns22="http://servicos.saude.gov.br/schema/corporativo/v1r2/email"
//					xmlns:ns21="http://servicos.saude.gov.br/schema/corporativo/telefone/v1r1/tipotelefone"
//					xmlns:ns28="http://servicos.saude.gov.br/wsdl/mensageria/v1r0/filtropesquisaprecadastrocnes"
//					xmlns:ns27="http://servicos.saude.gov.br/schema/cnes/v1r0/servicoclassificacao"
//					xmlns:ns26="http://servicos.saude.gov.br/schema/cnes/v1r0/servicoclassificacoes"
//					xmlns:ns25="http://servicos.saude.gov.br/schema/cnes/v1r0/servicoespecializado"
//					xmlns:ns29="http://servicos.saude.gov.br/schema/cnes/v1r0/dadosprecadastrocnes">
//					<ns4:numeroCNPJ>82951245000169</ns4:numeroCNPJ>
//				</ns29:CNPJMantenedora>
//				<nat:NaturezaJuridicaMantenedora
//					xmlns:nat="http://servicos.saude.gov.br/schema/cnes/v1r0/dadosprecadastrocnes">
//					<nat1:codigoNaturezaJuridica
//						xmlns:nat1="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica">02
//					</nat1:codigoNaturezaJuridica>
//					<nat1:descricaoNaturezaJuridica
//						xmlns:nat1="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica">ORGAO PUBLICO DO PODER EXECUTIVO ESTADUAL OU DO DISTRITO FEDERAL
//					</nat1:descricaoNaturezaJuridica>
//					<nat1:codigoNaturezaJuridicaConcla
//						xmlns:nat1="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica">1023
//					</nat1:codigoNaturezaJuridicaConcla>
//					<nat1:tipoNaturezaJuridica
//						xmlns:nat1="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica">
//						<tip:codigo
//							xmlns:tip="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/tiponaturezajuridica">1
//						</tip:codigo>
//						<tip:descricao
//							xmlns:tip="http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/tiponaturezajuridica">ADMINISTRACAO PUBLICA
//						</tip:descricao>
//					</nat1:tipoNaturezaJuridica>
//				</nat:NaturezaJuridicaMantenedora>
//				</dad:DadosPreCadastroCNES>
//			</est:responseConsultarPrecadastroCNES>
//		</S:Body>
//	</soap:Envelope>
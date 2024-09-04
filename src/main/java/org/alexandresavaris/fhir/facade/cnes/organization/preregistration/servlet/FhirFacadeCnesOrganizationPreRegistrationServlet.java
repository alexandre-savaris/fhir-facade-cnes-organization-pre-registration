package org.alexandresavaris.fhir.facade.cnes.organization.preregistration.servlet;

import java.util.ArrayList;
import java.util.List;

import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.provider.OrganizationCnesPreRegistrationResourceProvider;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.storage.interceptor.balp.BalpAuditCaptureInterceptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.interceptor.BearerAuthorizationInterceptor;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.interceptor.LoggingInterceptor;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.interceptor.balp.BalpAuditContextService;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.interceptor.balp.FileBalpSink;

/**
 * This servlet is the actual FHIR server itself.
 */
public class FhirFacadeCnesOrganizationPreRegistrationServlet
    extends RestfulServer {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public FhirFacadeCnesOrganizationPreRegistrationServlet() {
        
    	super(FhirContext.forR4Cached());
    }
	
    /**
     * This method is called automatically when the servlet is initializing.
     */
    @Override
    public void initialize() {
            
        try {

            // Load the properties file.
            String rootPath = Thread.currentThread()
                .getContextClassLoader()
                .getResource("")
                .getPath();
            String appConfigPath = rootPath + "application.properties";
            
            Properties appProps = new Properties();
            appProps.load(new FileInputStream(appConfigPath));
            
            String endpointEstabelecimentoSaudeService
                = appProps.getProperty(
                    "endpoint.cnes.estabelecimentosaudeservice"
                );
            String username
                = appProps.getProperty(
                    "endpoint.cnes.estabelecimentosaudeservice.username"
                );
            String password
                = appProps.getProperty(
                    "endpoint.cnes.estabelecimentosaudeservice.password"
                );
            String bearerToken
                = appProps.getProperty(
                    "authorization.bearer_token"
                );
                
            // Load the content of the SOAP envelope to be sent to the endpoint.
            URL url = this.getClass()
                .getClassLoader()
                .getResource(
                    "soap/envelopes/consultarEstabelecimentoSaude.xml"
                );
            File file = new File(url.getFile());
            String soapEnvelopeContent
                = new String(Files.readAllBytes(file.toPath()));

            // Load the XML snippet for filtering by CNES.
            url = this.getClass()
                .getClassLoader()
                .getResource("soap/envelopes/filtroCnes.xml");
            file = new File(url.getFile());
            String cnesFilter = new String(Files.readAllBytes(file.toPath()));

            /*
             * Resource providers that handle specific types of resources.
             */
            List<IResourceProvider> providers = new ArrayList<>();
            providers.add(new OrganizationCnesPreRegistrationResourceProvider(
                    endpointEstabelecimentoSaudeService,
                    username,
                    password,
                    soapEnvelopeContent,
                    cnesFilter
                )
            );
            setResourceProviders(providers);

            /*
             * Use a narrative generator. This is a completely optional step,
             * but can be useful as it causes HAPI to generate narratives for
             * resources which don't otherwise have one.
             */
            CustomThymeleafNarrativeGenerator narrativeGen
                = new CustomThymeleafNarrativeGenerator(
                    "classpath:org/alexandresavaris/fhir/facade/cnes/organization/preregistration/narratives/narratives.properties"
                );
            getFhirContext().setNarrativeGenerator(narrativeGen);
            
            /*
             * Use nice coloured HTML when a browser is used to request the
             * content.
             */
            registerInterceptor(new ResponseHighlighterInterceptor());
            
            // For Bearer Authorization.
            registerInterceptor(
                new BearerAuthorizationInterceptor(bearerToken)
            );

            // For logging.
            registerInterceptor(new LoggingInterceptor());
            
            // For Basic Audit Log Patterns (BALP).
            // Generation and persistence of AuditEvent instances.
            registerInterceptor(
                new BalpAuditCaptureInterceptor(
                    new FileBalpSink(this.getFhirContext(), "/data"),
                    new BalpAuditContextService()
                )
            );

        } catch (IOException ex) {
            Logger.getLogger(FhirFacadeCnesOrganizationPreRegistrationServlet.class.getName())
                .log(Level.SEVERE, null, ex);
        }
    }
}

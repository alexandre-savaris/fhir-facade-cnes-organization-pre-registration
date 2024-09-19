package org.alexandresavaris.fhir.facade.cnes.organization.preregistration.test.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.Arrays;
import java.util.List;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.model.OrganizationCnesPreRegistration;
import org.alexandresavaris.fhir.facade.cnes.organization.preregistration.util.Utils;
import org.hl7.fhir.r4.model.*;

/**
 * Definition of a ResourceProvider for managing instances of the
 * <code>OrganizationCnesPreRegistration</code> class used in tests.
 */
public class OrganizationCnesPreRegistrationResourceProvider
    implements IResourceProvider {

    // The empty constructor.
    public OrganizationCnesPreRegistrationResourceProvider() {
    }
    
    /**
     * The getResourceType method comes from IResourceProvider, and must be
     * overridden to indicate what type of resource this provider supplies.
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
     * @return Returns a resource matching this identifier, or null if none
     * exists.
     * @throws java.lang.Exception
     */
    @Read(type = OrganizationCnesPreRegistration.class)
    public OrganizationCnesPreRegistration getResourceById(
        @IdParam IdType theId) throws Exception {

        // Fill in the resource instance.
        OrganizationCnesPreRegistration retVal
            = fillResourceInstance(theId, null);
        
        return retVal;
    }

    /**
     * The "@Search" annotation indicates that this method supports the
     * search operation.
     *
     * @param theId The identifier of the resource instance to be searched for.
     * @param preRegistrationSituation The pre-registration situation for the
     * Organization.
     * @return Returns a resource list matching this identifier, or null if none
     * exists.
     * @throws java.lang.Exception
     */
    @Search()
    public List<OrganizationCnesPreRegistration> search(
        @RequiredParam(name = OrganizationCnesPreRegistration.SP_IDENTIFIER)
            TokenParam theId,
        @RequiredParam(name = OrganizationCnesPreRegistration.SP_PRE_REGISTRATION_SITUATION)
            TokenParam preRegistrationSituation) throws Exception {

        // System and value for the identifier.
        String identifierSystem = theId.getSystem();
        String identifier = theId.getValue();

        // System and value for the pre-registration situation code.
        String preRegistrationSituationSystem
            = preRegistrationSituation.getSystem();
        String preRegistrationSituationCode
            = preRegistrationSituation.getValue();

        // Fill in the resource instance.
        OrganizationCnesPreRegistration retVal
            = fillResourceInstance(
                new IdType(identifier), preRegistrationSituationCode
            );

        return Arrays.asList(retVal);
    }
    
    // Fill in the resource instance.
    private OrganizationCnesPreRegistration fillResourceInstance(
        IdType theId, String preRegistrationSituationCode) throws Exception {
        
        // The resource instance to be returned.
        OrganizationCnesPreRegistration retVal
            = new OrganizationCnesPreRegistration();
            
        // The logical ID replicates the Organization's CNES.
        retVal.setId(theId);
        
        // CodigoCNES -> Identifier: CNES.
        retVal.addIdentifier()
            .setSystem("urn:oid:" + Utils.oids.get("cnes"))
            .setValue(theId.getValue());

        // numeroCNPJ -> Identifier: CNPJ.
        retVal.addIdentifier()
            .setSystem(Utils.namingSystems.get("cnpj"))
            .setValue("22222222222222");

        // nomeFantasia -> name.
        retVal.setName("THE TEST ORGANIZATION'S NAME");

        // nomeEmpresarial -> alias.
        retVal.addAlias("THE TEST ORGANIZATION'S ALIAS");
            
        // Endereco -> Address.
        String street = "THE STREET";
        String number = "1";
        String neighborhood = "THE NEIGHBORHOOD";
        String city = "THE CITY";
        String state = "THE STATE";
        String addressTextTemplate = "{0}, {1} - {2} - {3} - {4}";
        String addressText = java.text.MessageFormat.format(
            addressTextTemplate, street, number, neighborhood, city, state);
        
        Address address = new Address()
            .setUse(Address.AddressUse.WORK)
            .setType(Address.AddressType.BOTH)
            .setText(addressText)
            .setCity(city)
            .setState(state)
            .setPostalCode("33333")
            .setCountry("BRA");

        // Extensions for IBGE codes.
        Extension cityCodeIbgeExtension
            = new Extension(Utils.extensions.get("cityCodeIbge"));
        cityCodeIbgeExtension.setValue(
            new Coding()
                .setSystem("urn:oid:" + Utils.oids.get("ibgeCode"))
                .setCode("444444")
                .setDisplay(
                    new String(
                        "Código do município no IBGE".getBytes("ISO-8859-1"),
                        "UTF-8"
                    )
                )
        );

        // Completing the address to be returned.
        address.addExtension(cityCodeIbgeExtension);
        
        // Completing the address to be returned.
        retVal.addAddress(address);
        
        // dataAtualizacao -> Extension (update date).
        retVal.setUpdateDate(new DateType("1980-01-01"));

        // tipoNaturezaJuridica -> Extension (Legal Nature Category).
        retVal.setLegalNatureCategory(
            new Coding()
                .setSystem(
                    Utils.namingSystems.get("categoriaNaturezaJuridica")
                )
                .setCode("5")
                .setDisplay("THE LEGAL NATURE CATEGORY")
            );

        // codigoNaturezaJuridicaConcla -> Extension (Legal Nature Code).
        // The correct format for the Legal Nature Code, according to
        // the Brazilian Institute of Geography and Statistics (IBGE).
        retVal.setLegalNatureCode(
            new Coding()
                .setSystem(
                    Utils.namingSystems.get("codigoNaturezaJuridica")
                )
                .setCode("666-6")
                .setDisplay("THE LEGAL NATURE CODE")
        );

        // CNPJMantenedora -> Extension (Maintainer's CNPJ).
        retVal.setMaintainerCnpj(
            new Coding()
                .setSystem(Utils.namingSystems.get("cnpj"))
                .setCode("77777777777777")
                .setDisplay(
                    new String(
                        "Número do CNPJ da mantenedora"
                            .getBytes("ISO-8859-1"),
                        "UTF-8"
                    )
                )
        );

        // tipoNaturezaJuridicaMantenedora
        //     -> Extension (Maintainer's Legal Nature Category).
        retVal.setMaintainerLegalNatureCategory(
            new Coding()
                .setSystem(
                    Utils.namingSystems.get("categoriaNaturezaJuridica")
                )
                .setCode("8")
                .setDisplay("THE LEGAL NATURE CATEGORY")
            );

        // codigoNaturezaJuridicaConclaMantenedora
        //     -> Extension (Maintainer's Legal Nature Code).
        retVal.setLegalNatureCode(
            new Coding()
                .setSystem(
                    Utils.namingSystems.get("codigoNaturezaJuridica")
                )
                .setCode("999-9")
                .setDisplay("THE LEGAL NATURE CODE")
        );

        // Situação do Pré-cadastro
        //     -> Extension (The situation of the pre-registration for the
        //                   Organization).
        retVal.setPreRegistrationSituation(
            new Coding()
                .setSystem(Utils.namingSystems.get("situacaoPreCadastro"))
                .setCode("A")
                .setDisplay(Utils.preRegistrationSituations.get("A"))
        );

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
        
        // Telefone -> contact
        String phoneTemplate = "{0} {1}";
        String phone = java.text.MessageFormat.format(
            phoneTemplate, "88", "999999999");
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
                        .setCode("1")
                        .setDisplay("THE PHONE TYPE")
                )
            );
        
        // Email -> contact
        retVal.addContact()
            .addTelecom(
                new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue("aa@bb.cc")
                    .setUse(ContactPoint.ContactPointUse.WORK)
            )
            .setPurpose(
                new CodeableConcept(
                    new Coding()
                        .setSystem(Utils.namingSystems.get("emailType"))
                        .setCode("1")
                )
            );
        
        return retVal;
    }
}

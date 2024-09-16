package org.alexandresavaris.fhir.facade.cnes.organization.preregistration.model;

import ca.uhn.fhir.model.api.annotation.*;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.*;

@ResourceDef(name = "Organization")
public class OrganizationCnesPreRegistration extends Organization {

    // Update date.
    @Description(
        shortDefinition
            = "The date when the resource instance's data were updated.")
    @ca.uhn.fhir.model.api.annotation.Extension(
        url = "https://alexandresavaris.org/fhir/r4/Extension/cnes/DataAtualizacao",
        isModifier = false,
        definedLocally = true)
    @Child(name = "updateDate")
    private DateType updateDate;

    // Legal Nature Category.
    @Description(
        shortDefinition
            = "The Category of the Legal Nature for the Organization.")
    @ca.uhn.fhir.model.api.annotation.Extension(
        url = "https://alexandresavaris.org/fhir/r4/Extension/cnes/CategoriaNaturezaJuridica",
        isModifier = false,
        definedLocally = true)
    @Child(name = "legalNatureCategory")
    private Coding legalNatureCategory;

    // Legal Nature Code.
    @Description(
        shortDefinition
            = "The Code of the Legal Nature for the Organization.")
    @ca.uhn.fhir.model.api.annotation.Extension(
        url = "https://alexandresavaris.org/fhir/r4/Extension/cnes/CodigoNaturezaJuridica",
        isModifier = false,
        definedLocally = true)
    @Child(name = "legalNatureCode")
    private Coding legalNatureCode;

    // Pre-registration Situation.
    @Description(
        shortDefinition
            = "The situation of the pre-registration for the Organization.")
    @ca.uhn.fhir.model.api.annotation.Extension(
        url = "https://alexandresavaris.org/fhir/r4/Extension/cnes/SituacaoPreCadastro",
        isModifier = false,
        definedLocally = true)
    @Child(name = "preRegistrationSituation")
    private Coding preRegistrationSituation;
    
    // Maintainer's CNPJ.
    @Description(
        shortDefinition
            = "The CNPJ number (Cadastro Nacional de Pessoas Jurídicas) of the Organization's Maintainer.")
    @ca.uhn.fhir.model.api.annotation.Extension(
        url = "https://alexandresavaris.org/fhir/r4/Extension/cnes/CnpjMantenedora",
        isModifier = false,
        definedLocally = true)
    @Child(name = "maintainerCnpj")
    private Coding maintainerCnpj;

    // Maintainer's Legal Nature Category.
    @Description(
        shortDefinition
            = "The Category of the Legal Nature for the Maintainer of the Organization.")
    @ca.uhn.fhir.model.api.annotation.Extension(
        url = "https://alexandresavaris.org/fhir/r4/Extension/cnes/CategoriaNaturezaJuridicaMantenedora",
        isModifier = false,
        definedLocally = true)
    @Child(name = "maintainerLegalNatureCategory")
    private Coding maintainerLegalNatureCategory;

    // Maintainer's Legal Nature Code.
    @Description(
        shortDefinition
            = "The Code of the Legal Nature for the Maintainer of the Organization.")
    @ca.uhn.fhir.model.api.annotation.Extension(
        url = "https://alexandresavaris.org/fhir/r4/Extension/cnes/CodigoNaturezaJuridicaMantenedora",
        isModifier = false,
        definedLocally = true)
    @Child(name = "maintainerLegalNatureCode")
    private Coding maintainerLegalNatureCode;
    
    /**
     * Search parameter: <b>preRegistrationSituation</b>
     * <p>
     * Description: <b>The pre-registration situation for the Organization.</b><br>
     * Type: <b>token</b><br>
     * Path: <b>Organization.extension('https://alexandresavaris.org/fhir/r4/Extension/cnes/SituacaoPreCadastro')</b><br>
     * </p>
     */
    @SearchParamDefinition(
        name = "preRegistrationSituation",
        path = "Organization.extension('https://alexandresavaris.org/fhir/r4/Extension/cnes/SituacaoPreCadastro')",
        description = "The pre-registration situation for the Organization.",
        type = "token")
    public static final String SP_PRE_REGISTRATION_SITUATION = "preRegistrationSituation";
    /**
     * <b>Fluent Client</b> search parameter constant for <b>preRegistrationSituation</b>
     * <p>
     * Description: <b>The pre-registration situation for the Organization.</b><br>
     * Type: <b>token</b><br>
     * Path: <b>Organization.extension('https://alexandresavaris.org/fhir/r4/Extension/cnes/SituacaoPreCadastro')</b><br>
     * </p>
     */
    public static final ca.uhn.fhir.rest.gclient.TokenClientParam PRE_REGISTRATION_SITUATION
        = new ca.uhn.fhir.rest.gclient.TokenClientParam(
            SP_PRE_REGISTRATION_SITUATION
        );

    public DateType getUpdateDate() {
        
        if (this.updateDate == null) {
            this.updateDate = new DateType();
        }
        
        return this.updateDate;
    }
    
    public void setUpdateDate(DateType updateDate) {
        
        this.updateDate = updateDate;
    }

    public Coding getLegalNatureCategory() {
        
        if (this.legalNatureCategory == null) {
            this.legalNatureCategory = new Coding();
        }
        
        return this.legalNatureCategory;
    }
    
    public void setLegalNatureCategory(Coding legalNatureCategory) {
        
        this.legalNatureCategory = legalNatureCategory;
    }

    public Coding getLegalNatureCode() {
        
        if (this.legalNatureCode == null) {
            this.legalNatureCode = new Coding();
        }
        
        return this.legalNatureCode;
    }
    
    public void setLegalNatureCode(Coding legalNatureCode) {
        
        this.legalNatureCode = legalNatureCode;
    }

    public Coding getPreRegistrationSituation() {
        
        if (this.preRegistrationSituation == null) {
            this.preRegistrationSituation = new Coding();
        }
        
        return this.preRegistrationSituation;
    }
    
    public void setPreRegistrationSituation(Coding preRegistrationSituation) {
        
        this.preRegistrationSituation = preRegistrationSituation;
    }

    public Coding getMaintainerCnpj() {
        
        if (this.maintainerCnpj == null) {
            this.maintainerCnpj = new Coding();
        }
        
        return this.maintainerCnpj;
    }
    
    public void setMaintainerCnpj(Coding maintainerCnpj) {
        
        this.maintainerCnpj = maintainerCnpj;
    }

    public Coding getMaintainerLegalNatureCategory() {
        
        if (this.maintainerLegalNatureCategory == null) {
            this.maintainerLegalNatureCategory = new Coding();
        }
        
        return this.maintainerLegalNatureCategory;
    }
    
    public void setMaintainerLegalNatureCategory(
        Coding maintainerLegalNatureCategory) {
        
        this.maintainerLegalNatureCategory = maintainerLegalNatureCategory;
    }

    public Coding getMaintainerLegalNatureCode() {
        
        if (this.maintainerLegalNatureCode == null) {
            this.maintainerLegalNatureCode = new Coding();
        }
        
        return this.maintainerLegalNatureCode;
    }
    
    public void setMaintainerLegalNatureCode(Coding maintainerLegalNatureCode) {
        
        this.maintainerLegalNatureCode = maintainerLegalNatureCode;
    }
    
    // Are all elements of the resource instance null?
    @Override
    public boolean isEmpty() {
        
      return super.isEmpty() && ElementUtil.isEmpty(
          this.updateDate,
          this.legalNatureCategory,
          this.legalNatureCode,
          this.preRegistrationSituation,
          this.maintainerCnpj,
          this.maintainerLegalNatureCategory,
          this.maintainerLegalNatureCode
      );
    }
}

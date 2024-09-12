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
          this.maintainerCnpj,
          this.legalNatureCategory,
          this.legalNatureCode,
          this.maintainerLegalNatureCategory,
          this.maintainerLegalNatureCode
      );
    }
}

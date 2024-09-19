package org.alexandresavaris.fhir.facade.cnes.organization.preregistration.util;

import java.util.HashMap;
import java.util.Map;

// Utility methods and constants.
public class Utils {
    // Map of namespaces for parsing XML content.
    public static final Map<String, String> xmlNamespaces = new HashMap<>();
    // Map of XPath expressions for extracting XML content.
    public static final Map<String, String> xpathExpressions = new HashMap<>();
    // Map of XPath expression suffixes for extracting XML content.
    public static final Map<String, String> xpathExpressionSuffixes
        = new HashMap<>();
    // Map of NamingSystems used in the OrganizationCnes instance.
    public static final Map<String, String> namingSystems = new HashMap<>();
    // Map of ValueSets used in the OrganizationCnes instance.
    public static final Map<String, String> valueSets = new HashMap<>();
    // Map of OIDs used in the OrganizationCnes instance.
    public static final Map<String, String> oids = new HashMap<>();
    // Map of Extensions used in the OrganizationCnes instance.
    public static final Map<String, String> extensions = new HashMap<>();
    // Map of Pre-registration Situation codes.
    public static final Map<String, String> preRegistrationSituations
        = new HashMap<>();
    
    static {

        // Insert namespaces.
        // -------------------------------
        xmlNamespaces.put("soap",
            "http://www.w3.org/2003/05/soap-envelope");
        xmlNamespaces.put("S",
            "http://www.w3.org/2003/05/soap-envelope");
        xmlNamespaces.put("est",
            "http://servicos.saude.gov.br/cnes/v1r0/estabelecimentosaudeservice");
        xmlNamespaces.put("dad",
            "http://servicos.saude.gov.br/schema/cnes/v1r0/dadosprecadastrocnes");
        // -------------------------------
        xmlNamespaces.put("bair",
            "http://servicos.saude.gov.br/schema/corporativo/endereco/v1r1/bairro");
        xmlNamespaces.put("cep",
            "http://servicos.saude.gov.br/schema/corporativo/endereco/v1r1/cep");
        xmlNamespaces.put("email",
            "http://servicos.saude.gov.br/schema/corporativo/v1r2/email");
        xmlNamespaces.put("end",
            "http://servicos.saude.gov.br/schema/corporativo/endereco/v1r2/endereco");
        xmlNamespaces.put("mun",
            "http://servicos.saude.gov.br/schema/corporativo/v1r2/municipio");
        xmlNamespaces.put("nat",
            "http://servicos.saude.gov.br/schema/cnes/v1r0/dadosprecadastrocnes");
        xmlNamespaces.put("nat1",
            "http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/naturezajuridica");
        xmlNamespaces.put("ns3",
            "http://servicos.saude.gov.br/schema/cnes/v1r0/codigocnes");
        xmlNamespaces.put("ns4",
            "http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/cnpj");
        xmlNamespaces.put("ns7",
            "http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/nomejuridico");
        xmlNamespaces.put("ns29",
            "http://servicos.saude.gov.br/schema/cnes/v1r0/dadosprecadastrocnes");
        xmlNamespaces.put("tel",
            "http://servicos.saude.gov.br/schema/cnes/v1r0/dadosprecadastrocnes");
        xmlNamespaces.put("tel1",
            "http://servicos.saude.gov.br/schema/corporativo/telefone/v1r2/telefone");
        xmlNamespaces.put("tip",
            "http://servicos.saude.gov.br/schema/corporativo/pessoajuridica/v1r0/tiponaturezajuridica");
        xmlNamespaces.put("tipTel",
            "http://servicos.saude.gov.br/schema/corporativo/telefone/v1r1/tipotelefone");
        xmlNamespaces.put("uf",
            "http://servicos.saude.gov.br/schema/corporativo/v1r1/uf");

        // Insert XPath expressions.
        xpathExpressions.put("alias",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/ns29:NomeEmpresarial/ns7:Nome/text()");
        xpathExpressions.put("city",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/end:Endereco/end:Municipio/mun:nomeMunicipio/text()");
        xpathExpressions.put("cityCodeIbge",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/end:Endereco/end:Municipio/mun:codigoMunicipio/text()");
        xpathExpressions.put("cnes",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/ns3:CodigoCNES/ns3:codigo/text()");
        xpathExpressions.put("cnpj",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/ns4:CNPJ/ns4:numeroCNPJ/text()");
        xpathExpressions.put("email",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/email:Email/email:descricaoEmail/text()");
        xpathExpressions.put("emailType",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/email:Email/email:tipoEmail/text()");
        xpathExpressions.put("legalNatureCategory",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/nat:NaturezaJuridica/nat1:tipoNaturezaJuridica/tip:codigo/text()");
        xpathExpressions.put("legalNatureCategoryDisplay",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/nat:NaturezaJuridica/nat1:tipoNaturezaJuridica/tip:descricao/text()");
        xpathExpressions.put("legalNatureCode",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/nat:NaturezaJuridica/nat1:codigoNaturezaJuridicaConcla/text()");
        xpathExpressions.put("legalNatureCodeDisplay",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/nat:NaturezaJuridica/nat1:descricaoNaturezaJuridica/text()");
        xpathExpressions.put("maintainerCnpj",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/ns29:CNPJMantenedora/ns4:numeroCNPJ/text()");
        xpathExpressions.put("maintainerLegalNatureCategory",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/nat:NaturezaJuridicaMantenedora/nat1:tipoNaturezaJuridica/tip:codigo/text()");
        xpathExpressions.put("maintainerLegalNatureCategoryDisplay",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/nat:NaturezaJuridicaMantenedora/nat1:tipoNaturezaJuridica/tip:descricao/text()");
        xpathExpressions.put("maintainerLegalNatureCode",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/nat:NaturezaJuridicaMantenedora/nat1:codigoNaturezaJuridicaConcla/text()");
        xpathExpressions.put("maintainerLegalNatureCodeDisplay",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/nat:NaturezaJuridicaMantenedora/nat1:descricaoNaturezaJuridica/text()");
        xpathExpressions.put("name",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/ns29:NomeFantasia/ns7:Nome/text()");
        xpathExpressions.put("neighborhood",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/end:Endereco/end:Bairro/bair:descricaoBairro/text()");
        xpathExpressions.put("number",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/end:Endereco/end:numero/text()");
        xpathExpressions.put("phoneAreaCode",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/tel:Telefones/tel1:DDD/text()");
        xpathExpressions.put("phoneDescription",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/tel:Telefones/tel1:TipoTelefone/tipTel:descricaoTipoTelefone/text()");
        xpathExpressions.put("phoneNumber",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/tel:Telefones/tel1:numeroTelefone/text()");
        xpathExpressions.put("phones",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/tel:Telefones");
        xpathExpressions.put("phoneType",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/tel:Telefones/tel1:TipoTelefone/tipTel:codigoTipoTelefone/text()");
        xpathExpressions.put("postalCode",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/end:Endereco/end:CEP/cep:numeroCEP/text()");
        xpathExpressions.put("state",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/end:Endereco/end:Municipio/mun:UF/uf:siglaUF/text()");
        xpathExpressions.put("street",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/end:Endereco/end:nomeLogradouro/text()");
        xpathExpressions.put("updateDate",
            "//soap:Envelope/S:Body/est:responseConsultarPrecadastroCNES/dad:DadosPreCadastroCNES/ns29:DataAtualizacao/text()");

        // Insert NamingSystems.
        namingSystems.put("categoriaNaturezaJuridica",
            "https://alexandresavaris.org/fhir/r4/NamingSystem/cnes/CategoriaNaturezaJuridica");
        namingSystems.put("cnpj",
            "http://rnds.saude.gov.br/fhir/r4/NamingSystem/cnpj");
        namingSystems.put("codigoNaturezaJuridica",
            "https://alexandresavaris.org/fhir/r4/NamingSystem/cnes/CodigoNaturezaJuridica");
        namingSystems.put("emailType",
            "https://alexandresavaris.org/fhir/r4/NamingSystem/cnes/TipoEmail");
        namingSystems.put("phoneType",
            "https://alexandresavaris.org/fhir/r4/NamingSystem/cnes/TipoTelefone");
        namingSystems.put("situacaoPreCadastro",
            "https://alexandresavaris.org/fhir/r4/NamingSystem/cnes/SituacaoPreCadastro");

        // Insert OIDs.
        // https://www.hl7.org/oid/index.cfm
        oids.put("cnes", "2.16.840.1.113883.13.36");
        oids.put("ibgeCode", "2.16.840.1.113883.4.707");
        
        // Insert Extensions.
        extensions.put(
            "cityCodeIbge",
            "https://alexandresavaris.org/fhir/r4/Extension/cnes/CodigoMunicipioIbge"
        );

        // Insert Pre-registration Situation codes.
        preRegistrationSituations.put("A", "ATIVO");
        preRegistrationSituations.put("D", "DESATIVADO");
        preRegistrationSituations.put("E", "EXCLUIDO");
    }
}

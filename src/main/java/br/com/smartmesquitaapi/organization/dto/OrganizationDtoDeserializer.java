package br.com.smartmesquitaapi.organization.dto;

import br.com.smartmesquitaapi.user.dto.AddressDto;
import br.com.smartmesquitaapi.user.dto.BankDetailsDto;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Deserializador customizado para OrganizationDto
 * Detecta automaticamente se é Mosque ou Church baseado nos campos presentes
 */
public class OrganizationDtoDeserializer extends JsonDeserializer<OrganizationDto> {

    @Override
    public OrganizationDto deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        // Verifica qual campo específico está presente
        boolean hasPriestName = node.has("priestName");
        boolean hasImaName = node.has("imaName");

        OrganizationDto dto;

        if (hasImaName && !hasPriestName) {
            // É uma Mesquita
            MosqueDto mosque = new MosqueDto();
            mosque.setImaName(node.get("imaName").asText());
            dto = mosque;
        } else if (hasPriestName && !hasImaName) {
            // É uma Igreja
            ChurchDto church = new ChurchDto();
            church.setPriestName(node.get("priestName").asText());
            dto = church;
        } else if (hasImaName && hasPriestName) {
            throw new IllegalArgumentException(
                "Organização não pode ter ambos 'imaName' e 'priestName'. " +
                "Use apenas 'imaName' para mesquita ou 'priestName' para igreja."
            );
        } else {
            // Se nenhum campo específico está presente, assume Mesquita como padrão
            dto = new MosqueDto();
        }

        // Popula os campos comuns
        if (node.has("orgName")) {
            dto.setOrgName(node.get("orgName").asText());
        }
        if (node.has("phoneNumber")) {
            dto.setPhoneNumber(node.get("phoneNumber").asText());
        }
        if (node.has("foundationDate")) {
            dto.setFoundationDate(LocalDate.parse(node.get("foundationDate").asText()));
        }
        if (node.has("administratorName")) {
            dto.setAdministratorName(node.get("administratorName").asText());
        }
        if (node.has("cnpj")) {
            dto.setCnpj(node.get("cnpj").asText());
        }
        if (node.has("openingHours")) {
            dto.setOpeningHours(node.get("openingHours").asText());
        }

        // Deserializa BankDetails
        if (node.has("bankDetails")) {
            BankDetailsDto bankDetails = jp.getCodec().treeToValue(node.get("bankDetails"), BankDetailsDto.class);
            dto.setBankDetails(bankDetails);
        }

        // Deserializa AddressDto
        if (node.has("addressDto")) {
            AddressDto addressDto = jp.getCodec().treeToValue(node.get("addressDto"), AddressDto.class);
            dto.setAddressDto(addressDto);
        }

        return dto;
    }
}

package br.com.smartmesquitaapi.user.mapper;

import br.com.smartmesquitaapi.user.domain.Address;
import br.com.smartmesquitaapi.user.domain.MosqueInfo;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.dto.AddressDto;
import br.com.smartmesquitaapi.user.dto.MosqueInfoDto;
import br.com.smartmesquitaapi.user.dto.MosqueProfileDto;
import org.springframework.stereotype.Component;

@Component
public class MosqueMapper {

    public MosqueProfileDto toProfileDto(User user){
        MosqueProfileDto mosqueProfileDto = new MosqueProfileDto();

        MosqueInfoDto infoDto = new MosqueInfoDto();

        if (user.getMosqueInfo() != null) {
            var source = user.getMosqueInfo();
            infoDto.setMosqueName(source.getMosqueName());
            infoDto.setAdministratorName(source.getAdministratorName());
            infoDto.setImaName(source.getImaName());
            infoDto.setCnpj(source.getCnpj());
            infoDto.setOpeningHours(source.getOpeningHours());
            infoDto.setPhoneNumber(source.getPhoneNumber());
            infoDto.setFoundationDate(source.getFoundationDate());
        }
        mosqueProfileDto.setMosqueInfoDto(infoDto);
        mosqueProfileDto.setAddressDto(toAddressDto(user.getAddress()));

        return mosqueProfileDto;
    }

    private AddressDto toAddressDto(Address address) {
        if (address == null) return null;

        AddressDto dto = new AddressDto();
        dto.setStreet(address.getStreet());
        dto.setNumber(address.getNumber());
        dto.setNeighborhood(address.getNeighborhood());
        dto.setZipcode(address.getZipcode());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setComplement(address.getComplement());

        return dto;
    }

    public void updateUserFromDto(MosqueProfileDto dto, User user){
        if (dto.getMosqueInfoDto() != null){
            MosqueInfo mosqueInfo = user.getMosqueInfo();

            if (mosqueInfo == null){
                mosqueInfo = new MosqueInfo();
            }

            MosqueInfoDto mosqueInfoDto = dto.getMosqueInfoDto();

            mosqueInfo.setAdministratorName(mosqueInfoDto.getAdministratorName());
            mosqueInfo.setCnpj(mosqueInfoDto.getCnpj());
            mosqueInfo.setFoundationDate(mosqueInfoDto.getFoundationDate());
            mosqueInfo.setImaName(mosqueInfoDto.getImaName());
            mosqueInfo.setMosqueName(mosqueInfoDto.getMosqueName());
            mosqueInfo.setOpeningHours(mosqueInfoDto.getOpeningHours());
            mosqueInfo.setPhoneNumber(mosqueInfoDto.getPhoneNumber());

            user.setMosqueInfo(mosqueInfo);
        }

        if (dto.getAddressDto() != null){
            Address address = user.getAddress();

            if (address == null){
                address = new Address();
            }

            AddressDto addressDto = dto.getAddressDto();

            address.setCity(addressDto.getCity());
            address.setComplement(addressDto.getComplement());
            address.setNeighborhood(addressDto.getNeighborhood());
            address.setNumber(addressDto.getNumber());
            address.setStreet(addressDto.getStreet());
            address.setState(addressDto.getState());
            address.setZipcode(addressDto.getZipcode());

            user.setAddress(address);
        }
    }

}

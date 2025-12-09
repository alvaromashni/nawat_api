package br.com.smartmesquitaapi.organization.mapper;

import br.com.smartmesquitaapi.organization.domain.Church;
import br.com.smartmesquitaapi.organization.domain.Mosque;
import br.com.smartmesquitaapi.organization.domain.Organization;
import br.com.smartmesquitaapi.organization.dto.ChurchDto;
import br.com.smartmesquitaapi.organization.dto.MosqueDto;
import br.com.smartmesquitaapi.organization.dto.OrganizationDto;
import br.com.smartmesquitaapi.user.domain.Address;
import br.com.smartmesquitaapi.user.domain.Notification;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.dto.AddressDto;
import br.com.smartmesquitaapi.user.dto.NotificationsSettingsDto;
import br.com.smartmesquitaapi.user.dto.OrganizationProfileDto;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public OrganizationProfileDto toProfileDto(User user){

        Organization org = user.getOrganization();
        OrganizationProfileDto organizationProfileDto = new OrganizationProfileDto();

        NotificationsSettingsDto notificationDto = new NotificationsSettingsDto();

        if (user.getNotification() != null){
            Notification notification = user.getNotification();
            notificationDto.setDailySummary(notification.isDailySummary());
            notificationDto.setDonationDone(notification.isDonationDone());
            notificationDto.setTotemMaintenance(notification.isTotemMaintenance());
        }

        if (org instanceof Church) {
            ChurchDto churchdto =  mapToChurchDto((Church) org);
            organizationProfileDto.setOrganizationDto(churchdto);
        }
        else if (org instanceof Mosque) {
            MosqueDto mosqueDto = mapToMosqueDto((Mosque) org);
            organizationProfileDto.setOrganizationDto(mosqueDto);
        }
        organizationProfileDto.setNotificationsSettingsDto(notificationDto);

        return organizationProfileDto;
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

    public void updateOrganizationFromDto(OrganizationDto dto, Organization org){
        if (dto != null) {
            org.setAddress(mapToAddress(dto.getAddressDto()));
            org.setBankDetails(dto.getBankDetails());
            org.setAdministratorName(dto.getAdministratorName());
            org.setCnpj(dto.getCnpj());
            org.setFoundationDate(dto.getFoundationDate());
            org.setOpeningHours(dto.getOpeningHours());
            org.setOrgName(dto.getOrgName());
            org.setPhoneNumber(dto.getPhoneNumber());

            if (org instanceof Church && dto instanceof ChurchDto) {
                ((Church) org).setPriestName(((ChurchDto) dto).getPriestName());
            } else if (org instanceof Mosque && dto instanceof MosqueDto) {
                ((Mosque) org).setImaName(((MosqueDto) dto).getImaName());
            }
        }
    }

    /**
     * Converte um OrganizationDTO em uma Organization Entity
     */
    public Organization toEntity(OrganizationDto dto){

        Address address = mapToAddress(dto.getAddressDto());

        if (dto instanceof ChurchDto) {
            return getChurch(dto, address);
        }
        else if (dto instanceof MosqueDto) {
            return getMosque(dto, address);
        }
        return null;
    }

    private ChurchDto mapToChurchDto(Church church){
        ChurchDto dto = new ChurchDto();
        dto.setAdministratorName(church.getAdministratorName());
        dto.setBankDetails(church.getBankDetails());
        dto.setCnpj(church.getCnpj());
        dto.setFoundationDate(church.getFoundationDate());
        dto.setOpeningHours(church.getOpeningHours());
        dto.setOrgName(church.getOrgName());
        dto.setPhoneNumber(church.getPhoneNumber());
        dto.setPriestName(church.getPriestName());
        dto.setAddressDto(toAddressDto(church.getAddress()));

        return dto;
    }

    private MosqueDto mapToMosqueDto(Mosque mosque){

        MosqueDto dto = new MosqueDto();
        dto.setAdministratorName(mosque.getAdministratorName());
        dto.setBankDetails(mosque.getBankDetails());
        dto.setCnpj(mosque.getCnpj());
        dto.setFoundationDate(mosque.getFoundationDate());
        dto.setOpeningHours(mosque.getOpeningHours());
        dto.setOrgName(mosque.getOrgName());
        dto.setPhoneNumber(mosque.getPhoneNumber());
        dto.setImaName(mosque.getImaName());
        dto.setAddressDto(toAddressDto(mosque.getAddress()));

        return dto;
    }


    private static Church getChurch(OrganizationDto dto, Address address) {
        Church church = new Church();
        church.setAdministratorName(dto.getAdministratorName());
        church.setBankDetails(dto.getBankDetails());
        church.setCnpj(dto.getCnpj());
        church.setFoundationDate(dto.getFoundationDate());
        church.setOpeningHours(dto.getOpeningHours());
        church.setOrgName(dto.getOrgName());
        church.setPhoneNumber(dto.getPhoneNumber());
        church.setPriestName(((ChurchDto) dto).getPriestName());
        church.setAddress(address);
        return church;
    }
    private static Mosque getMosque(OrganizationDto dto,  Address address){
        Mosque mosque = new Mosque();
        mosque.setAdministratorName(dto.getAdministratorName());
        mosque.setBankDetails(dto.getBankDetails());
        mosque.setCnpj(dto.getCnpj());
        mosque.setFoundationDate(dto.getFoundationDate());
        mosque.setOpeningHours(dto.getOpeningHours());
        mosque.setOrgName(dto.getOrgName());
        mosque.setPhoneNumber(dto.getPhoneNumber());
        mosque.setImaName(((MosqueDto) dto).getImaName());
        mosque.setAddress(address);
        return mosque;
    }

    private static Address mapToAddress(AddressDto dto) {

        if (dto != null){
            Address address = new Address();

            address.setCity(dto.getCity());
            address.setComplement(dto.getComplement());
            address.setNeighborhood(dto.getNeighborhood());
            address.setNumber(dto.getNumber());
            address.setStreet(dto.getStreet());
            address.setState(dto.getState());
            address.setZipcode(dto.getZipcode());
            return address;
        }
        return null;
    }

}

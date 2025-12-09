package br.com.smartmesquitaapi.organization.service;

import br.com.smartmesquitaapi.organization.exception.OrganizationNotFoundException;
import br.com.smartmesquitaapi.pix.exception.UserNotFoundException;
import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.dto.OrganizationProfileDto;
import br.com.smartmesquitaapi.organization.mapper.OrganizationMapper;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final UserRepository userRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationService(UserRepository userRepository, OrganizationMapper organizationMapper) {
        this.userRepository = userRepository;
        this.organizationMapper = organizationMapper;
    }

    public OrganizationProfileDto getOrganizationProfile(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("Usuário não encontrado com email: " + email));

        if (user.getOrganization() == null) {
            throw new OrganizationNotFoundException("Usuário não possui organização associada");
        }

        return organizationMapper.toProfileDto(user);
    }

    @Transactional
    public void updateOrganizationProfile(OrganizationProfileDto dto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("Usuário não encontrado com email: " + email));

        if (user.getOrganization() == null) {
            throw new OrganizationNotFoundException("Usuário não possui organização associada");
        }

        organizationMapper.updateOrganizationFromDto(dto.getOrganizationDto(), user.getOrganization());
        userRepository.save(user);
    }

}

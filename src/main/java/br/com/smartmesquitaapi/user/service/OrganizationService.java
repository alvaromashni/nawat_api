package br.com.smartmesquitaapi.user.service;

import br.com.smartmesquitaapi.organization.dto.OrganizationDto;
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
                () -> new RuntimeException("Email não encontrado."));

        return organizationMapper.toProfileDto(user);
    }

    @Transactional
    public void updateMosqueProfile(OrganizationProfileDto dto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email não encontrado."));

        organizationMapper.updateUserFromDto(dto, user);
        userRepository.save(user);
    }

}

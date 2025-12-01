package br.com.smartmesquitaapi.user.service;

import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.domain.Address;
import br.com.smartmesquitaapi.user.domain.MosqueInfo;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.dto.AddressDto;
import br.com.smartmesquitaapi.user.dto.MosqueInfoDto;
import br.com.smartmesquitaapi.user.dto.MosqueProfileDto;
import br.com.smartmesquitaapi.user.mapper.MosqueMapper;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MosqueService {

    private final UserRepository userRepository;
    private final MosqueMapper mosqueMapper;

    public MosqueService(UserRepository userRepository, MosqueMapper mosqueMapper) {
        this.userRepository = userRepository;
        this.mosqueMapper = mosqueMapper;
    }

    public MosqueProfileDto getMosqueProfile(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email não encontrado."));

        return mosqueMapper.toProfileDto(user);
    }

    @Transactional
    public void updateMosqueProfile(MosqueProfileDto dto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email não encontrado."));

        mosqueMapper.updateUserFromDto(dto, user);
        userRepository.save(user);
    }

}

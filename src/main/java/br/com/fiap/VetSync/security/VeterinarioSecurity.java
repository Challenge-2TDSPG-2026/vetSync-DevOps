package br.com.fiap.VetSync.security;

import br.com.fiap.VetSync.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("veterinarioSecurity")
@RequiredArgsConstructor
public class VeterinarioSecurity {

    private final VeterinarioRepository veterinarioRepository;

    public boolean isSelf(Long idVeterinario, Authentication authentication) {
        if (authentication == null || idVeterinario == null) {
            return false;
        }
        return veterinarioRepository.findById(idVeterinario)
                .map(vet -> vet.getDsEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }
}
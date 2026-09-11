package br.com.fiap.VetSync.security;

import br.com.fiap.VetSync.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


@Component("petSecurity")
@RequiredArgsConstructor
public class PetSecurity {

    private final PetRepository petRepository;

    public boolean isOwner(Long idPet, Authentication authentication) {
        if (authentication == null || idPet == null) {
            return false;
        }
        return petRepository.findById(idPet)
                .map(pet -> pet.getTutor() != null
                        && pet.getTutor().getDsEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }
}
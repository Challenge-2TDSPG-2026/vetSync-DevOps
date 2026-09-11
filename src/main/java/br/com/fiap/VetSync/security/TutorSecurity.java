package br.com.fiap.VetSync.security;

import br.com.fiap.VetSync.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("tutorSecurity")
@RequiredArgsConstructor
public class TutorSecurity {

    private final TutorRepository tutorRepository;

    public boolean isSelf(Long idTutor, Authentication authentication) {
        if (authentication == null || idTutor == null) {
            return false;
        }
        return tutorRepository.findById(idTutor)
                .map(tutor -> tutor.getDsEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }
}
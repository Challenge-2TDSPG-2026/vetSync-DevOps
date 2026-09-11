package br.com.fiap.VetSync.security;

import br.com.fiap.VetSync.repository.PlanoItemRepository;
import br.com.fiap.VetSync.repository.PlanoTratamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("planoTratamentoSecurity")
@RequiredArgsConstructor
public class PlanoTratamentoSecurity {

    private final PlanoTratamentoRepository planoTratamentoRepository;
    private final PlanoItemRepository planoItemRepository;

    public boolean isRelacionadoAoPlano(Long idPlano, Authentication authentication) {
        if (authentication == null || idPlano == null) return false;
        return planoTratamentoRepository.findById(idPlano).map(plano -> {
            boolean isVet = plano.getVeterinario() != null
                    && plano.getVeterinario().getDsEmail().equalsIgnoreCase(authentication.getName());
            boolean isTutor = plano.getPet() != null && plano.getPet().getTutor() != null
                    && plano.getPet().getTutor().getDsEmail().equalsIgnoreCase(authentication.getName());
            return isVet || isTutor;
        }).orElse(false);
    }

    public boolean isTutorDoItem(Long idItem, Authentication authentication) {
        if (authentication == null || idItem == null) return false;
        return planoItemRepository.findById(idItem).map(item -> {
            var plano = item.getPlano();
            return plano != null && plano.getPet() != null && plano.getPet().getTutor() != null
                    && plano.getPet().getTutor().getDsEmail().equalsIgnoreCase(authentication.getName());
        }).orElse(false);
    }
}

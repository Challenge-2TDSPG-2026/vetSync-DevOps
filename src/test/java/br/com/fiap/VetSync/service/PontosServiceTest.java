package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.repository.AdminRepository;
import br.com.fiap.VetSync.repository.LancamentoPontosRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PontosServiceTest {
    @Mock
    private LancamentoPontosRepository lancamentoPontosRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private PontosService pontosService;

    @Test
    @DisplayName("Deve lançar pontos pendentes a partir de evento concluído")
    void lancarPendente_Evento() {
        TipoEvento tipo = TipoEvento.builder().nrPontos(25).build();
        EventoSaude evento = EventoSaude.builder().idEvento(1L).tipoEvento(tipo).build();

        when(lancamentoPontosRepository.save(any(LancamentoPontos.class))).thenAnswer(inv -> inv.getArgument(0));

        LancamentoPontos lanc = pontosService.lancarPendente(evento);
        assertThat(lanc.getNrPontos()).isEqualTo(25);
        assertThat(lanc.getDsStatus()).isEqualTo(StatusLancamentoPontos.PENDENTE);
        assertThat(lanc.getEvento()).isEqualTo(evento);
    }

    @Test
    @DisplayName("Deve lançar bônus pendente a partir de plano de tratamento concluído")
    void lancarBonusPendente_Plano() {
        PlanoTratamento plano = PlanoTratamento.builder().idPlano(1L).nrPontosBonus(100).build();
        when(lancamentoPontosRepository.save(any(LancamentoPontos.class))).thenAnswer(inv -> inv.getArgument(0));

        LancamentoPontos lanc = pontosService.lancarBonusPendente(plano);
        assertThat(lanc.getNrPontos()).isEqualTo(100);
        assertThat(lanc.getDsStatus()).isEqualTo(StatusLancamentoPontos.PENDENTE);
        assertThat(lanc.getPlanoTratamento()).isEqualTo(plano);
    }

    @Test
    @DisplayName("Deve liberar pontos pendentes com sucesso")
    void liberar_Sucesso() {
        LancamentoPontos lanc = LancamentoPontos.builder()
                .idLancamento(10L)
                .dsStatus(StatusLancamentoPontos.PENDENTE)
                .build();
        Admin admin = Admin.builder().idAdmin(2L).build();

        when(lancamentoPontosRepository.findById(10L)).thenReturn(Optional.of(lanc));
        when(adminRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(lancamentoPontosRepository.save(any(LancamentoPontos.class))).thenAnswer(inv -> inv.getArgument(0));

        LancamentoPontos liberado = pontosService.liberar(10L, 2L);
        assertThat(liberado.getDsStatus()).isEqualTo(StatusLancamentoPontos.LIBERADO);
        assertThat(liberado.getAdminValidador()).isEqualTo(admin);
    }

    @Test
    @DisplayName("Deve lançar 409 se tentar liberar lançamento já liberado")
    void liberar_JaLiberado() {
        LancamentoPontos lanc = LancamentoPontos.builder()
                .idLancamento(10L)
                .dsStatus(StatusLancamentoPontos.LIBERADO)
                .build();

        when(lancamentoPontosRepository.findById(10L)).thenReturn(Optional.of(lanc));

        assertThatThrownBy(() -> pontosService.liberar(10L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("já está LIBERADO");

        verify(lancamentoPontosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve calcular pontos liberados somando eventos e bônus de planos")
    void calcularPontosLiberados() {
        LancamentoPontos l1 = LancamentoPontos.builder().nrPontos(20).build();
        LancamentoPontos l2 = LancamentoPontos.builder().nrPontos(30).build();
        LancamentoPontos l3 = LancamentoPontos.builder().nrPontos(50).build();

        when(lancamentoPontosRepository.findByEvento_Pet_Tutor_IdTutorAndDsStatus(1L, StatusLancamentoPontos.LIBERADO))
                .thenReturn(List.of(l1, l2));
        when(lancamentoPontosRepository.findByPlanoTratamento_Pet_Tutor_IdTutorAndDsStatus(1L, StatusLancamentoPontos.LIBERADO))
                .thenReturn(List.of(l3));

        int total = pontosService.calcularPontosLiberados(1L);
        assertThat(total).isEqualTo(100);
    }
}
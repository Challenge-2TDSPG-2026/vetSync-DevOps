package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.repository.*;
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
class RecompensaServiceTest {

    @Mock
    private RecompensaRepository recompensaRepository;

    @Mock
    private ResgateRepository resgateRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private TutorService tutorService;

    @Mock
    private PontosService pontosService;

    @InjectMocks
    private RecompensaService recompensaService;

    @Test
    @DisplayName("Deve criar recompensa ativa no catálogo")
    void criar_Sucesso() {
        when(recompensaRepository.save(any(Recompensa.class))).thenAnswer(inv -> inv.getArgument(0));

        Recompensa r = recompensaService.criar("Banho Grátis", "Vale 1 banho", 100, TipoRecompensa.PRODUTO);
        assertThat(r.getNmRecompensa()).isEqualTo("Banho Grátis");
        assertThat(r.getNrCustoPontos()).isEqualTo(100);
        assertThat(r.getFlAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve calcular saldo líquido de pontos (ganhos - resgates validados)")
    void calcularSaldo() {
        when(pontosService.calcularPontosLiberados(1L)).thenReturn(200);

        Recompensa r1 = Recompensa.builder().nrCustoPontos(50).build();
        Resgate val = Resgate.builder().dsStatus(StatusResgate.VALIDADO).recompensa(r1).build();

        Recompensa r2 = Recompensa.builder().nrCustoPontos(80).build();
        Resgate pend = Resgate.builder().dsStatus(StatusResgate.PENDENTE).recompensa(r2).build();

        when(resgateRepository.findByTutor_IdTutorOrderByDtResgateDesc(1L)).thenReturn(List.of(val, pend));

        int saldo = recompensaService.calcularSaldo(1L);
        assertThat(saldo).isEqualTo(150); // 200 - 50 = 150
    }

    @Test
    @DisplayName("Deve solicitar resgate com sucesso quando saldo for suficiente")
    void solicitarResgate_Sucesso() {
        Recompensa rec = Recompensa.builder()
                .idRecompensa(10L)
                .nrCustoPontos(50)
                .flAtivo(true)
                .build();
        Tutor tutor = Tutor.builder().idTutor(1L).build();

        when(recompensaRepository.findById(10L)).thenReturn(Optional.of(rec));
        when(pontosService.calcularPontosLiberados(1L)).thenReturn(100);
        when(resgateRepository.findByTutor_IdTutorOrderByDtResgateDesc(1L)).thenReturn(List.of());
        when(tutorService.buscarPorId(1L)).thenReturn(tutor);
        when(resgateRepository.save(any(Resgate.class))).thenAnswer(inv -> inv.getArgument(0));

        Resgate resgate = recompensaService.solicitarResgate(1L, 10L);
        assertThat(resgate.getDsStatus()).isEqualTo(StatusResgate.PENDENTE);
        assertThat(resgate.getTutor()).isEqualTo(tutor);
        assertThat(resgate.getRecompensa()).isEqualTo(rec);
    }

    @Test
    @DisplayName("Deve falhar ao solicitar resgate com saldo insuficiente")
    void solicitarResgate_SaldoInsuficiente() {
        Recompensa rec = Recompensa.builder()
                .idRecompensa(10L)
                .nrCustoPontos(150)
                .flAtivo(true)
                .build();

        when(recompensaRepository.findById(10L)).thenReturn(Optional.of(rec));
        when(pontosService.calcularPontosLiberados(1L)).thenReturn(100);
        when(resgateRepository.findByTutor_IdTutorOrderByDtResgateDesc(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> recompensaService.solicitarResgate(1L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(resgateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao solicitar resgate de recompensa inativa")
    void solicitarResgate_RecompensaInativa() {
        Recompensa rec = Recompensa.builder()
                .idRecompensa(10L)
                .flAtivo(false)
                .build();

        when(recompensaRepository.findById(10L)).thenReturn(Optional.of(rec));

        assertThatThrownBy(() -> recompensaService.solicitarResgate(1L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não está mais disponível");

        verify(resgateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve validar resgate com sucesso")
    void validar_Sucesso() {
        Resgate resgate = Resgate.builder()
                .idResgate(5L)
                .dsStatus(StatusResgate.PENDENTE)
                .build();
        Veterinario vet = Veterinario.builder().idVeterinario(3L).build();

        when(resgateRepository.findById(5L)).thenReturn(Optional.of(resgate));
        when(veterinarioRepository.findById(3L)).thenReturn(Optional.of(vet));
        when(resgateRepository.save(any(Resgate.class))).thenAnswer(inv -> inv.getArgument(0));

        Resgate validado = recompensaService.validar(5L, 3L, true);
        assertThat(validado.getDsStatus()).isEqualTo(StatusResgate.VALIDADO);
        assertThat(validado.getVeterinarioValidador()).isEqualTo(vet);
    }
}

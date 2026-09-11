package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.repository.RecompensaRepository;
import br.com.fiap.VetSync.repository.ResgateRepository;
import br.com.fiap.VetSync.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecompensaService {

    private final RecompensaRepository recompensaRepository;
    private final ResgateRepository resgateRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final TutorService tutorService;
    private final PontosService pontosService;



    public Recompensa criar(String nome, String descricao, Integer custoPontos, TipoRecompensa tipo) {
        Recompensa recompensa = Recompensa.builder()
                .nmRecompensa(nome)
                .dsDescricao(descricao)
                .nrCustoPontos(custoPontos)
                .dsTipo(tipo)
                .flAtivo(true)
                .build();
        return recompensaRepository.save(recompensa);
    }

    public List<Recompensa> listarAtivas() {
        return recompensaRepository.findByFlAtivoTrue();
    }

    public Recompensa buscarPorId(Long id) {
        return recompensaRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recompensa não encontrada com id: " + id)
        );
    }




    public int calcularSaldo(Long idTutor) {
        int ganhos = pontosService.calcularPontosLiberados(idTutor);

        int gastos = resgateRepository.findByTutor_IdTutorOrderByDtResgateDesc(idTutor).stream()
                .filter(r -> r.getDsStatus() == StatusResgate.VALIDADO)
                .mapToInt(r -> r.getRecompensa().getNrCustoPontos())
                .sum();

        return ganhos - gastos;
    }



    public Resgate solicitarResgate(Long idTutor, Long idRecompensa) {
        Recompensa recompensa = buscarPorId(idRecompensa);
        if (!Boolean.TRUE.equals(recompensa.getFlAtivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Essa recompensa não está mais disponível");
        }
        int saldo = calcularSaldo(idTutor);
        if (saldo < recompensa.getNrCustoPontos()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Saldo insuficiente: você tem " + saldo + " pontos, precisa de " + recompensa.getNrCustoPontos());
        }
        Resgate resgate = Resgate.builder()
                .tutor(tutorService.buscarPorId(idTutor))
                .recompensa(recompensa)
                .dsStatus(StatusResgate.PENDENTE)
                .build();
        return resgateRepository.save(resgate);
    }

    public List<Resgate> listarResgatesDoTutor(Long idTutor) {
        return resgateRepository.findByTutor_IdTutorOrderByDtResgateDesc(idTutor);
    }

    public List<Resgate> listarPendentes() {
        return resgateRepository.findByDsStatusOrderByDtResgateAsc(StatusResgate.PENDENTE);
    }

    public Resgate validar(Long idResgate, Long idVeterinario, boolean aprovado) {
        Resgate resgate = resgateRepository.findById(idResgate).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resgate não encontrado")
        );
        if (resgate.getDsStatus() != StatusResgate.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esse resgate já foi " + resgate.getDsStatus());
        }
        Veterinario vet = veterinarioRepository.findById(idVeterinario).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinário não encontrado")
        );
        resgate.setVeterinarioValidador(vet);
        resgate.setDsStatus(aprovado ? StatusResgate.VALIDADO : StatusResgate.NEGADO);
        return resgateRepository.save(resgate);
    }
}
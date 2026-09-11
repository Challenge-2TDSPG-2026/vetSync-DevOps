package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlanoTratamentoRepositoryTest {

    @Autowired
    private PlanoTratamentoRepository planoTratamentoRepository;

    @Autowired
    private PlanoItemRepository planoItemRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private RacaRepository racaRepository;

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private TipoEventoRepository tipoEventoRepository;

    @Test
    @DisplayName("Deve buscar planos por tutor e veterinário, e itens ordenados por nrOrdem")
    void buscarPlanosEItens() {
        Tutor tutor = tutorRepository.save(Tutor.builder().nmTutor("Daniel").dsEmail("daniel.plano@teste.com").dsCpf("44455566677").dsSenha("pwd").build());
        Especie esp = especieRepository.save(Especie.builder().nmEspecie("Cão Pl").build());
        Raca raca = racaRepository.save(Raca.builder().nmRaca("Pinscher").especie(esp).build());
        Pet pet = petRepository.save(Pet.builder().nmPet("Tobby").dtNascimento(LocalDate.now().minusYears(1)).tutor(tutor).raca(raca).build());
        Clinica clinica = clinicaRepository.save(Clinica.builder().nmClinica("Clinica Pl").dsCnpj("55566677000199").build());
        Veterinario vet = veterinarioRepository.save(Veterinario.builder().nmVeterinario("Dr. Pl").dsEmail("vet.pl@teste.com").nrCrmv("CRMV-55667").dsSenha("pwd").clinica(clinica).build());
        TipoEvento t1 = tipoEventoRepository.save(TipoEvento.builder().nmTipoEvento("Cirurgia").dsCategoria("TERAPEUTICO").build());
        TipoEvento t2 = tipoEventoRepository.save(TipoEvento.builder().nmTipoEvento("Fisioterapia").dsCategoria("TERAPEUTICO").build());

        PlanoTratamento plano = planoTratamentoRepository.save(PlanoTratamento.builder()
                .pet(pet).veterinario(vet).nrPontosBonus(50).dsStatus(StatusPlanoTratamento.EM_ANDAMENTO).dtCriacao(LocalDate.now()).build());

        planoItemRepository.save(PlanoItem.builder().plano(plano).nrOrdem(2).tipoEvento(t2).dsStatus(StatusPlanoItem.PENDENTE).build());
        planoItemRepository.save(PlanoItem.builder().plano(plano).nrOrdem(1).tipoEvento(t1).dsStatus(StatusPlanoItem.PENDENTE).build());

        List<PlanoTratamento> planosTutor = planoTratamentoRepository.findByPet_Tutor_DsEmailOrderByDtCriacaoDesc("daniel.plano@teste.com");
        assertThat(planosTutor).hasSize(1);

        List<PlanoItem> itens = planoItemRepository.findByPlano_IdPlanoOrderByNrOrdemAsc(plano.getIdPlano());
        assertThat(itens).hasSize(2);
        assertThat(itens.get(0).getNrOrdem()).isEqualTo(1);
        assertThat(itens.get(1).getNrOrdem()).isEqualTo(2);
    }
}

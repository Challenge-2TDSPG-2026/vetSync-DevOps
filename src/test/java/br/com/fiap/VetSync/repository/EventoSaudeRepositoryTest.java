package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventoSaudeRepositoryTest {

    @Autowired
    private EventoSaudeRepository eventoSaudeRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private RacaRepository racaRepository;

    @Autowired
    private TipoEventoRepository tipoEventoRepository;

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Test
    @DisplayName("Deve buscar eventos por pet, tutor e veterinário")
    void buscarEventos() {
        Tutor tutor = tutorRepository.save(Tutor.builder()
                .nmTutor("Juliana")
                .dsEmail("juliana.ev@teste.com")
                .dsCpf("55544433322")
                .dsSenha("pwd123")
                .build());

        Especie canino = especieRepository.save(Especie.builder().nmEspecie("Canino").build());
        Raca poodle = racaRepository.save(Raca.builder().nmRaca("Poodle").especie(canino).build());

        Pet pet = petRepository.save(Pet.builder()
                .nmPet("Pipoca")
                .dtNascimento(LocalDate.now().minusYears(1))
                .tutor(tutor)
                .raca(poodle)
                .build());

        Clinica clinica = clinicaRepository.save(Clinica.builder().nmClinica("Clinica Central").dsCnpj("11222333000144").build());

        Veterinario vet = veterinarioRepository.save(Veterinario.builder()
                .nmVeterinario("Dr. Lucas")
                .dsEmail("lucas.ev@teste.com")
                .nrCrmv("CRMV-11223")
                .dsSenha("pwd123")
                .clinica(clinica)
                .build());

        TipoEvento tipo = tipoEventoRepository.save(TipoEvento.builder().nmTipoEvento("Checkup").dsCategoria("PREVENTIVO").nrPontos(10).build());

        eventoSaudeRepository.save(EventoSaude.builder()
                .pet(pet)
                .tipoEvento(tipo)
                .veterinario(vet)
                .dtEvento(LocalDate.now().minusDays(3))
                .dsStatus(StatusEvento.CONCLUIDO)
                .vlCusto(new BigDecimal("120.00"))
                .build());

        eventoSaudeRepository.save(EventoSaude.builder()
                .pet(pet)
                .tipoEvento(tipo)
                .veterinario(vet)
                .dtEvento(LocalDate.now())
                .dsStatus(StatusEvento.AGENDADO)
                .vlCusto(BigDecimal.ZERO)
                .build());

        List<EventoSaude> porPet = eventoSaudeRepository.findByPet_IdPet(pet.getIdPet());
        assertThat(porPet).hasSize(2);

        List<EventoSaude> porTutor = eventoSaudeRepository.findByPet_Tutor_DsEmailOrderByDtEventoDesc("juliana.ev@teste.com");
        assertThat(porTutor).hasSize(2);
        assertThat(porTutor.get(0).getDtEvento()).isAfterOrEqualTo(porTutor.get(1).getDtEvento());

        List<EventoSaude> porVet = eventoSaudeRepository.findByVeterinario_DsEmailOrderByDtEventoDesc("lucas.ev@teste.com");
        assertThat(porVet).hasSize(2);
    }
}

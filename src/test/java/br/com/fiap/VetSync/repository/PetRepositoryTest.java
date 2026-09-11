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
class PetRepositoryTest {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private RacaRepository racaRepository;

    @Test
    @DisplayName("Deve buscar pets por id do tutor")
    void findByTutor_IdTutor() {
        Tutor tutor = tutorRepository.save(Tutor.builder()
                .nmTutor("Roberta")
                .dsEmail("roberta.pet@teste.com")
                .dsCpf("12312312399")
                .dsSenha("pwd123")
                .build());

        Especie gato = especieRepository.save(Especie.builder().nmEspecie("Felino").build());
        Raca persa = racaRepository.save(Raca.builder().nmRaca("Persa").especie(gato).build());

        petRepository.save(Pet.builder()
                .nmPet("Mimi")
                .dtNascimento(LocalDate.now().minusYears(2))
                .nrPesoKg(new BigDecimal("4.2"))
                .tutor(tutor)
                .raca(persa)
                .build());

        petRepository.save(Pet.builder()
                .nmPet("Frajola")
                .dtNascimento(LocalDate.now().minusYears(1))
                .nrPesoKg(new BigDecimal("3.8"))
                .tutor(tutor)
                .raca(persa)
                .build());

        List<Pet> pets = petRepository.findByTutor_IdTutor(tutor.getIdTutor());
        assertThat(pets).hasSize(2);
        assertThat(pets).extracting(Pet::getNmPet).containsExactlyInAnyOrder("Mimi", "Frajola");
    }
}

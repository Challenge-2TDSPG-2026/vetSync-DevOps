package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Tutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TutorRepositoryTest {

    @Autowired
    private TutorRepository tutorRepository;

    @Test
    @DisplayName("Deve salvar e buscar tutor por e-mail")
    void findByDsEmail() {
        Tutor tutor = Tutor.builder()
                .nmTutor("Mariana Santos")
                .dsEmail("mariana.repo@teste.com")
                .dsCpf("99988877766")
                .nrTelefone("11988887777")
                .dsSenha("segredo123")
                .build();
        tutorRepository.save(tutor);

        Optional<Tutor> opt = tutorRepository.findByDsEmail("mariana.repo@teste.com");
        assertThat(opt).isPresent();
        assertThat(opt.get().getNmTutor()).isEqualTo("Mariana Santos");
    }

    @Test
    @DisplayName("Deve verificar existência por e-mail")
    void existsByDsEmail() {
        Tutor tutor = Tutor.builder()
                .nmTutor("Carlos Alberto")
                .dsEmail("carlos.repo@teste.com")
                .dsCpf("11122233344")
                .nrTelefone("11977778888")
                .dsSenha("segredo123")
                .build();
        tutorRepository.save(tutor);

        assertThat(tutorRepository.existsByDsEmail("carlos.repo@teste.com")).isTrue();
        assertThat(tutorRepository.existsByDsEmail("inexistente@teste.com")).isFalse();
    }
}

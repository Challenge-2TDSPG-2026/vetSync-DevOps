package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.Medicamento;
import br.com.fiap.VetSync.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    public Medicamento criar(String nmMedicamento, String dsPrincipio, BigDecimal vlPrecoRef) {
        Medicamento medicamento = Medicamento.builder()
                .nmMedicamento(nmMedicamento)
                .dsPrincipio(dsPrincipio)
                .vlPrecoRef(vlPrecoRef)
                .build();
        return medicamentoRepository.save(medicamento);
    }

    public Medicamento buscarPorId(Long id) {
        return medicamentoRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento não encontrado com id: " + id));
    }

    public List<Medicamento> listar() {
        return medicamentoRepository.findAll();
    }

    public Medicamento atualizar(Long id, String nmMedicamento, String dsPrincipio, BigDecimal vlPrecoRef) {
        Medicamento medicamento = buscarPorId(id);
        medicamento.setNmMedicamento(nmMedicamento);
        medicamento.setDsPrincipio(dsPrincipio);
        medicamento.setVlPrecoRef(vlPrecoRef);
        return medicamentoRepository.save(medicamento);
    }

    public void deletar(Long id) {
        Medicamento medicamento = buscarPorId(id);
        medicamentoRepository.delete(medicamento);
    }
}
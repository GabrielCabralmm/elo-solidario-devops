package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.dto.DoacaoRequest;
import br.com.fiap.elosolidario.dto.DoacaoResponse;
import br.com.fiap.elosolidario.dto.ItemDoacaoRequest;
import br.com.fiap.elosolidario.exception.AcessoNegadoException;
import br.com.fiap.elosolidario.exception.RecursoNaoEncontradoException;
import br.com.fiap.elosolidario.exception.RegraNegocioException;
import br.com.fiap.elosolidario.model.*;
import br.com.fiap.elosolidario.repository.AlimentoRepository;
import br.com.fiap.elosolidario.repository.DoacaoRepository;
import br.com.fiap.elosolidario.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final AlimentoRepository alimentoRepository;
    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public Page<DoacaoResponse> listar(StatusDoacao status, String bairro, Long idCategoria, Pageable pageable) {
        String filtroBairro = bairro == null || bairro.isBlank() ? null : bairro.trim();
        return doacaoRepository.buscar(status, filtroBairro, idCategoria, pageable).map(DoacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<DoacaoResponse> listarDoDoador(Usuario doador, Pageable pageable) {
        return doacaoRepository.findByDoador(doador, pageable).map(DoacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public DoacaoResponse buscar(Long id) {
        return DoacaoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public DoacaoResponse criar(DoacaoRequest request, Usuario doador) {
        Map<Long, Alimento> alimentos = alimentosValidos(request);

        Doacao doacao = new Doacao();
        doacao.setDoador(doador);
        doacao.setStatus(StatusDoacao.DISPONIVEL);
        doacao.setDtPublicacao(LocalDateTime.now());
        preencherDados(doacao, request);

        for (ItemDoacaoRequest itemRequest : request.itens()) {
            ItemDoacao item = new ItemDoacao();
            preencherItem(item, itemRequest, alimentos.get(itemRequest.idAlimento()));
            doacao.adicionarItem(item);
        }

        return DoacaoResponse.de(doacaoRepository.save(doacao));
    }

    @Transactional
    public DoacaoResponse atualizar(Long id, DoacaoRequest request, Usuario usuario) {
        Doacao doacao = buscarEntidade(id);
        validarDono(doacao, usuario);
        validarDisponivel(doacao, "editada");

        Map<Long, Alimento> alimentos = alimentosValidos(request);
        Map<Long, ItemDoacaoRequest> novosItens = request.itens().stream()
                .collect(Collectors.toMap(ItemDoacaoRequest::idAlimento, Function.identity()));

        preencherDados(doacao, request);
        doacao.getItens().removeIf(item -> !novosItens.containsKey(item.getAlimento().getId()));

        for (ItemDoacaoRequest itemRequest : request.itens()) {
            ItemDoacao item = doacao.getItens().stream()
                    .filter(existente -> existente.getAlimento().getId().equals(itemRequest.idAlimento()))
                    .findFirst()
                    .orElseGet(() -> {
                        ItemDoacao novo = new ItemDoacao();
                        doacao.adicionarItem(novo);
                        return novo;
                    });
            preencherItem(item, itemRequest, alimentos.get(itemRequest.idAlimento()));
        }

        return DoacaoResponse.de(doacaoRepository.save(doacao));
    }

    @Transactional
    public void excluir(Long id, Usuario usuario) {
        Doacao doacao = buscarEntidade(id);
        validarDono(doacao, usuario);
        validarDisponivel(doacao, "excluída");

        if (reservaRepository.existsByDoacao(doacao)) {
            throw new RegraNegocioException("A doação possui histórico de reservas e não pode ser excluída");
        }
        doacaoRepository.delete(doacao);
    }

    public Doacao buscarEntidade(Long id) {
        return doacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doação " + id + " não encontrada"));
    }

    public void validarDono(Doacao doacao, Usuario usuario) {
        if (!usuario.isAdmin() && !doacao.getDoador().getId().equals(usuario.getId())) {
            throw new AcessoNegadoException("Somente o doador responsável pode alterar esta doação");
        }
    }

    private void validarDisponivel(Doacao doacao, String acao) {
        if (doacao.getStatus() != StatusDoacao.DISPONIVEL) {
            throw new RegraNegocioException("Somente doações disponíveis podem ser " + acao
                    + ". Status atual: " + doacao.getStatus());
        }
    }

    private Map<Long, Alimento> alimentosValidos(DoacaoRequest request) {
        Set<Long> ids = new HashSet<>();
        for (ItemDoacaoRequest item : request.itens()) {
            if (!ids.add(item.idAlimento())) {
                throw new RegraNegocioException("O alimento " + item.idAlimento() + " foi informado mais de uma vez");
            }
        }

        Map<Long, Alimento> alimentos = alimentoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Alimento::getId, Function.identity()));

        for (Long idAlimento : ids) {
            Alimento alimento = alimentos.get(idAlimento);
            if (alimento == null) {
                throw new RecursoNaoEncontradoException("Alimento " + idAlimento + " não encontrado no catálogo");
            }
            if (!alimento.getAtivo()) {
                throw new RegraNegocioException("O alimento " + alimento.getNome() + " não está ativo no catálogo");
            }
        }
        return alimentos;
    }

    private void preencherDados(Doacao doacao, DoacaoRequest request) {
        doacao.setTitulo(request.titulo().trim());
        doacao.setBairro(request.bairro().trim());
        doacao.setLocalRetirada(request.localRetirada().trim());
        doacao.setObservacao(request.observacao());
    }

    private void preencherItem(ItemDoacao item, ItemDoacaoRequest request, Alimento alimento) {
        item.setAlimento(alimento);
        item.setQuantidade(request.quantidade());
        item.setUnidade(request.unidade());
        item.setPesoKg(request.pesoKg());
        item.setDtValidade(request.dtValidade());
    }
}

package br.com.fiap.elosolidario.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/doacoes/1/reservas");

    @Test
    void deveTraduzirErroDoGatilhoOracleParaConflito() {
        var erro = new DataIntegrityViolationException("erro",
                new SQLException("ORA-20011: A doação 1 não está disponível para reserva.\nORA-06512: at line 9", "72000", 20011));

        var resposta = handler.banco(erro, request);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().mensagem()).isEqualTo("A doação 1 não está disponível para reserva.");
    }

    @Test
    void deveTraduzirErroDoGatilhoSqlServerParaConflito() {
        var erro = new DataIntegrityViolationException("erro",
                new SQLException("A doação 1 não está disponível para reserva.", "S0001", 50011));

        var resposta = handler.banco(erro, request);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().mensagem()).isEqualTo("A doação 1 não está disponível para reserva.");
    }

    @Test
    void naoDeveExporDetalhesDeOutrosErrosDeBanco() {
        var erro = new DataIntegrityViolationException("erro", new SQLException("FK violada: TB_ELO_X", "23000", 547));

        var resposta = handler.banco(erro, request);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().mensagem()).doesNotContain("TB_ELO");
    }
}

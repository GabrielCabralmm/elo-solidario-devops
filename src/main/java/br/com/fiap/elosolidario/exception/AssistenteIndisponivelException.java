package br.com.fiap.elosolidario.exception;

public class AssistenteIndisponivelException extends RuntimeException {

    public AssistenteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

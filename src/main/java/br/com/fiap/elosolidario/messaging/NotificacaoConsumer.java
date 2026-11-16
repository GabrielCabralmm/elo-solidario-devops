package br.com.fiap.elosolidario.messaging;

import br.com.fiap.elosolidario.config.RabbitConfig;
import br.com.fiap.elosolidario.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoConsumer {

    private final NotificacaoService notificacaoService;

    @RabbitListener(queues = RabbitConfig.FILA_NOTIFICACOES)
    public void consumir(ReservaEvento evento) {
        log.info("Evento {} recebido da fila para a reserva {}", evento.tipo(), evento.idReserva());
        notificacaoService.registrar(evento);
    }
}

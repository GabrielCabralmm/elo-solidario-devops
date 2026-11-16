package br.com.fiap.elosolidario.messaging;

import br.com.fiap.elosolidario.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaEventoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener
    public void publicar(ReservaEvento evento) {
        String routingKey = "reserva." + evento.tipo().name().toLowerCase();
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_RESERVAS, routingKey, evento);
            log.info("Evento {} publicado para a reserva {}", evento.tipo(), evento.idReserva());
        } catch (AmqpException e) {
            log.error("Não foi possível publicar o evento {} da reserva {}: {}",
                    evento.tipo(), evento.idReserva(), e.getMessage());
        }
    }
}

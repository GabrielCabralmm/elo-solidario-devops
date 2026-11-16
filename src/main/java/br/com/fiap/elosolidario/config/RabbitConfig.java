package br.com.fiap.elosolidario.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_RESERVAS = "elo.reservas";
    public static final String FILA_NOTIFICACOES = "elo.notificacoes";
    public static final String FILA_NOTIFICACOES_ERRO = "elo.notificacoes.erro";

    @Bean
    public TopicExchange exchangeReservas() {
        return new TopicExchange(EXCHANGE_RESERVAS);
    }

    @Bean
    public Queue filaNotificacoes() {
        return QueueBuilder.durable(FILA_NOTIFICACOES)
                .deadLetterExchange("")
                .deadLetterRoutingKey(FILA_NOTIFICACOES_ERRO)
                .build();
    }

    @Bean
    public Queue filaNotificacoesErro() {
        return QueueBuilder.durable(FILA_NOTIFICACOES_ERRO).build();
    }

    @Bean
    public Binding bindingNotificacoes(Queue filaNotificacoes, TopicExchange exchangeReservas) {
        return BindingBuilder.bind(filaNotificacoes).to(exchangeReservas).with("reserva.#");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

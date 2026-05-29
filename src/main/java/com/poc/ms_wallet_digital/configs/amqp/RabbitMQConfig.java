package com.poc.ms_wallet_digital.configs.amqp;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nomes dos Tópicos (Exchanges)
    public static final String REQUESTS_TOPIC = "wallet-transaction-requests-topic";
    public static final String RESULTS_TOPIC = "wallet-transaction-results-topic";

    // Nomes das Filas (Queues)
    public static final String TRANSFER_COMMANDS_QUEUE = "wallet-ms-transfer-commands-queue";
    public static final String CALLBACK_QUEUE = "orchestrator-wallet-callback-queue";

    // 🚀 1. Declaração dos Tópicos (Exchanges do tipo Topic)
    @Bean
    public TopicExchange requestsTopic() {
        return new TopicExchange(REQUESTS_TOPIC);
    }

    @Bean
    public TopicExchange resultsTopic() {
        return new TopicExchange(RESULTS_TOPIC);
    }

    // 🚀 2. Declaração das Filas Duráveis (Não somem se o Docker reiniciar)
    @Bean
    public Queue transferCommandsQueue() {
        return QueueBuilder.durable(TRANSFER_COMMANDS_QUEUE).build();
    }

    @Bean
    public Queue callbackQueue() {
        return QueueBuilder.durable(CALLBACK_QUEUE).build();
    }

    // 🚀 3. Amarrações (Bindings) - Conecta as Filas aos seus respectivos Tópicos
    @Bean
    public Binding bindingRequests() {
        return BindingBuilder.bind(transferCommandsQueue())
                .to(requestsTopic())
                .with("wallet.transfer.command"); // Routing Key de envio
    }

    @Bean
    public Binding bindingResults() {
        return BindingBuilder.bind(callbackQueue())
                .to(resultsTopic())
                .with("wallet.transaction.result"); // Routing Key de retorno
    }
}

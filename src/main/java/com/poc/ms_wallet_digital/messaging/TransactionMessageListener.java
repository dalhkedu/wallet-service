package com.poc.ms_wallet_digital.messaging;


import com.poc.ms_wallet_digital.configs.amqp.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionMessageListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionMessageListener.class);
    private final RabbitTemplate rabbitTemplate;

    public TransactionMessageListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // 📥 CONSUMIDOR: Escuta nativamente a fila de comandos de transferência
    @RabbitListener(queues = RabbitMQConfig.TRANSFER_COMMANDS_QUEUE)
    public void consumeTransferCommand(String payload) {
        log.info("Mensagem recebida da fila de comandos: {}", payload);

        try {
            // Lógica de negócio: Executa o débito/crédito no banco PostgreSQL...

            String transactionResultJson = "{\"status\": \"SUCCESS\", \"walletId\": \"uuid-da-carteira\"}";

            // 📤 PRODUTOR: Posta o resultado da transação de volta no Tópico de Resultados
            log.info("Publicando resultado no tópico: {}", RabbitMQConfig.RESULTS_TOPIC);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RESULTS_TOPIC,
                    "wallet.transaction.result", // Routing Key correspondente
                    transactionResultJson
            );

        } catch (Exception e) {
            log.error("Erro catastrófico ao processar comando de transferência", e);
            // O Spring se encarrega de descartar ou mandar para uma DLQ baseado nas propriedades
        }
    }
}

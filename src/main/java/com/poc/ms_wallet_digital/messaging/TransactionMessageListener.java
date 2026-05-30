package com.poc.ms_wallet_digital.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.ms_wallet_digital.configs.amqp.RabbitMQConfig;
import com.poc.ms_wallet_digital.controllers.requests.TransactionRequestDTO;
import com.poc.ms_wallet_digital.controllers.responses.TransactionResponseDTO;
import com.poc.ms_wallet_digital.services.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionMessageListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionMessageListener.class);
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final WalletService walletService;

    public TransactionMessageListener(RabbitTemplate rabbitTemplate,
                                      ObjectMapper objectMapper,
                                      WalletService walletService) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.walletService = walletService;
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSFER_COMMANDS_QUEUE)
    public void consumeTransferCommand(String payload) {
        log.info("Received transfer command message payload from queue");

        try {
            var request = objectMapper.readValue(payload, TransactionRequestDTO.class);

            MDC.put("correlation_id", request.getTransferId());
            MDC.put("flow", "async_" + request.getType().toString().toLowerCase());
            MDC.put("wallet_id", request.getId().toString());

            log.info("Processing ledger transaction. Type: {}, Amount: {}", request.getType(), request.getAmount());

            TransactionResponseDTO response = walletService.processTransaction(request);

            log.info("Ledger transaction finalized with status: {}", response.status());

            String responseJson = objectMapper.writeValueAsString(response);

            log.info("Publishing processing result to topic: {}, routing key: {}",
                    RabbitMQConfig.RESULTS_TOPIC, "wallet.transaction.result");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RESULTS_TOPIC,
                    "wallet.transaction.result",
                    responseJson
            );

        } catch (Exception e) {
            log.error("Critical error while processing async transfer command saga branch: {}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }
}
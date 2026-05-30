package com.poc.ms_wallet_digital.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.ms_wallet_digital.configs.amqp.RabbitMQConfig;
import com.poc.ms_wallet_digital.controllers.requests.CounterpartyRequestDTO;
import com.poc.ms_wallet_digital.controllers.requests.TransactionRequestDTO;
import com.poc.ms_wallet_digital.controllers.responses.TransactionResponseDTO;
import com.poc.ms_wallet_digital.enums.StatusEnum;
import com.poc.ms_wallet_digital.enums.TransactionTypeEnum;
import com.poc.ms_wallet_digital.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class TransactionMessageListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private WalletService walletService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TransactionMessageListener transactionMessageListener;

    private static final UUID TEST_WALLET_ID = UUID.randomUUID();
    private static final String TEST_TRANSFER_ID = "tx-async-999";
    private static final UUID TEST_COUNTERPARTY_ACCOUNT = UUID.randomUUID();
    private String validJsonPayload;
    private TransactionRequestDTO mappedRequest;

    @BeforeEach
    void setUp() throws Exception {
        mappedRequest = new TransactionRequestDTO(
                TEST_TRANSFER_ID,
                TEST_WALLET_ID,
                TransactionTypeEnum.TRANSFER,
                new BigDecimal("250.00"),
                new CounterpartyRequestDTO(TEST_COUNTERPARTY_ACCOUNT)
        );

        validJsonPayload = objectMapper.writeValueAsString(mappedRequest);
    }

    @Test
    @DisplayName("Should consume message, process transaction through service, and publish COMPLETED result back to RabbitMQ")
    void shouldConsumeAndPublishSuccessfulResult() throws Exception {

        var mockResponse = new TransactionResponseDTO(TEST_TRANSFER_ID, StatusEnum.COMPLETED);
        String expectedOutboundJson = objectMapper.writeValueAsString(mockResponse);

        Mockito.when(walletService.processTransaction(any(TransactionRequestDTO.class)))
                .thenReturn(mockResponse);

        transactionMessageListener.consumeTransferCommand(validJsonPayload);

        ArgumentCaptor<TransactionRequestDTO> requestCaptor = ArgumentCaptor.forClass(TransactionRequestDTO.class);
        Mockito.verify(walletService).processTransaction(requestCaptor.capture());

        TransactionRequestDTO capturedRequest = requestCaptor.getValue();
        assertEquals(TEST_TRANSFER_ID, capturedRequest.getTransferId());
        assertEquals(new BigDecimal("250.00"), capturedRequest.getAmount());
        assertEquals(TEST_WALLET_ID.toString(), capturedRequest.getId().toString());

        Mockito.verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.RESULTS_TOPIC),
                eq("wallet.transaction.result"),
                eq(expectedOutboundJson)
        );
    }

    @Test
    @DisplayName("Should publish FAILED result message back to RabbitMQ when ledger engine detects business failure")
    void shouldPublishFailedResultWhenBusinessRuleFails() throws Exception {

        var mockResponse = new TransactionResponseDTO(TEST_TRANSFER_ID, StatusEnum.FAILED);
        String expectedOutboundJson = objectMapper.writeValueAsString(mockResponse);

        Mockito.when(walletService.processTransaction(any(TransactionRequestDTO.class)))
                .thenReturn(mockResponse);

        transactionMessageListener.consumeTransferCommand(validJsonPayload);

        Mockito.verify(walletService).processTransaction(any(TransactionRequestDTO.class));
        Mockito.verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.RESULTS_TOPIC),
                eq("wallet.transaction.result"),
                eq(expectedOutboundJson)
        );
    }

    @Test
    @DisplayName("Should gracefully handle and log exception without crashing or publishing when payload is malformed")
    void shouldHandleExceptionGracefullyOnMalformedJson() {
        String malformedJson = "{ invalid-json-payload : missing-quotes }";

        transactionMessageListener.consumeTransferCommand(malformedJson);

        Mockito.verifyNoInteractions(walletService);
        Mockito.verifyNoInteractions(rabbitTemplate);
    }
}
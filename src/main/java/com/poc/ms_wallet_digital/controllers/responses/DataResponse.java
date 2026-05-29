package com.poc.ms_wallet_digital.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope genérico padrão para todas as respostas da API da Carteira Digital.
 * Garante a propriedade "data" na raiz do JSON.
 *
 * @param <T> O tipo do payload de resposta interno (ex: WalletDTO, TransactionDTO, etc.)
 */
public record DataResponse<T>(
        @JsonProperty("data") T data
) {

    public static <T> DataResponse<T> of(T data) {
        return new DataResponse<>(data);
    }
}
package com.github.masterweber.walletservice.domain.wallet.model.request;

import com.github.masterweber.walletservice.domain.wallet.model.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class WalletOperationRequest {

    @NotNull(message = "walletId is required")
    private UUID walletId;

    @NotNull(message = "operationType is required")
    private OperationType operationType;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private Long amount;

}

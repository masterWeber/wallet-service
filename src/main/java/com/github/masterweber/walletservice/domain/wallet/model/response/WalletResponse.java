package com.github.masterweber.walletservice.domain.wallet.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletResponse {

    UUID walletId;
    Long balance;

}

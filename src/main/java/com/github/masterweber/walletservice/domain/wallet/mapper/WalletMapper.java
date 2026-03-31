package com.github.masterweber.walletservice.domain.wallet.mapper;

import com.github.masterweber.walletservice.domain.wallet.entity.Wallet;
import com.github.masterweber.walletservice.domain.wallet.model.response.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WalletMapper {

    @Mapping(target = "walletId", source = "id")
    WalletResponse toResponse(Wallet wallet);

}

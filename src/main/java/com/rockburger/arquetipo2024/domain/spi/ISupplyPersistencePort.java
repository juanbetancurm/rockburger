package com.rockburger.arquetipo2024.domain.spi;

import com.rockburger.arquetipo2024.domain.model.SupplyModel;

public interface ISupplyPersistencePort {
    SupplyModel saveSupply(SupplyModel supplyModel);
    void updateArticleQuantity(Long articleId, int quantityToAdd);
}


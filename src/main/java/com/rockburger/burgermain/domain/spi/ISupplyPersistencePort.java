package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.domain.model.SupplyModel;

public interface ISupplyPersistencePort {
    SupplyModel saveSupply(SupplyModel supplyModel);
    void updateArticleQuantity(Long articleId, int quantityToAdd);
}


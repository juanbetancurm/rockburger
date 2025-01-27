package com.rockburger.arquetipo2024.domain.api.usecase;

import com.rockburger.arquetipo2024.domain.api.ISupplyServicePort;
import com.rockburger.arquetipo2024.domain.exception.SupplyOperationException;
import com.rockburger.arquetipo2024.domain.model.SupplyModel;
import com.rockburger.arquetipo2024.domain.spi.ISupplyPersistencePort;

public class SupplyUseCase implements ISupplyServicePort {
    private final ISupplyPersistencePort supplyPersistencePort;

    public SupplyUseCase(ISupplyPersistencePort supplyPersistencePort) {
        this.supplyPersistencePort = supplyPersistencePort;
    }

    @Override
    public SupplyModel addSupply(SupplyModel supplyModel) {
        try {
            // Domain validation
            supplyModel.validateSupply();

            // Save supply record
            SupplyModel savedSupply = supplyPersistencePort.saveSupply(supplyModel);

            // Update article quantity
            supplyPersistencePort.updateArticleQuantity(
                    supplyModel.getArticle().getId(),
                    supplyModel.getQuantity()
            );

            return savedSupply;
        } catch (Exception e) {
            throw new SupplyOperationException("Failed to process supply operation", e);
        }
    }
}
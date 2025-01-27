package com.rockburger.arquetipo2024.domain.api;


import com.rockburger.arquetipo2024.domain.model.SupplyModel;

public interface ISupplyServicePort {
    SupplyModel addSupply(SupplyModel supplyModel);
}

package com.rockburger.burgermain.domain.api;


import com.rockburger.burgermain.domain.model.SupplyModel;

public interface ISupplyServicePort {
    SupplyModel addSupply(SupplyModel supplyModel);
}

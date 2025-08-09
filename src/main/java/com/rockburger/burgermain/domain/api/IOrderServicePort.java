package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.OrderModel;
import com.rockburger.burgermain.domain.model.SalesSummaryModel;
import java.time.LocalDate;

public interface IOrderServicePort {
    OrderModel completePurchase(OrderModel orderModel);
    SalesSummaryModel getDailySalesSummary(LocalDate date);
}
package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.domain.model.OrderModel;
import com.rockburger.burgermain.domain.model.SalesSummaryModel;
import java.time.LocalDate;

public interface IOrderPersistencePort {
    OrderModel saveOrder(OrderModel orderModel);
    SalesSummaryModel getDailySalesSummary(LocalDate date);
}

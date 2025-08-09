package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.request.CompleteOrderRequest;
import com.rockburger.burgermain.domain.model.OrderModel;

public interface IOrderRequestMapper {
    OrderModel toModel(CompleteOrderRequest request, Long userId);
}

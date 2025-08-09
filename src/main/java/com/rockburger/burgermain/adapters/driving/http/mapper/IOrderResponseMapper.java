package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.response.OrderResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.SalesSummaryResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.ProductAvailabilityResponse;
import com.rockburger.burgermain.domain.model.OrderModel;
import com.rockburger.burgermain.domain.model.SalesSummaryModel;
import com.rockburger.burgermain.domain.model.ArticleModel;

import java.util.List;

public interface IOrderResponseMapper {
    OrderResponse toResponse(OrderModel orderModel);
    SalesSummaryResponse toResponse(SalesSummaryModel salesSummaryModel);
    ProductAvailabilityResponse toAvailabilityResponse(ArticleModel articleModel);
    List<ProductAvailabilityResponse> toAvailabilityResponseList(List<ArticleModel> articles);
}

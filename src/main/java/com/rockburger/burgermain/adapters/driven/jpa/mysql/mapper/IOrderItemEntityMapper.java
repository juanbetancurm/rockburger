package com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.OrderItemEntity;
import com.rockburger.burgermain.domain.model.OrderItemModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IOrderItemEntityMapper {
    @Mapping(target = "order", ignore = true)
    OrderItemEntity toEntity(OrderItemModel orderItemModel);

    @Mapping(target = "orderId", source = "order.id")
    OrderItemModel toModel(OrderItemEntity orderItemEntity);
}

package com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.OrderEntity;
import com.rockburger.burgermain.domain.model.OrderModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {IOrderItemEntityMapper.class})
public interface IOrderEntityMapper {
    OrderEntity toEntity(OrderModel orderModel);
    OrderModel toModel(OrderEntity orderEntity);
}

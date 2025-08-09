package com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.OrderEntity;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.OrderItemEntity;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper.IOrderEntityMapper;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.IOrderRepository;
import com.rockburger.burgermain.domain.model.OrderModel;
import com.rockburger.burgermain.domain.model.SalesSummaryModel;
import com.rockburger.burgermain.domain.spi.IOrderPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class OrderAdapter implements IOrderPersistencePort {
    private static final Logger logger = LoggerFactory.getLogger(OrderAdapter.class);

    private final IOrderRepository orderRepository;
    private final IOrderEntityMapper orderEntityMapper;

    public OrderAdapter(IOrderRepository orderRepository, IOrderEntityMapper orderEntityMapper) {
        this.orderRepository = orderRepository;
        this.orderEntityMapper = orderEntityMapper;
    }

    @Override
    public OrderModel saveOrder(OrderModel orderModel) {
        logger.info("Saving order in persistence layer: {}", orderModel);

        // Convert to entity
        OrderEntity orderEntity = orderEntityMapper.toEntity(orderModel);

        // CRITICAL: Set up bidirectional relationships
        if (orderEntity.getItems() != null) {
            for (OrderItemEntity item : orderEntity.getItems()) {
                item.setOrder(orderEntity);  // This fixes the null order_id issue
            }
        }

        // Save the order (will cascade to items)
        OrderEntity savedOrder = orderRepository.save(orderEntity);

        logger.info("Order saved successfully with ID: {}", savedOrder.getId());

        return orderEntityMapper.toModel(savedOrder);
    }

    @Override
    public SalesSummaryModel getDailySalesSummary(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        int totalOrders = orderRepository.countOrdersByDateRange(startOfDay, endOfDay);
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByDateRange(startOfDay, endOfDay);

        return new SalesSummaryModel(date, totalOrders, totalRevenue);
    }
}

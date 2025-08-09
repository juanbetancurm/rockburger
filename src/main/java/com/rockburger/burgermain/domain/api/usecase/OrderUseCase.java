package com.rockburger.burgermain.domain.api.usecase;

import com.rockburger.burgermain.domain.api.IOrderServicePort;
import com.rockburger.burgermain.domain.exception.InsufficientStockException;
import com.rockburger.burgermain.domain.exception.NotFoundException;
import com.rockburger.burgermain.domain.model.ArticleModel;
import com.rockburger.burgermain.domain.model.OrderModel;
import com.rockburger.burgermain.domain.model.OrderItemModel;
import com.rockburger.burgermain.domain.model.SalesSummaryModel;
import com.rockburger.burgermain.domain.spi.IOrderPersistencePort;
import com.rockburger.burgermain.domain.spi.IArticlePersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public class OrderUseCase implements IOrderServicePort {
    private static final Logger logger = LoggerFactory.getLogger(OrderUseCase.class);

    private final IOrderPersistencePort orderPersistencePort;
    private final IArticlePersistencePort articlePersistencePort;

    public OrderUseCase(IOrderPersistencePort orderPersistencePort,
                        IArticlePersistencePort articlePersistencePort) {
        this.orderPersistencePort = orderPersistencePort;
        this.articlePersistencePort = articlePersistencePort;
    }

    @Override
    @Transactional
    public OrderModel completePurchase(OrderModel orderModel) {
        logger.info("Completing purchase for user ID: {}", orderModel.getUserId());

        // Validate stock availability first
        validateStockAvailability(orderModel.getItems());

        // Update inventory for each item
        for (OrderItemModel item : orderModel.getItems()) {
            updateArticleInventory(item.getArticleId(), item.getQuantity());
        }

        // Save the order
        OrderModel savedOrder = orderPersistencePort.saveOrder(orderModel);
        logger.info("Order completed successfully with ID: {}", savedOrder.getId());

        return savedOrder;
    }

    private void validateStockAvailability(List<OrderItemModel> items) {
        for (OrderItemModel item : items) {
            ArticleModel article = articlePersistencePort.findById(item.getArticleId())
                    .orElseThrow(() -> new NotFoundException("Article not found: " + item.getArticleId()));

            if (article.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for article '%s'. Available: %d, Requested: %d",
                                article.getName(), article.getQuantity(), item.getQuantity()));
            }
        }
    }

    @Override
    public SalesSummaryModel getDailySalesSummary(LocalDate date) {
        logger.info("Getting daily sales summary for date: {}", date);
        return orderPersistencePort.getDailySalesSummary(date);
    }

    private void updateArticleInventory(Long articleId, int quantitySold) {
        articlePersistencePort.findById(articleId)
                .ifPresentOrElse(
                        article -> {
                            int newQuantity = article.getQuantity() - quantitySold;
                            if (newQuantity < 0) {
                                logger.warn("Article {} will have negative inventory: {}", articleId, newQuantity);
                            }
                            article.setQuantity(newQuantity);
                            articlePersistencePort.save(article);
                            logger.info("Updated inventory for article {} - reduced by {}", articleId, quantitySold);
                        },
                        () -> logger.error("Article not found for inventory update: {}", articleId)
                );
    }
}

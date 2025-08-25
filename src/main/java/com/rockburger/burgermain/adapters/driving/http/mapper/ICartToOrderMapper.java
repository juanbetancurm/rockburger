package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driven.feign.dto.CartResponse;
import com.rockburger.burgermain.domain.model.OrderModel;

/**
 * Mapper interface for converting cart-related DTOs to order domain models.
 * This mapper is specifically used by the checkout functionality to transform
 * cart data from the cart service into order data for the order service.
 */
public interface ICartToOrderMapper {

	/**
	 * Converts a CartResponse from the cart service to an OrderModel for order processing.
	 *
	 * @param cartResponse The cart response containing cart items and totals
	 * @param userId The ID of the user making the purchase
	 * @return OrderModel ready for processing by OrderUseCase.completePurchase()
	 * @throws IllegalArgumentException if cartResponse is null or contains invalid data
	 */
	OrderModel toOrderModel(CartResponse cartResponse, Long userId);
}
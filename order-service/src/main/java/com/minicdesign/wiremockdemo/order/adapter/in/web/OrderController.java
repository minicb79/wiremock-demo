package com.minicdesign.wiremockdemo.order.adapter.in.web;

import com.minicdesign.otel.GlobalExceptionHandler;
import com.minicdesign.wiremockdemo.order.application.service.ProductNotFoundException;
import com.minicdesign.wiremockdemo.order.domain.model.Order;
import com.minicdesign.wiremockdemo.order.domain.model.OrderStatus;
import com.minicdesign.wiremockdemo.order.domain.port.in.PlaceOrderUseCase;
import com.minicdesign.wiremockdemo.order.orderapi.api.OrdersApi;
import com.minicdesign.wiremockdemo.order.orderapi.model.OrderResponse;
import com.minicdesign.wiremockdemo.order.orderapi.model.PlaceOrderRequest;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController implements OrdersApi {
	private final PlaceOrderUseCase placeOrderUseCase;

	@Override
	public ResponseEntity<OrderResponse> placeOrder(PlaceOrderRequest placeOrderRequest) {
		log.info("POST /v1/orders received productId={} quantity={}", placeOrderRequest.getProductId(),
				placeOrderRequest.getQuantity());

		Order order = placeOrderUseCase.placeOrder(placeOrderRequest.getProductId(), placeOrderRequest.getQuantity());

		OrderResponse.StatusEnum statusEnum = order.status() == OrderStatus.CONFIRMED
				? OrderResponse.StatusEnum.CONFIRMED
				: OrderResponse.StatusEnum.REJECTED;

		OrderResponse response = new OrderResponse(order.orderId(), order.productId(), order.quantity(), statusEnum);

		HttpStatus httpStatus = order.status() == OrderStatus.CONFIRMED ? HttpStatus.CREATED : HttpStatus.CONFLICT;
		return ResponseEntity.status(httpStatus).body(response);
	}
}

@RestControllerAdvice
@Slf4j
class OrderExceptionHandler extends GlobalExceptionHandler {

	public OrderExceptionHandler(Tracer tracer) {
		super(tracer);
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
		log.warn("Product not found: {}", ex.getMessage());
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
				ex.getMessage() != null ? ex.getMessage() : "Product not found");
		enrichWithTraceId(problem);
		return problem;
	}
}

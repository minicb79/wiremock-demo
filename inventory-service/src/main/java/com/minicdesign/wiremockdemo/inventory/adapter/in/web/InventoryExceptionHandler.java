package com.minicdesign.wiremockdemo.inventory.adapter.in.web;

import com.minicdesign.wiremockdemo.inventory.application.service.InventoryItemNotFoundException;
import com.minicdesign.otel.GlobalExceptionHandler;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class InventoryExceptionHandler extends GlobalExceptionHandler {

	public InventoryExceptionHandler(Tracer tracer) {
		super(tracer);
	}

	@ExceptionHandler(InventoryItemNotFoundException.class)
	public ProblemDetail handleInventoryItemNotFound(InventoryItemNotFoundException ex) {
		log.warn("Inventory item not found: {}", ex.getMessage());
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		enrichWithTraceId(problem);
		return problem;
	}
}

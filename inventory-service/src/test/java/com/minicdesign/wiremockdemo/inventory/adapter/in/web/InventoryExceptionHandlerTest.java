package com.minicdesign.wiremockdemo.inventory.adapter.in.web;

import com.minicdesign.wiremockdemo.inventory.application.service.InventoryItemNotFoundException;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryExceptionHandlerTest {

	@Test
	void shouldReturn404ProblemDetailWithTraceId() {
		Tracer tracer = mock(Tracer.class);
		io.micrometer.tracing.TraceContext ctx = mock(io.micrometer.tracing.TraceContext.class);
		io.micrometer.tracing.CurrentTraceContext currentTraceContext = mock(
				io.micrometer.tracing.CurrentTraceContext.class);
		when(tracer.currentTraceContext()).thenReturn(currentTraceContext);
		when(currentTraceContext.context()).thenReturn(ctx);
		when(ctx.traceId()).thenReturn("abc123");

		InventoryExceptionHandler handler = new InventoryExceptionHandler(tracer);
		InventoryItemNotFoundException ex = new InventoryItemNotFoundException("UNKNOWN");

		ProblemDetail problem = handler.handleInventoryItemNotFound(ex);

		assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
		assertNotNull(problem.getDetail());
		assertEquals("abc123", problem.getProperties().get("traceId"));
	}

	@Test
	void shouldReturn404ProblemDetailWithoutTraceIdWhenTracerUnavailable() {
		Tracer tracer = mock(Tracer.class);
		when(tracer.currentTraceContext()).thenReturn(null);

		InventoryExceptionHandler handler = new InventoryExceptionHandler(tracer);
		InventoryItemNotFoundException ex = new InventoryItemNotFoundException("UNKNOWN");

		ProblemDetail problem = handler.handleInventoryItemNotFound(ex);

		assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
	}
}

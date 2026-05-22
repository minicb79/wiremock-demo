package com.minicdesign.wiremockdemo.order.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient

class WiremockProxyInitializerTest {
    @Test
    fun `should not register proxy mapping when feature is disabled`() {
        val restClient = mock(RestClient::class.java)
        val objectMapper = ObjectMapper()
        val initializer =
            WiremockProxyInitializer(
                wiremockRestClient = restClient,
                objectMapper = objectMapper,
                asProxyEnabled = false,
                inventoryBaseUrl = "http://localhost:8081",
            )

        initializer.run()

        verify(restClient, never()).post()
    }

    @Test
    fun `should register proxy mapping when feature is enabled`() {
        val restClient = mock(RestClient::class.java)
        val postSpec = mock(RestClient.RequestBodyUriSpec::class.java)
        val bodySpec = mock(RestClient.RequestBodySpec::class.java)
        val responseSpec = mock(RestClient.ResponseSpec::class.java)
        val objectMapper = ObjectMapper()

        @Suppress("UNCHECKED_CAST")
        val responseEntity = mock(ResponseEntity::class.java) as ResponseEntity<Void>

        `when`(restClient.post()).thenReturn(postSpec)
        `when`(postSpec.uri(any(String::class.java))).thenReturn(postSpec)
        `when`(postSpec.contentType(any())).thenReturn(postSpec)
        `when`(postSpec.body(any())).thenReturn(bodySpec)
        `when`(bodySpec.retrieve()).thenReturn(responseSpec)
        `when`(responseSpec.toBodilessEntity()).thenReturn(responseEntity)
        `when`(responseEntity.statusCode).thenReturn(HttpStatus.OK)

        val initializer =
            WiremockProxyInitializer(
                wiremockRestClient = restClient,
                objectMapper = objectMapper,
                asProxyEnabled = true,
                inventoryBaseUrl = "http://localhost:8081",
            )

        initializer.run()

        verify(restClient).post()
        verify(postSpec).uri("/__admin/mappings")

        val bodyCaptor = org.mockito.ArgumentCaptor.forClass(Any::class.java)
        verify(postSpec).body(bodyCaptor.capture())
        val bodyMap = bodyCaptor.value as Map<*, *>
        val responseMap = bodyMap["response"] as Map<*, *>
        org.junit.jupiter.api.Assertions
            .assertEquals("http://host.docker.internal:8081", responseMap["proxyBaseUrl"])

        val expectedRequestCriteria = mapOf(
            "method" to "ANY",
            "urlPattern" to "/inventory/.*"
        )
        val expectedBytes = objectMapper.writeValueAsBytes(expectedRequestCriteria)
        val expectedUuid = java.util.UUID.nameUUIDFromBytes(expectedBytes).toString()
        org.junit.jupiter.api.Assertions
            .assertEquals(expectedUuid, bodyMap["id"])
    }

    @Test
    fun `should log and not throw exception when registration fails`() {
        val restClient = mock(RestClient::class.java)
        `when`(restClient.post()).thenThrow(RuntimeException("Connection refused"))
        val objectMapper = ObjectMapper()

        val initializer =
            WiremockProxyInitializer(
                wiremockRestClient = restClient,
                objectMapper = objectMapper,
                asProxyEnabled = true,
                inventoryBaseUrl = "http://localhost:8081",
            )

        assertDoesNotThrow {
            initializer.run()
        }
    }
}

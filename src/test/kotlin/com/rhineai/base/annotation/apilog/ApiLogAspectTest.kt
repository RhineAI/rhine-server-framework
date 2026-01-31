package com.rhineai.framework.annotation.apilog

import com.rhineai.framework.exception.RestExceptionHandler
import com.rhineai.framework.test.DummyController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import(DummyController::class, RestExceptionHandler::class, ApiLogAspect::class, ApiLogProperties::class)
class ApiLogAspectTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `ok endpoint with @ApiLog returns 200 when enabled`() {
        mockMvc.perform(get("/api/ok").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("OK"))
    }
}

package com.rhineai.framework.exception

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
@Import(DummyController::class, RestExceptionHandler::class)
class RestExceptionHandlerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `business exception returns 400 with code`() {
        mockMvc.perform(get("/api/biz").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.startsWith("E")))
    }

    @Test
    fun `missing parameter returns 400`() {
        mockMvc.perform(get("/api/invalid").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("E00013001"))
    }

    @Test
    fun `generic exception returns 500`() {
        mockMvc.perform(get("/api/oops").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.code").value("E00000001"))
    }

    @Test
    fun `ok returns 200`() {
        mockMvc.perform(get("/api/ok").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("OK"))
    }
}

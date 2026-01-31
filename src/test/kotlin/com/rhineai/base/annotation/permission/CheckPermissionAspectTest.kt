package com.rhineai.framework.annotation.permission

import com.rhineai.framework.exception.RestExceptionHandler
import com.rhineai.framework.test.ProtectedController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import(ProtectedController::class, RestExceptionHandler::class, CheckPermissionAspectTest.TestBeans::class, AopAutoConfiguration::class)
class CheckPermissionAspectTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @TestConfiguration
    class TestBeans {
        @Bean
        fun checkPermissionAspect(): CheckPermissionAspect = object : CheckPermissionAspect() {
            override fun getUserHasPermission(userId: String?): List<String> {
                return when (userId) {
                    "1" -> listOf("perm:view")
                    null, "" -> emptyList()
                    else -> emptyList()
                }
            }
        }
    }

    @Test
    fun `allowed when user has permission`() {
        mockMvc.perform(get("/api/secure/ok").header("currentUserId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun `forbidden when user lacks permission`() {
        mockMvc.perform(get("/api/secure/deny").header("currentUserId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `forbidden when missing userId`() {
        mockMvc.perform(get("/api/secure/ok").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `allowed when user has one of OR permissions`() {
        // User has perm:view, requires perm:view OR admin:only -> OK
        mockMvc.perform(get("/api/secure/or").header("currentUserId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun `forbidden when user lacks all AND permissions`() {
        // User has perm:view, requires perm:view AND admin:only -> Forbidden
        mockMvc.perform(get("/api/secure/and").header("currentUserId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden)
    }
}

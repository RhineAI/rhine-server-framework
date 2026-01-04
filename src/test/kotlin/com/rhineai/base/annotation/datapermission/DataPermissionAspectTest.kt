package com.rhine.framework.annotation.datapermission

import jakarta.persistence.*
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles

/**
 * Tests for DataPermissionAspect using a Hibernate filter on the Note entity.
 */
@DataJpaTest
@ImportAutoConfiguration(AopAutoConfiguration::class)
@ActiveProfiles("test")
@Import(DataPermissionAspectTest.TestConfig::class)
class DataPermissionAspectTest {

    @Autowired
    lateinit var repository: NoteRepository

    @Autowired
    lateinit var service: NoteService

    @BeforeEach
    fun setup(@Autowired repo: NoteRepository) {
        // Setup data without aspect interference
        repo.deleteAll()
        repo.save(Note(content = "a", ownerId = "u1"))
        repo.save(Note(content = "b", ownerId = "u2"))
    }

    @Test
    fun `should filter by ownerId when header is u1`() {
        TestRequestContext.withHeader("currentUserId", "u1") {
            val notes = service.listAll()
            assertEquals(1, notes.size)
            assertEquals("u1", notes[0].ownerId)
        }
    }

    @Test
    fun `should filter by ownerId when header is u2`() {
        TestRequestContext.withHeader("currentUserId", "u2") {
            val notes = service.listAll()
            assertEquals(1, notes.size)
            assertEquals("u2", notes[0].ownerId)
        }
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun dataPermissionAspect(em: jakarta.persistence.EntityManager): DataPermissionAspect = DataPermissionAspect(em)

        @Bean
        fun noteService(repo: NoteRepository) = NoteService(repo)
    }
}

@Entity
@Table(name = "t_note")
@FilterDef(name = "data_permission_filter", parameters = [ParamDef(name = "userId", type = String::class)])
@Filter(name = "data_permission_filter", condition = "owner_id = :userId")
class Note(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val content: String,
    @Column(name = "owner_id")
    val ownerId: String
)

interface NoteRepository : JpaRepository<Note, Long>

@DataPermission
@Service
class NoteService(private val repository: NoteRepository) {
    fun listAll(): List<Note> = repository.findAll()
}

object TestRequestContext {
    fun <T> withHeader(name: String, value: String, block: () -> T): T {
        val request = org.springframework.mock.web.MockHttpServletRequest()
        request.addHeader(name, value)
        val attrs = org.springframework.web.context.request.ServletRequestAttributes(request)
        val holder = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
        try {
            org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(attrs)
            return block()
        } finally {
            org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(holder)
        }
    }
}
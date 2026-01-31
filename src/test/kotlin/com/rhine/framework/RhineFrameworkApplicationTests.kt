package com.rhineai.framework

import com.rhineai.TestApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [TestApplication::class],
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    ]
)
class RhineFrameworkApplicationTests {

    @Test
    fun contextLoads() {
    }

}

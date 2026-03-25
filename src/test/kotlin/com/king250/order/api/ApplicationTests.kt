package com.king250.order.api

import com.king250.order.api.integration.logto.CreateUserRequest
import com.king250.order.api.integration.logto.CustomData
import com.king250.order.api.integration.logto.LogtoService
import com.king250.order.jooq.tables.references.USER
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@SpringBootTest
@ActiveProfiles("local")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ApplicationTests(
    private val dsl: DSLContext,
    private val logto: LogtoService
) {
    @Test
    fun contextLoads() {
        val users = dsl.selectFrom(USER)
            .where(USER.REFERENCE_ID.isNull)
            .fetch()
        users.forEach { user ->
            val result = runBlocking {
                logto.addUser(CreateUserRequest(
                    primaryEmail = "${user.qq!!}@qq.com",
                    name = user.name!!,
                    avatar = "https://q1.qlogo.cn/g?b=qq&nk=${user.qq!!}&s=0",
                    customData = CustomData(
                        qq = user.qq!!,
                    )
                ))
            }
            user.referenceId = result.id
            user.store()
        }
    }

}

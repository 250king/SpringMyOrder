package com.king250.order.api.integration.auth

import com.king250.order.api.integration.logto.LogtoService
import com.king250.order.jooq.tables.references.*
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component("auth")
class AuthService(
    private val dsl: DSLContext,
    private val logto: LogtoService
) {
    fun getUid() : Long {
        val auth = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
        val referenceId = auth.token.subject
        val id = dsl.select(USER.ID)
            .from(USER)
            .where(USER.REFERENCE_ID.eq(referenceId))
            .fetchOne(USER.ID)
        if (id != null) {
            return id
        }
        return runCatching {
            runBlocking {
                logto.getUserInfo(referenceId)
            }
        }.mapCatching { userInfo ->
            dsl.insertInto(USER)
                .set(USER.REFERENCE_ID, referenceId)
                .set(USER.NAME, userInfo.name)
                .set(USER.EMAIL, userInfo.primaryEmail)
                .set(USER.QQ, userInfo.customData.qq)
                .onConflict(USER.REFERENCE_ID).doUpdate()
                .set(USER.NAME, userInfo.name)
                .returning(USER.ID)
                .fetchOne()?.id
                ?: throw IllegalStateException("Database insert failed")
        }.getOrElse {
            throw AuthorizationDeniedException("No user found")
        }
    }

    fun isAdmin(): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth !is JwtAuthenticationToken) {
            return false
        }
        val groups = auth.token.getClaimAsString("scope")?.split(" ") ?: emptyList()
        return groups.any { it == "admin:all" }
    }

    fun isMember(groupId: Long): Boolean {
        if (isAdmin()) {
            return true
        }
        return dsl.select(GROUP_USER.USER_ID)
            .from(GROUP_USER)
            .where(GROUP_USER.GROUP_ID.eq(groupId))
            .and(GROUP_USER.USER_ID.eq(getUid()))
            .fetchOne(GROUP_USER.USER_ID) != null
    }

    fun isSelfAddress(addressId: Long): Boolean {
        if (isAdmin()) {
            return true
        }
        return dsl.select(ADDRESS.ID)
            .from(ADDRESS)
            .where(ADDRESS.ID.eq(addressId))
            .and(ADDRESS.USER_ID.eq(getUid()))
            .fetchOne(ADDRESS.ID) != null
    }

    fun isSelfDelivery(deliveryId: Long): Boolean {
        if (isAdmin()) {
            return true
        }
        return dsl.select(DELIVERY.ID)
            .from(DELIVERY)
            .where(DELIVERY.ID.eq(deliveryId))
            .and(DELIVERY.USER_ID.eq(getUid()))
            .fetchOne(DELIVERY.ID) != null
    }

    fun isSelfPayment(paymentId: Long): Boolean {
        if (isAdmin()) {
            return true
        }
        return dsl.select(PAYMENT.USER_ID)
            .where(PAYMENT.ID.eq(paymentId))
            .and(PAYMENT.USER_ID.eq(getUid()))
            .fetchOne() != null
    }
}
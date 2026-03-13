package com.king250.order.api.integration.auth

import com.king250.order.jooq.enums.GroupRole
import com.king250.order.jooq.tables.references.GROUP_USER
import com.king250.order.jooq.tables.references.USER
import org.jooq.DSLContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component("auth")
class AuthService(
    private val dsl: DSLContext
) {
    private val roleLevel = mapOf(
        GroupRole.OWNER to 30,
        GroupRole.ADMIN to 20,
        GroupRole.MEMBER to 10
    )

    private fun hasAtLeastRole(groupId: Long, minRequiredRole: GroupRole): Boolean {
        if (isSuperAdmin()) {
            return true
        }
        val auth = SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken ?: return false
        val userRole = dsl.select(GROUP_USER.ROLE)
            .from(GROUP_USER)
            .join(USER).on(GROUP_USER.USER_ID.eq(USER.ID))
            .where(USER.QQ.eq(auth.token.getClaimAsString("qq")))
            .and(GROUP_USER.GROUP_ID.eq(groupId))
            .fetchOne(GROUP_USER.ROLE) ?: return false
        val actualPower = roleLevel[userRole] ?: 0
        val requiredPower = roleLevel[minRequiredRole] ?: 999
        return actualPower >= requiredPower
    }

    fun isOwner(groupId: Long) = hasAtLeastRole(groupId, GroupRole.OWNER)

    fun isAdmin(groupId: Long) = hasAtLeastRole(groupId, GroupRole.ADMIN)

    fun isMember(groupId: Long) = hasAtLeastRole(groupId, GroupRole.MEMBER)

    fun isSuperAdmin(): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth !is JwtAuthenticationToken) {
            return false
        }
        return auth.token.subject == "6"
    }

    fun isSelf(uid: Long): Boolean {
        return isSuperAdmin() || getUid() == uid
    }

    fun getUid() : Long {
        val auth = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
        return dsl.select(USER.ID)
            .from(USER)
            .where(USER.QQ.eq(auth.token.getClaimAsString("qq")))
            .fetchSingle(USER.ID)!!
    }
}
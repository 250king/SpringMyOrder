package com.king250.order.api.integration.auth

import com.king250.order.jooq.enums.GroupRole
import com.king250.order.jooq.enums.PaymentType
import com.king250.order.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
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

    fun isSuperAdmin(): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth !is JwtAuthenticationToken) {
            return false
        }
        return auth.token.subject == "6"
    }

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

    fun isSelfAddress(addressId: Long): Boolean {
        if (isSuperAdmin()) {
            return true
        }
        return dsl.select(ADDRESS.ID)
            .from(ADDRESS)
            .where(ADDRESS.ID.eq(addressId))
            .and(ADDRESS.USER_ID.eq(getUid()))
            .fetchOne(ADDRESS.ID) != null
    }

    fun isSelfDelivery(deliveryId: Long): Boolean {
        if (isSuperAdmin()) {
            return true
        }
        return dsl.select(DELIVERY.ID)
            .from(DELIVERY)
            .where(DELIVERY.ID.eq(deliveryId))
            .and(DSL.or(
                DELIVERY.USER_ID.eq(getUid()),
                DSL.exists(
                    dsl.selectOne()
                        .from(DELIVERY_LIST)
                        .join(ITEM).on(ITEM.ID.eq(DELIVERY_LIST.LIST_ID))
                        .join(GROUP).on(GROUP.ID.eq(ITEM.GROUP_ID))
                        .where(DELIVERY_LIST.DELIVERY_ID.eq(DELIVERY.ID))
                        .and(GROUP.OWNER_ID.eq(getUid()))
                )
            ))
            .fetchOne(DELIVERY.ID) != null
    }

    fun isSelfPayment(paymentId: Long): Boolean {
        if (isSuperAdmin()) {
            return true
        }
        val payment = dsl.selectFrom(PAYMENT)
            .where(PAYMENT.ID.eq(paymentId))
            .fetchSingle()
        return when(payment.type!!) {
            PaymentType.DELIVERY -> {
                dsl.fetchExists(
                    dsl.selectOne()
                        .from(DELIVERY)
                        .join(DELIVERY_LIST).on(DELIVERY_LIST.DELIVERY_ID.eq(DELIVERY.ID))
                        .join(LIST).on(LIST.ID.eq(DELIVERY_LIST.LIST_ID))
                        .join(ITEM).on(ITEM.ID.eq(LIST.ITEM_ID))
                        .join(GROUP).on(GROUP.ID.eq(ITEM.GROUP_ID))
                        .where(DELIVERY.ID.eq(payment.referenceId!!))
                        .and(
                            DSL.or(
                                GROUP.OWNER_ID.eq(getUid()),
                                DELIVERY.USER_ID.eq(getUid()),
                            )
                        )
                )
            }
            PaymentType.LIST -> {
                false
            }
            PaymentType.SHIPPING -> {
                false
            }
            PaymentType.TAX -> {
                false
            }
        }
    }

    fun isAdminDeliveries(deliveries: List<Long>): Boolean {
        if (isSuperAdmin()) {
            return true
        }
        return dsl.select(DELIVERY.ID)
            .from(DELIVERY)
            .join(DELIVERY_LIST).on(DELIVERY_LIST.DELIVERY_ID.eq(DELIVERY.ID))
            .join(ITEM).on(ITEM.ID.eq(DELIVERY_LIST.LIST_ID))
            .join(GROUP).on(GROUP.ID.eq(ITEM.GROUP_ID))
            .where(DELIVERY.ID.`in`(deliveries))
            .and(GROUP.OWNER_ID.eq(getUid()))
            .fetch().size == deliveries.toSet().size
    }

    fun isAdminList(lists: List<Long>): Boolean {
        if (isSuperAdmin()) {
            return true
        }
        return dsl.select(LIST.ID)
            .from(LIST)
            .join(ITEM).on(ITEM.ID.eq(LIST.ITEM_ID))
            .join(GROUP).on(GROUP.ID.eq(ITEM.GROUP_ID))
            .where(LIST.ID.`in`(lists))
            .and(GROUP.OWNER_ID.eq(getUid()))
            .fetch().size == lists.toSet().size
    }

    fun isAdminMember(uid: Long?): Boolean {
        if (isSuperAdmin()) {
            return true
        }
        if (uid == null) {
            return false
        }
        if (uid == getUid()) {
            return true
        }
        return dsl.select(GROUP_USER.USER_ID)
            .from(GROUP_USER)
            .join(GROUP).on(GROUP_USER.GROUP_ID.eq(GROUP.ID))
            .where(GROUP_USER.USER_ID.eq(uid))
            .and(GROUP.OWNER_ID.eq(getUid()))
            .fetchOne() != null
    }
}
package com.king250.order.api.integration.napcat

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import org.springframework.stereotype.Service

@Service
class NapcatService(
    ktor: HttpClient,
    private val properties: NapcatProperties
) {
    private val client = ktor.config {
        install(DefaultRequest) {
            url(properties.url)
            header("Authorization", "Bearer ${properties.token}")
        }
    }

    suspend fun getUserInfo(userId: String): NapcatResponse<UserResponse> {
        return client.get("get_stranger_info") {
            parameter("user_id", userId)
        }.body()
    }

    suspend fun getGroupInfo(groupId: String): NapcatResponse<GroupResponse> {
        return client.get("get_group_detail_info") {
            parameter("group_id", groupId)
        }.body()
    }
}
package com.king250.order.api.integration.aliyun

import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.model.ListObjectsV2Request
import com.aliyun.oss.model.ListObjectsV2Result
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service

@Service
class AliyunService(
    private val properties: AliyunProperties
) {
    private val client = OSSClientBuilder().build(
        properties.endpoint,
        properties.key,
        properties.secret
    )

    fun findAll(path: String): ListObjectsV2Result {
        val request = ListObjectsV2Request(properties.bucket).apply {
            prefix = path
        }
        return client.listObjectsV2(request)
    }

    @PreDestroy
    fun destroy() {
        client.shutdown()
    }
}

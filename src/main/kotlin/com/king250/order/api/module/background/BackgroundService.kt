package com.king250.order.api.module.background

import com.king250.order.api.integration.aliyun.AliyunService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Service
@OptIn(ExperimentalAtomicApi::class)
class BackgroundService(
    private val oss: AliyunService,
    private val properties: BackgroundProperties
) {
    private val pool = AtomicReference(Pool())

    @Scheduled(fixedDelay = 60 * 1000)
    fun refresh() {
        val mobile = oss.findAll("${properties.path}/mobile/").objectSummaries.map { it.key }.filter { it != "${properties.path}/mobile/" }
        val desktop = oss.findAll("${properties.path}/desktop/").objectSummaries.map { it.key }.filter { it != "${properties.path}/desktop/" }
        pool.store(Pool(mobile, desktop))
    }

    fun toUri(url: String): URI {
        return UriComponentsBuilder.fromUriString(url).build().toUri()
    }

    fun getUrl(isMobile: Boolean): URI {
        val list = if (isMobile) pool.load().mobile else pool.load().desktop
        val result = list.randomOrNull() ?: return toUri(properties.default)
        return toUri("${properties.url}$result")
    }
}

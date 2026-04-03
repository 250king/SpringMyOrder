package com.king250.order.api.module.background

import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
class BackgroundController(
    private val service: BackgroundService
) {
    private val cache = CacheControl.maxAge(600, TimeUnit.SECONDS).cachePublic()

    @GetMapping("/_/background/mobile")
    fun getMobilePhoto(): ResponseEntity<Void> {
        val uri = service.getUrl(true)
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(uri)
            .cacheControl(cache)
            .build()
    }

    @GetMapping("/_/background/desktop")
    fun getDesktopPhoto(): ResponseEntity<Void> {
        val uri = service.getUrl(false)
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(uri)
            .cacheControl(cache)
            .build()
    }
}

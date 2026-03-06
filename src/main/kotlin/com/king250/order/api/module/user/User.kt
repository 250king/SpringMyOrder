package com.king250.order.api.module.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Table(name = "\"user\"")
@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLDelete(sql = "UPDATE \"user\" SET name = '已注销_' || left(md5(name), 8), email = NULL, qq = MD5(qq), deleted_at = NOW() WHERE id = ?")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    var name: String,

    var qq: String,

    var email: String? = null,

    var creditScore: Int = 100,

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var deletedAt: LocalDateTime? = null
)

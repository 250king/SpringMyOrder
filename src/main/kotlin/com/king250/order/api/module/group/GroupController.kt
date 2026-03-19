package com.king250.order.api.module.group

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
class GroupController(
    private val service: GroupService,
    private val mapper: GroupMapper,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/groups")
    fun findAll(@Valid @ParameterObject request: QueryGroupRequest): ItemResponse<GroupResponse> {
        val groups = service.findAll(request)
        return groups.toItem(mapper::toResponse)
    }

    @PostMapping("/groups")
    @PreAuthorize("@auth.isSuperAdmin()")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateGroupRequest): GroupResponse {
        val group = mapper.toEntity(request)
        return mapper.toResponse(service.save(group, request.users))
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("@auth.isMember(#groupId)")
    fun findById(@PathVariable groupId: Long): GroupResponse {
        val group = service.findById(groupId)
        return mapper.toResponse(group)
    }

    @PatchMapping("/groups/{groupId}")
    @PreAuthorize("@auth.isOwner(#groupId)")
    fun update(@PathVariable groupId: Long, @Valid @RequestBody request: UpdateGroupRequest): GroupResponse {
        val group = service.findById(groupId)
        mapper.updateEntity(request, group)
        return mapper.toResponse(service.save(group))
    }

    @PostMapping("/groups/{groupId}/transfer")
    @PreAuthorize("@auth.isOwner(#groupId)")
    fun transferOwnership(
        @PathVariable groupId: Long,
        @Valid @RequestBody request: TransferOwnershipRequest
    ): GroupResponse {
        return mapper.toResponse(service.transferOwnership(groupId, request.ownerId))
    }

    @GetMapping("/groups/{groupId}/users")
    @PreAuthorize("@auth.isMember(#groupId)")
    fun getMembers(
        @PathVariable groupId: Long,
        @Valid @ParameterObject request: QueryMemberRequest
    ): ItemResponse<MemberResponse> {
        val users = service.getMembers(request, groupId)
        return users.toItem(mapper::toMemberResponse)
    }

    @PostMapping("/groups/{groupId}/users")
    @PreAuthorize("@auth.isAdmin(#groupId)")
    fun addMembers(@PathVariable groupId: Long, @Valid @RequestBody request: AddMemberRequest): ObjectNode {
        val result = service.addMembers(groupId, request.users.toSet())
        return objectMapper.createObjectNode().apply {
            put("total", result)
        }
    }

    @PostMapping("/groups/{groupId}/users/{userId}")
    @PreAuthorize("@auth.isOwner(#groupId)")
    fun changeRole(
        @PathVariable groupId: Long,
        @PathVariable userId: Long,
        @Valid @RequestBody request: UpdateMemberRequest
    ): MemberResponse {
        val member = service.changeRole(groupId, userId, request.role)
        return mapper.toMemberResponse(member)
    }
}

package com.king250.order.api.module.group

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import org.springframework.http.RequestEntity.put
import org.springframework.web.bind.annotation.*

@RestController
class GroupController(
    private val service: GroupService,
    private val mapper: GroupMapper,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/groups")
    fun getGroups(@Valid request: QueryGroupRequest): ItemResponse<GroupResponse> {
        val groups = service.findAll(request)
        return groups.toItem(mapper::toResponse)
    }

    @PostMapping("/groups")
    fun createGroup(@Valid @RequestBody request: CreateGroupRequest): GroupResponse {
        val group = mapper.toEntity(request)
        return mapper.toResponse(service.save(group, request.users))
    }

    @GetMapping("/groups/{groupId}")
    fun getGroupById(@PathVariable groupId: Long): GroupResponse {
        val group = service.findById(groupId)
        return mapper.toResponse(group)
    }

    @PatchMapping("/groups/{groupId}")
    fun updateGroupById(@PathVariable groupId: Long, @Valid @RequestBody request: UpdateGroupRequest): GroupResponse {
        val group = service.findById(groupId)
        mapper.updateEntity(request, group)
        return mapper.toResponse(service.save(group))
    }

    @GetMapping("/groups/{groupId}/users")
    fun getMembers(@PathVariable groupId: Long, @Valid request: QueryMemberRequest): ItemResponse<MemberResponse> {
        val users = service.getMembers(request, groupId)
        return users.toItem()
    }

    @PostMapping("/groups/{groupId}/users")
    fun addMember(@PathVariable groupId: Long, @Valid @RequestBody request: AddMemberRequest): ObjectNode {
        val result = service.addMembersToGroup(groupId, request.users.toSet())
        return objectMapper.createObjectNode().apply {
            put("total", result)
        }
    }
}

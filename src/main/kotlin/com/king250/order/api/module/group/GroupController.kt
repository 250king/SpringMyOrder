package com.king250.order.api.module.group

import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
class GroupController(
    private val service: GroupService,
    private val mapper: GroupMapper
) {
    @GetMapping("/groups")
    fun getGroups(@Valid request: GroupQueryRequest): ItemResponse<GroupResponse> {
        val groups = service.findAll(request)
        return groups.toItem(mapper::toResponse)
    }

    @PostMapping("/groups")
    fun createGroup(@Valid @RequestBody request: GroupCreateRequest): GroupResponse {
        val group = mapper.toEntity(request)
        return mapper.toResponse(service.save(group, request.users))
    }

    @GetMapping("/groups/{id}")
    fun getGroupById(@PathVariable id: Long): GroupResponse {
        val group = service.findById(id)
        return mapper.toResponse(group)
    }

    @PatchMapping("/groups/{id}")
    fun updateGroupById(@PathVariable id: Long, @Valid @RequestBody request: GroupUpdateRequest): GroupResponse {
        val group = service.findById(id)
        mapper.updateEntity(request, group)
        return mapper.toResponse(service.save(group))
    }

    @GetMapping("/groups/{id}/users")
    fun getGroupUsers(@PathVariable id: Long, @Valid request: MemberQueryRequest): ItemResponse<MemberResponse> {
        val users = service.getMembers(request, id)
        return users.toItem()
    }
}

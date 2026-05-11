package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.Group;
import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.dto.GroupRequest;
import com.chiclete.reminder.dto.GroupResponse;
import com.chiclete.reminder.infra.GroupRepository;
import com.chiclete.reminder.infra.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GroupResponse create(GroupRequest request, User creator) {
        Group group = new Group();
        group.setName(request.name());
        group.getMembers().add(creator);
        groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listFor(User user) {
        return groupRepository.findAllForMember(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GroupResponse addMember(Long groupId, String memberEmail, User actor) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (group.getMembers().stream().noneMatch(m -> m.getId().equals(actor.getId()))) {
            throw new ForbiddenException("Apenas membros do grupo podem convidar outros usuários");
        }
        User invited = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new DomainRuleException("Usuário convidado não encontrado"));
        if (group.getMembers().stream().anyMatch(m -> m.getId().equals(invited.getId()))) {
            return toResponse(group);
        }
        group.getMembers().add(invited);
        groupRepository.save(group);
        return toResponse(group);
    }

    private GroupResponse toResponse(Group g) {
        List<String> emails = g.getMembers().stream()
                .map(User::getEmail)
                .sorted()
                .toList();
        return new GroupResponse(g.getId(), g.getName(), emails);
    }
}

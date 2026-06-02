package com.veltro.user;

import com.veltro.user.dto.CreateMemberRequest;
import com.veltro.user.dto.MemberResponse;
import com.veltro.user.dto.UpdateMemberRequest;
import com.veltro.user.entity.AccountStatus;
import com.veltro.user.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberLifecycleTest extends BaseIntegrationTest {

    @Autowired
    private MemberService memberService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private CreateMemberRequest buildRequest(long userId) {
        CreateMemberRequest req = new CreateMemberRequest();
        req.setUserId(userId);
        req.setIdentifier("member" + userId + "@test.com");
        req.setFirstname("Alice");
        req.setLastname("Smith");
        req.setPhone("+1-555-" + userId);
        req.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return req;
    }

    @Test
    void createAndFindMember() {
        MemberResponse created = memberService.create(buildRequest(1001L));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getFirstname()).isEqualTo("Alice");
        assertThat(created.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        MemberResponse found = memberService.findById(created.getId());
        assertThat(found.getLastname()).isEqualTo("Smith");
    }

    @Test
    void updateMember() {
        MemberResponse created = memberService.create(buildRequest(1002L));

        UpdateMemberRequest update = new UpdateMemberRequest();
        update.setFirstname("Alicia");
        update.setPhone("+1-555-9999");

        MemberResponse updated = memberService.update(created.getId(), update);
        assertThat(updated.getFirstname()).isEqualTo("Alicia");
        assertThat(updated.getPhone()).isEqualTo("+1-555-9999");
        assertThat(updated.getLastname()).isEqualTo("Smith");
    }

    @Test
    void suspendAndActivateMember() {
        MemberResponse created = memberService.create(buildRequest(1003L));

        MemberResponse suspended = memberService.suspend(created.getId());
        assertThat(suspended.getStatus()).isEqualTo(AccountStatus.SUSPENDED);

        MemberResponse reactivated = memberService.activate(created.getId());
        assertThat(reactivated.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void duplicateUserIdThrowsConflict() {
        memberService.create(buildRequest(1004L));
        assertThatThrownBy(() -> memberService.create(buildRequest(1004L)))
                .hasMessageContaining("already exists");
    }

    @Test
    void findByUserIdReturnsCorrectMember() {
        MemberResponse created = memberService.create(buildRequest(1005L));
        MemberResponse found = memberService.findByUserId(1005L);
        assertThat(found.getId()).isEqualTo(created.getId());
    }
}

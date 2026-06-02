package com.veltro.user;

import com.veltro.user.dto.CreateMemberRequest;
import com.veltro.user.dto.MemberResponse;
import com.veltro.user.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarUploadTest extends BaseIntegrationTest {

    @Autowired private MemberService memberService;
    @MockitoBean private RabbitTemplate rabbitTemplate;

    private MemberResponse createMember(long userId) {
        CreateMemberRequest req = new CreateMemberRequest();
        req.setUserId(userId);
        req.setIdentifier("avatar" + userId + "@test.com");
        req.setFirstname("Avatar");
        req.setLastname("Test");
        return memberService.create(req);
    }

    @Test
    void uploadValidJpegSetsAvatarUrl() {
        MemberResponse member = createMember(5001L);
        byte[] fakeJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", fakeJpeg
        );

        MemberResponse updated = memberService.uploadAvatar(member.getId(), file);
        assertThat(updated.getAvatarUrl()).isNotNull().contains("/avatars/");
    }

    @Test
    void uploadEmptyFileThrowsException() {
        MemberResponse member = createMember(5002L);
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        assertThatThrownBy(() -> memberService.uploadAvatar(member.getId(), emptyFile))
                .isInstanceOf(Exception.class);
    }

    @Test
    void uploadInvalidContentTypeThrowsException() {
        MemberResponse member = createMember(5003L);
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}
        );

        assertThatThrownBy(() -> memberService.uploadAvatar(member.getId(), pdfFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JPEG, PNG");
    }
}

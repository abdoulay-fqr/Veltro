package com.veltro.booking.client;

import com.veltro.booking.exception.ServiceUnavailableException;
import com.veltro.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
        name = "user-client",
        url = "${clients.user.url}",
        fallbackFactory = UserClient.UserClientFallback.class
)
public interface UserClient {

    @PutMapping("/api/v1/users/members/{id}/suspend")
    ApiResponse<Map<String, Object>> suspendMember(
            @PathVariable("id") Long memberId,
            @RequestHeader("X-User-Role") String role);

    @Component
    @Slf4j
    class UserClientFallback implements FallbackFactory<UserClient> {
        @Override
        public UserClient create(Throwable cause) {
            return (memberId, role) -> {
                log.error("User service unavailable for suspending memberId={}: {}", memberId, cause.getMessage());
                throw new ServiceUnavailableException(
                        "User service is temporarily unavailable. Suspension will be retried.");
            };
        }
    }
}

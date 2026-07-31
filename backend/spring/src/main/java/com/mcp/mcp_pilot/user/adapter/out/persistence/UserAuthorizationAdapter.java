package com.mcp.mcp_pilot.user.adapter.out.persistence;

import com.mcp.mcp_pilot.ops.port.out.UserAuthorizationPort;
import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAuthorizationAdapter implements UserAuthorizationPort {

    private final UserPersistencePort userPersistencePort;

    @Override
    public boolean canDeploy(Long userId) {
        return userPersistencePort.findById(userId)
                .map(User::isAdmin)
                .orElse(false);
    }
}

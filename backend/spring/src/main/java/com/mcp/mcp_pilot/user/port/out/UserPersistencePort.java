package com.mcp.mcp_pilot.user.port.out;

import com.mcp.mcp_pilot.user.domain.entitiy.User;

import java.util.Optional;


public interface UserPersistencePort {
    Optional<User> findByUsername(String username);
}

package com.mcp.mcp_pilot.user.domain.entitiy;

import com.mcp.mcp_pilot.user.domain.vo.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class User {
    private final Long id;
    private final String username;
    private final String password;
    private final Role role;

    @Builder
    public User(Long id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
}

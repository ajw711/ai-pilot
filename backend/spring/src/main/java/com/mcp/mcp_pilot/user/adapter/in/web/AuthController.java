package com.mcp.mcp_pilot.user.adapter.in.web;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.SaveKnowledgeResponse;
import com.mcp.mcp_pilot.user.adapter.in.web.dto.LoginRequest;
import com.mcp.mcp_pilot.user.adapter.in.web.dto.LoginResponse;
import com.mcp.mcp_pilot.user.adapter.in.web.mapper.AuthWebMapper;
import com.mcp.mcp_pilot.user.port.in.LoginUseCase;
import com.mcp.mcp_pilot.user.port.in.dto.LoginCommand;
import com.mcp.mcp_pilot.user.port.in.dto.LoginResult;
import com.mcp.mcp_pilot.user.port.in.LogoutUseCase;
import com.mcp.mcp_pilot.user.port.in.dto.LogoutCommand;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/{version}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping(path = "/login", version = "v1")
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        log.info("[AuthController] 로그인 요청 접수.");

        // 로그인 실패 시 내부에서 UserException이 터져 전역 핸들러에서 fail(ErrorCode)을 반환함
        LoginCommand command = AuthWebMapper.toCommand(request);
        TokenResult result = loginUseCase.login(command);

        // SSE 인증 및 브라우저 세션 유지를 위한 refresh_token 쿠키 주입
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", result.refreshToken())
                .path("/")
                .maxAge(3600)
                .secure(false) // 로컬 HTTP 환경
                .httpOnly(false)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ApiResponse.success(LoginResponse.from(result.accessToken()));
    }

    @PostMapping(path = "/logout", version = "v1")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("[AuthController] 로그아웃 요청 접수. access_token 쿠키를 파괴하고 DB 세션을 무효화합니다.");

        // 브라우저 쿠키에서 JWT 토큰을 추출하여 DB 무효화(Revoke)
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken != null) {
            logoutUseCase.logout(new LogoutCommand(refreshToken));
        }

        // 브라우저 쿠키 즉시 소멸 처리 (maxAge 0)
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .path("/")
                .maxAge(0)
                .secure(false)
                .httpOnly(false)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.success(null);
    }
}
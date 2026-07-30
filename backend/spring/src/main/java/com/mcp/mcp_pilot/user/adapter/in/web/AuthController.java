package com.mcp.mcp_pilot.user.adapter.in.web;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.user.port.in.LoginUseCase;
import com.mcp.mcp_pilot.user.port.in.dto.LoginCommand;
import com.mcp.mcp_pilot.user.port.in.dto.LoginResult;
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

    @PostMapping(path = "/login", version = "v1")
    public ApiResponse<LoginResult> login(
            @RequestBody LoginCommand command,
            HttpServletResponse response
    ) {
        log.info("[AuthController] 로그인 요청 접수. Username: {}", command.username());

        // 로그인 실패 시 내부에서 UserException이 터져 전역 핸들러에서 fail(ErrorCode)을 반환함
        LoginResult result = loginUseCase.login(command);

        // SSE 인증 및 브라우저 세션 유지를 위한 access_token 쿠키 주입
        ResponseCookie cookie = ResponseCookie.from("access_token", result.accessToken())
                .path("/")
                .maxAge(3600)
                .secure(false) // 로컬 HTTP 환경
                .httpOnly(false)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.success(result);
    }

    @PostMapping(path = "/logout", version = "v1")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        log.info("[AuthController] 로그아웃 요청 접수. access_token 쿠키를 파괴합니다.");

        // 브라우저 쿠키 즉시 소멸 처리 (maxAge 0)
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .path("/")
                .maxAge(0)
                .secure(false)
                .httpOnly(false)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.success(null);
    }
}
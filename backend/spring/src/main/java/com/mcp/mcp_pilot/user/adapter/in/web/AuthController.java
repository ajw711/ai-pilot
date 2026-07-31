package com.mcp.mcp_pilot.user.adapter.in.web;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.user.adapter.in.web.dto.LoginRequest;
import com.mcp.mcp_pilot.user.adapter.in.web.dto.LoginResponse;
import com.mcp.mcp_pilot.user.adapter.in.web.mapper.AuthWebMapper;
import com.mcp.mcp_pilot.user.exception.UserException;
import com.mcp.mcp_pilot.user.port.in.LoginUseCase;
import com.mcp.mcp_pilot.user.port.in.LogoutUseCase;
import com.mcp.mcp_pilot.user.port.in.TokenRefreshUseCase;
import com.mcp.mcp_pilot.user.port.in.dto.LoginCommand;
import com.mcp.mcp_pilot.user.port.in.dto.LogoutCommand;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;
import com.mcp.mcp_pilot.user.port.out.dto.TokenRotationResult;
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
    private final TokenRefreshUseCase tokenRefreshUseCase;

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
        log.info("[AuthController] 로그아웃 요청 접수.");
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            logoutUseCase.logout(new LogoutCommand(refreshToken));
        }

        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .path("/").maxAge(0).secure(false).httpOnly(true).sameSite("Strict").build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.success(null);
    }

    @PostMapping(path = "/refresh", version = "v1")
    public ApiResponse<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("[AuthController] 토큰 재발행 요청 접수.");
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }

        TokenRotationResult result = tokenRefreshUseCase.rotate(refreshToken);

        long remainingSeconds = java.time.Duration.between(
                java.time.LocalDateTime.now(), 
                result.getExpiredAt()
        ).toSeconds();

        if (remainingSeconds <= 0) {
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", result.getRefreshToken())
                .path("/").maxAge(remainingSeconds).secure(false).httpOnly(true).sameSite("Strict").build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ApiResponse.success(LoginResponse.from(result.getAccessToken()));
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
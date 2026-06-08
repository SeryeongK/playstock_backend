package com.playstock.user.oauth;

import com.playstock.common.ApiResponse;
import com.playstock.infra.google.GoogleOAuthProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/oauth/youtube")
@RequiredArgsConstructor
@Tag(name = "YouTube OAuth", description = "크리에이터 YouTube 채널 연동 API")
public class OAuthController {

    private final YouTubeOAuthService oAuthService;
    private final GoogleOAuthProperties properties;

    @GetMapping("/connect")
    @Operation(summary = "YouTube 연동 URL 생성", description = "Google OAuth 동의 화면 URL을 반환합니다.")
    public ResponseEntity<ApiResponse<OAuthConnectUrlResponse>> connect(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(oAuthService.getConnectUrl(userId)));
    }

    @GetMapping("/callback")
    @Operation(summary = "OAuth 콜백", description = "Google에서 리다이렉트하는 콜백 엔드포인트입니다.")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response
    ) throws IOException {
        if (error != null) {
            log.warn("Google OAuth 거부: error={}", error);
            response.sendRedirect(properties.getFrontendRedirectUrl() + "/oauth/error?reason=" +
                    URLEncoder.encode("연동이 취소되었습니다", StandardCharsets.UTF_8));
            return;
        }

        try {
            oAuthService.processCallback(code, state);
            response.sendRedirect(properties.getFrontendRedirectUrl() + "/oauth/success");
        } catch (Exception e) {
            log.error("OAuth 콜백 처리 실패", e);
            response.sendRedirect(properties.getFrontendRedirectUrl() + "/oauth/error?reason=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "YouTube 연동 상태 확인")
    public ResponseEntity<ApiResponse<OAuthStatusResponse>> status(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(oAuthService.getStatus(userId)));
    }

    @DeleteMapping("/disconnect")
    @Operation(summary = "YouTube 연동 해제")
    public ResponseEntity<ApiResponse<Void>> disconnect(
            @AuthenticationPrincipal Long userId
    ) {
        oAuthService.disconnect(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

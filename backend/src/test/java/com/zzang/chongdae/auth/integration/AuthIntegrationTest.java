package com.zzang.chongdae.auth.integration;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

import com.epages.restdocs.apispec.HeaderDescriptorWithType;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.zzang.chongdae.auth.service.dto.LoginRequest;
import com.zzang.chongdae.auth.service.dto.SignupRequest;
import com.zzang.chongdae.global.integration.IntegrationTest;
import com.zzang.chongdae.member.repository.entity.MemberEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.restdocs.payload.FieldDescriptor;

class AuthIntegrationTest extends IntegrationTest {

    @DisplayName("로그인")
    @Nested
    class Login {

        List<FieldDescriptor> requestDescriptors = List.of(
                fieldWithPath("ci").description("회원 식별자 인증 정보")
        );
        List<HeaderDescriptorWithType> responseHeaderDescriptors = List.of(
                headerWithName("Set-Cookie").description("""
                        access_token=a.b.c; Path=/; HttpOnly \n
                        refresh_token=a.b.c; Path=/; HttpOnly
                        """)
        );
        ResourceSnippetParameters successSnippets = builder()
                .summary("회원 로그인")
                .description("회원 식별자 인증 정보로 로그인 합니다.")
                .requestFields(requestDescriptors)
                .responseHeaders(responseHeaderDescriptors)
                .requestSchema(schema("LonginRequest"))
                .build();

        MemberEntity member;

        @BeforeEach
        void setUp() {
            member = memberFixture.createMember();
        }

        @DisplayName("회원 식별자 인증 정보로 로그인한다.")
        @Test
        void should_loginSuccess_when_givenMemberCI() {
            LoginRequest request = new LoginRequest(
                    "dora1234"
            );

            given(spec).log().all()
                    .filter(document("login-success", resource(successSnippets)))
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/auth/login")
                    .then().log().all()
                    .statusCode(200);
        }
    }

    @DisplayName("회원가입")
    @Nested
    class Signup {
        List<FieldDescriptor> requestDescriptors = List.of(
                fieldWithPath("ci").description("회원 식별자 인증 정보")
        );
        List<FieldDescriptor> responseDescriptors = List.of(
                fieldWithPath("memberId").description("회원 id"),
                fieldWithPath("nickname").description("닉네임")
        );
        List<HeaderDescriptorWithType> responseHeaderDescriptors = List.of(
                headerWithName("Set-Cookie").description("""
                        access_token=a.b.c; Path=/; HttpOnly \n
                        refresh_token=a.b.c; Path=/; HttpOnly
                        """)
        );
        ResourceSnippetParameters successSnippets = builder()
                .summary("회원 가입")
                .description("회원 식별자 인증 정보로 가입합니다.")
                .requestFields(requestDescriptors)
                .responseFields(responseDescriptors)
                .responseHeaders(responseHeaderDescriptors)
                .requestSchema(schema("SignupRequest"))
                .responseSchema(schema("SignupResponse"))
                .build();
        ResourceSnippetParameters failSnippets = builder()
                .responseFields(failResponseDescriptors)
                .requestSchema(schema("SignupFailRequest"))
                .responseSchema(schema("SignupFailResponse"))
                .build();

        MemberEntity member;

        @BeforeEach
        void setUp() {
            member = memberFixture.createMember();
        }

        @DisplayName("회원 식별자 인증 정보로 회원가입 한다.")
        @Test
        void should_signupSuccess_when_givenMemberCI() {
            SignupRequest request = new SignupRequest(
                    "poke1234"
            );

            given(spec).log().all()
                    .filter(document("signup-success", resource(successSnippets)))
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/auth/signup")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("이미 가입된 회원이 있으면 예외가 발생한다.")
        @Test
        void should_throwException_when_givenAlreadyExistMember() {
            SignupRequest request = new SignupRequest(
                    "dora1234"
            );

            given(spec).log().all()
                    .filter(document("signup-fail-duplicated-member", resource(failSnippets)))
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/auth/signup")
                    .then().log().all()
                    .statusCode(409);
        }
    }

    @DisplayName("토큰 재발급")
    @Nested
    class Refresh {

        List<HeaderDescriptorWithType> responseHeaderDescriptors = List.of(
                headerWithName("Set-Cookie").description("""
                        access_token=a.b.c; Path=/; HttpOnly \n
                        refresh_token=a.b.c; Path=/; HttpOnly
                        """)
        );
        ResourceSnippetParameters successSnippets = builder()
                .summary("토큰 재발급")
                .description("토큰을 재발급합니다.")
                .responseHeaders(responseHeaderDescriptors)
                .build();
        ResourceSnippetParameters failedSnippets = builder()
                .responseFields(failResponseDescriptors)
                .requestSchema(schema("RefreshFailResponse"))
                .build();

        @Value("${security.jwt.token.refresh-secret-key}")
        String refreshSecretKey;

        @Value("${security.jwt.token.refresh-token-expired}")
        Duration refreshTokenExpired;

        MemberEntity member;
        Date now;

        @BeforeEach
        void setUp() {
            member = memberFixture.createMember();
            now = Date.from(clock.instant());
        }

        @DisplayName("refreshToken으로 accessToken과 refreshToken을 재발급 한다.")
        @Test
        void should_refreshSuccess_when_givenRefreshToken() {

            given(spec).log().all()
                    .filter(document("refresh-success", resource(successSnippets)))
                    .cookies(cookieProvider.createCookies())
                    .when().post("/auth/refresh")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("유효하지 않은 refeshToken인 경우 예외가 발생한다.")
        @Test
        void should_throwException_when_givenInvalidRefreshToken() {

            given(spec).log().all()
                    .filter(document("refresh-fail-invalid-token", resource(failedSnippets)))
                    .cookie("refresh_token", "invalidRefreshToken")
                    .when().post("/auth/refresh")
                    .then().log().all()
                    .statusCode(401);
        }

        @DisplayName("만료된 refeshToken인 경우 예외가 발생한다.")
        @Test
        void should_throwException_when_givenExpiredRefreshToken() {
            Date alreadyExpiredAt = new Date(now.getTime() - refreshTokenExpired.toMillis());
            String expiredToken = Jwts.builder()
                    .setSubject(member.getId().toString())
                    .setExpiration(alreadyExpiredAt)
                    .signWith(SignatureAlgorithm.HS256, refreshSecretKey)
                    .compact();

            given(spec).log().all()
                    .filter(document("refresh-fail-expired-token", resource(failedSnippets)))
                    .cookie("refresh_token", expiredToken)
                    .when().post("/auth/refresh")
                    .then().log().all()
                    .statusCode(401);
        }
    }
}

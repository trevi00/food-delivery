package com.portfolio.food_delivery.domain.user.controller;

import com.portfolio.food_delivery.common.BaseIntegrationTest;
import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.user.dto.LoginRequest;
import com.portfolio.food_delivery.domain.user.dto.LoginResponse;
import com.portfolio.food_delivery.domain.user.dto.UserRegisterRequest;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerUser_Success() throws Exception {
        // given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("password123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .build();

        // when & then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 실패")
    void registerUser_DuplicateEmail_BadRequest() throws Exception {
        // given
        User existingUser = User.builder()
                .email("existing@example.com")
                .password(passwordEncoder.encode("password"))
                .name("기존유저")
                .phoneNumber("010-0000-0000")
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(existingUser);

        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("existing@example.com")
                .password("password123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        // when & then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("U001"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 회원가입 실패")
    void registerUser_InvalidEmail_BadRequest() throws Exception {
        // given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("invalid-email")
                .password("password123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        // when & then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].reason").value("올바른 이메일 형식이 아닙니다."));
    }

    @Test
    @DisplayName("잘못된 비밀번호 형식으로 회원가입 실패")
    void registerUser_InvalidPassword_BadRequest() throws Exception {
        // given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("weak")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        // when & then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest("test@example.com", "password123!");

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_UserNotFound_Unauthorized() throws Exception {
        // given
        LoginRequest request = new LoginRequest("notfound@example.com", "password123!");

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("U002"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_InvalidPassword_BadRequest() throws Exception {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("correctPassword"))
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("U003"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() throws Exception {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .role(UserRole.CUSTOMER)
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .build();
        User savedUser = userRepository.save(user);

        // 로그인하여 토큰 발급
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        String token = loginResponse.getAccessToken();

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.address.city").value("서울시"))
                .andExpect(jsonPath("$.address.district").value("강남구"));
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 인증되지 않은 사용자")
    void getMyInfo_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(status().isForbidden());  // Spring Security가 403을 반환
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 유효하지 않은 토큰")
    void getMyInfo_InvalidToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
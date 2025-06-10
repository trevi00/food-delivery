package com.portfolio.food_delivery.domain.user.controller;

import com.portfolio.food_delivery.common.BaseIntegrationTest;
import com.portfolio.food_delivery.common.entity.Address;
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
}
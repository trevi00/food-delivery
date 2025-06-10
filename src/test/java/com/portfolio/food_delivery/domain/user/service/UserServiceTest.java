package com.portfolio.food_delivery.domain.user.service;

import com.portfolio.food_delivery.domain.user.dto.UserRegisterRequest;
import com.portfolio.food_delivery.domain.user.dto.UserResponse;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.exception.DuplicateEmailException;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void registerUser_Success() {
        // given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("password123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // ID를 설정하여 저장된 것처럼 시뮬레이션
            return User.builder()
                    .id(1L)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(user.getName())
                    .phoneNumber(user.getPhoneNumber())
                    .build();
        });

        // when
        UserResponse response = userService.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 실패")
    void registerUser_DuplicateEmail_ThrowsException() {
        // given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("existing@example.com")
                .password("password123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}
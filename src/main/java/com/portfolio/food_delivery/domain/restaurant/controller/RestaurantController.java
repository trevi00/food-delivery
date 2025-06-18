package com.portfolio.food_delivery.domain.restaurant.controller;

import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantCreateRequest;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantResponse;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantUpdateRequest;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
import com.portfolio.food_delivery.domain.restaurant.service.RestaurantService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import com.portfolio.food_delivery.presentation.advice.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Restaurants", description = "레스토랑 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final UserService userService;

    @Operation(summary = "레스토랑 등록", description = "새로운 레스토랑을 등록합니다. RESTAURANT_OWNER 권한이 필요합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "레스토랑 등록 성공",
                    content = @Content(schema = @Schema(implementation = RestaurantResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (RESTAURANT_OWNER 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Parameter(description = "레스토랑 등록 정보", required = true)
            @Valid @RequestBody RestaurantCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        log.debug("Creating restaurant for user: {}", email);
        Long userId = userService.getUserIdByEmail(email);

        RestaurantResponse response = restaurantService.createRestaurant(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "레스토랑 상세 조회", description = "레스토랑의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RestaurantResponse.class))),
            @ApiResponse(responseCode = "404", description = "레스토랑을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurant(
            @Parameter(description = "레스토랑 ID", required = true, example = "1")
            @PathVariable Long id) {
        RestaurantResponse response = restaurantService.getRestaurant(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "레스토랑 목록 조회", description = "레스토랑 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> getRestaurants(
            @Parameter(description = "레스토랑 상태", example = "OPEN")
            @RequestParam(defaultValue = "OPEN") RestaurantStatus status,
            @Parameter(description = "레스토랑 카테고리", example = "KOREAN")
            @RequestParam(required = false) RestaurantCategory category,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<RestaurantResponse> response = restaurantService.getRestaurants(status, category, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "레스토랑 정보 수정", description = "레스토랑 정보를 수정합니다. 레스토랑 소유자만 수정 가능합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = RestaurantResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (레스토랑 소유자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "레스토랑을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @Parameter(description = "레스토랑 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "레스토랑 수정 정보", required = true)
            @Valid @RequestBody RestaurantUpdateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        log.debug("Updating restaurant {} for user: {}", id, email);
        Long userId = userService.getUserIdByEmail(email);

        RestaurantResponse response = restaurantService.updateRestaurant(id, userId, request);
        return ResponseEntity.ok(response);
    }
}
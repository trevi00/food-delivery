package com.portfolio.food_delivery.domain.menu.controller;

import com.portfolio.food_delivery.domain.menu.dto.MenuCreateRequest;
import com.portfolio.food_delivery.domain.menu.dto.MenuResponse;
import com.portfolio.food_delivery.domain.menu.dto.MenuUpdateRequest;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.service.MenuService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import com.portfolio.food_delivery.presentation.advice.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Menus", description = "메뉴 관련 API")
@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final UserService userService;

    @Operation(summary = "메뉴 등록", description = "레스토랑에 새로운 메뉴를 등록합니다. 레스토랑 소유자만 등록 가능합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "메뉴 등록 성공",
                    content = @Content(schema = @Schema(implementation = MenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (레스토랑 소유자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "레스토랑을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/restaurants/{restaurantId}/menus")
    public ResponseEntity<MenuResponse> createMenu(
            @Parameter(description = "레스토랑 ID", required = true, example = "1")
            @PathVariable Long restaurantId,
            @Parameter(description = "메뉴 등록 정보", required = true)
            @Valid @RequestBody MenuCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        MenuResponse response = menuService.createMenu(restaurantId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "레스토랑 메뉴 목록 조회", description = "특정 레스토랑의 판매 중인 메뉴 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuResponse.class)))),
            @ApiResponse(responseCode = "404", description = "레스토랑을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/restaurants/{restaurantId}/menus")
    public ResponseEntity<List<MenuResponse>> getMenusByRestaurant(
            @Parameter(description = "레스토랑 ID", required = true, example = "1")
            @PathVariable Long restaurantId) {
        List<MenuResponse> responses = menuService.getMenusByRestaurant(restaurantId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "메뉴 정보 수정", description = "메뉴 정보를 수정합니다. 레스토랑 소유자만 수정 가능합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = MenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (레스토랑 소유자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/menus/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @Parameter(description = "메뉴 ID", required = true, example = "1")
            @PathVariable Long menuId,
            @Parameter(description = "메뉴 수정 정보", required = true)
            @Valid @RequestBody MenuUpdateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        MenuResponse response = menuService.updateMenu(menuId, userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "메뉴 상태 변경", description = "메뉴의 판매 상태를 변경합니다. (판매중/품절/숨김)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = MenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 상태값",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (레스토랑 소유자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/api/menus/{menuId}/status")
    public ResponseEntity<MenuResponse> updateMenuStatus(
            @Parameter(description = "메뉴 ID", required = true, example = "1")
            @PathVariable Long menuId,
            @Parameter(description = "메뉴 상태", required = true,
                    schema = @Schema(allowableValues = {"AVAILABLE", "SOLD_OUT", "HIDDEN"}))
            @RequestParam MenuStatus status) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        MenuResponse response = menuService.updateMenuStatus(menuId, userId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "메뉴 삭제", description = "메뉴를 삭제합니다. 실제로는 상태를 DELETED로 변경하는 소프트 삭제입니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (레스토랑 소유자 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @Parameter(description = "메뉴 ID", required = true, example = "1")
            @PathVariable Long menuId) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        menuService.deleteMenu(menuId, userId);
        return ResponseEntity.noContent().build();
    }
}
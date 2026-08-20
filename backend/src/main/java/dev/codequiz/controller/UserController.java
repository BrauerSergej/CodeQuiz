package dev.codequiz.controller;

import dev.codequiz.dto.user.PasswordChangeDto;
import dev.codequiz.dto.user.UserDto;
import dev.codequiz.dto.user.UserStatusUpdateDto;
import dev.codequiz.dto.user.UserUpdateDto;
import dev.codequiz.security.UserPrincipal;
import dev.codequiz.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// /users/me/** — операции пользователя над СВОИМ профилем: id берётся из
// токена (principal), а не из URL. Это единственный безопасный способ —
// если бы id передавался в пути (/users/{id}), пришлось бы в сервисе
// отдельно проверять "id из URL == id из токена", иначе пользователь A
// смог бы отредактировать профиль пользователя B, просто подставив чужой id.
//
// /users/{id} и /users/{id}/status — админские операции над ЛЮБЫМ
// пользователем, доступ ограничен ролью ADMIN в SecurityConfig
// (не здесь — см. пояснение в CategoryController про единую точку конфигурации).
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Профиль пользователя и админские операции над аккаунтами")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getById(principal.getUser().getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMyProfile(@Valid @RequestBody UserUpdateDto dto,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.updateProfile(principal.getUser().getId(), dto));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(@Valid @RequestBody PasswordChangeDto dto,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(principal.getUser().getId(), dto);
        return ResponseEntity.noContent().build();
    }

    // ADMIN-only — просмотр любого пользователя по id (например, для
    // модерации/поддержки). Правило доступа — в SecurityConfig.
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // ADMIN-only — блокировка/разблокировка чужого аккаунта.
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDto> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusUpdateDto dto) {
        return ResponseEntity.ok(userService.updateStatus(id, dto));
    }
}
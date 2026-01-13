package com.example.be.controller.user;

import com.example.be.dto.user.AdminUserOrderRowDTO;
import com.example.be.dto.user.UpdateUserAdminRequestDTO;
import com.example.be.dto.user.UploadAvatarResponseDTO;
import com.example.be.dto.user.UserAdminDTO;
import com.example.be.service.user.AdminUserService;
import com.example.be.service.user.UserAvatarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserAvatarService userAvatarService;

    // GET /api/admin/users?q=abc&status=ACTIVE&page=0&size=10&sort=createdAt,desc
    @GetMapping
    public Page<UserAdminDTO> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        return adminUserService.listUsers(q, status, pageable);
    }

    @GetMapping("/{id}")
    public UserAdminDTO get(@PathVariable Long id) {
        return adminUserService.getUser(id);
    }

    @PutMapping("/{id}")
    public UserAdminDTO update(@PathVariable Long id,
                               @RequestBody @Valid UpdateUserAdminRequestDTO req) {
        return adminUserService.updateUser(id, req);
    }

    @PatchMapping("/{id}/lock")
    public UserAdminDTO lock(@PathVariable Long id) {
        return adminUserService.lockUser(id);
    }

    @PatchMapping("/{id}/unlock")
    public UserAdminDTO unlock(@PathVariable Long id) {
        return adminUserService.unlockUser(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminUserService.deleteUser(id);
    }

    @PostMapping("/{id}/avatar")
    public UploadAvatarResponseDTO uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        return userAvatarService.adminUploadAvatar(id, file);
    }

    @GetMapping("/{id}/orders")
    public Page<AdminUserOrderRowDTO> ordersOfUser(@PathVariable Long id, Pageable pageable) {
        return adminUserService.getUserOrders(id, pageable);
    }
}

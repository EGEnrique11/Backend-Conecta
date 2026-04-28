package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.idat.BackEndConecta.entity.Role;
import pe.idat.BackEndConecta.repository.RoleRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<List<Role>> getRoles(Authentication authentication) {
        boolean isDeveloper = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_DEVELOPER"));

        List<Role> allRoles = roleRepository.findAll();

        if (isDeveloper) {
            return ResponseEntity.ok(allRoles);
        } else {
            // ADMIN only sees VENDEDOR and TECNICO
            List<Role> filteredRoles = allRoles.stream()
                    .filter(r -> r.getRoleName().equals("ROLE_VENDEDOR") || r.getRoleName().equals("ROLE_TECNICO"))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filteredRoles);
        }
    }
}

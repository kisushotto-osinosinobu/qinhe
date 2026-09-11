package com.retailable.supermarket.admin;

import com.retailable.supermarket.audit.AuditService;
import com.retailable.supermarket.common.ApiResponse;
import com.retailable.supermarket.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/admin/config") @PreAuthorize("hasRole('ADMIN')")
public class ConfigController {
    private final ConfigMapper mapper;private final AuditService audit;public ConfigController(ConfigMapper mapper,AuditService audit){this.mapper=mapper;this.audit=audit;}
    @GetMapping public ApiResponse<List<Map<String,Object>>> all(){return ApiResponse.ok(mapper.all());}
    @PutMapping("/{key}") public ApiResponse<Void> put(@PathVariable @Pattern(regexp="^[a-z][a-z0-9.-]{1,79}$")String key,@Valid @RequestBody ConfigRequest r,@AuthenticationPrincipal UserPrincipal p){mapper.upsert(key,r.value(),r.description(),p.id());audit.record("CONFIG_UPDATE","SYSTEM_CONFIG",key,r.description());return ApiResponse.ok();}
    public record ConfigRequest(@NotBlank @Size(max=500)String value,@Size(max=255)String description){}
}


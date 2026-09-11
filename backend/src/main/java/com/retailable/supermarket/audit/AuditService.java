package com.retailable.supermarket.audit;

import com.retailable.supermarket.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {
    private final AuditMapper mapper;

    public AuditService(AuditMapper mapper) {
        this.mapper = mapper;
    }

    public void record(String action, String type, Object id, String detail) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = auth != null && auth.getPrincipal() instanceof UserPrincipal p ? p : null;
        mapper.insert(principal == null ? null : principal.id(), principal == null ? null : principal.username(), action,
                type, id == null ? null : id.toString(), detail, clientIp(), true);
    }

    private String clientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return "system";
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}

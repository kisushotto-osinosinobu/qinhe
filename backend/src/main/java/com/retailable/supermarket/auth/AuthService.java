package com.retailable.supermarket.auth;

import com.retailable.supermarket.audit.AuditService;
import com.retailable.supermarket.common.BusinessException;
import com.retailable.supermarket.security.JwtService;
import com.retailable.supermarket.security.UserAccount;
import com.retailable.supermarket.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.retailable.supermarket.auth.AuthDtos.*;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService, AuditService auditService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public UserView register(RegisterRequest request) {
        if (userMapper.findByUsername(request.username()) != null) throw BusinessException.conflict("用户名已存在");
        UserAccount user = new UserAccount();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        userMapper.insert(user);
        auditService.record("USER_REGISTER", "USER", user.getId(), "公共注册普通会员");
        return view(userMapper.findById(user.getId()));
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        UserAccount user = userMapper.findByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) throw BusinessException.forbidden("账号已被停用");
        UserPrincipal principal = UserPrincipal.from(user);
        JwtService.IssuedToken token = jwtService.issue(principal);
        userMapper.insertSession(token.jti(), user.getId(), token.expiresAt());
        auditService.record("USER_LOGIN", "USER", user.getId(), "账号密码登录");
        return new AuthResult(token.value(), token.expiresAt().toString(), view(user));
    }

    @Transactional
    public void logout(String jti, Long userId) {
        if (jti != null) userMapper.revokeSession(jti);
        auditService.record("USER_LOGOUT", "USER", userId, "当前会话注销");
    }

    public UserView profile(Long id) {
        UserAccount user = userMapper.findById(id);
        if (user == null) throw BusinessException.notFound("用户不存在");
        return view(user);
    }

    @Transactional
    public UserView updateProfile(Long id, ProfileRequest request) {
        UserAccount user = userMapper.findById(id);
        user.setDisplayName(request.displayName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setAvatarUrl(request.avatarUrl());
        userMapper.updateProfile(user);
        auditService.record("PROFILE_UPDATE", "USER", id, "更新个人资料");
        return view(userMapper.findById(id));
    }

    @Transactional
    public void changePassword(Long id, PasswordRequest request) {
        UserAccount user = userMapper.findById(id);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) throw BusinessException.badRequest("原密码错误");
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) throw BusinessException.badRequest("新密码不能与原密码相同");
        userMapper.updatePassword(id, passwordEncoder.encode(request.newPassword()));
        userMapper.revokeAllSessions(id);
        auditService.record("PASSWORD_CHANGE", "USER", id, "修改密码并注销全部会话");
    }

    private UserView view(UserAccount u) {
        return new UserView(u.getId(), u.getUsername(), u.getDisplayName(), u.getPhone(), u.getEmail(), u.getAvatarUrl(), u.getRole());
    }
}


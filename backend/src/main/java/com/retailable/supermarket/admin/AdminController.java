package com.retailable.supermarket.admin;

import com.retailable.supermarket.audit.AuditMapper;
import com.retailable.supermarket.audit.AuditService;
import com.retailable.supermarket.auth.UserMapper;
import com.retailable.supermarket.common.ApiResponse;
import com.retailable.supermarket.common.BusinessException;
import com.retailable.supermarket.common.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserMapper users;private final AuditMapper audits;private final AuditService audit;
    public AdminController(UserMapper users,AuditMapper audits,AuditService audit){this.users=users;this.audits=audits;this.audit=audit;}

    @GetMapping("/users") public ApiResponse<PageResult<Map<String,Object>>> users(@RequestParam(required=false)String keyword,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int pageSize){int p=Math.max(1,page),s=Math.min(100,Math.max(1,pageSize));return ApiResponse.ok(PageResult.of(users.page(keyword,(p-1)*s,s),users.count(keyword),p,s));}
    @PutMapping("/users/{id}/role-status") public ApiResponse<Void> roleStatus(@PathVariable Long id,@Valid @RequestBody RoleStatusRequest r){if(users.findById(id)==null)throw BusinessException.notFound("用户不存在");users.updateRoleStatus(id,r.role(),r.status());users.revokeAllSessions(id);audit.record("USER_ROLE_STATUS","USER",id,r.role()+" / "+r.status());return ApiResponse.ok();}
    @GetMapping("/roles") public ApiResponse<List<RoleView>> roles(){return ApiResponse.ok(List.of(
            new RoleView("ADMIN","管理员",List.of("全部系统权限","用户与配置管理","审计与统计导出")),
            new RoleView("INVENTORY_MANAGER","库存管理员",List.of("分类、商品、供应商","采购入库","库存与盘点","库存预警")),
            new RoleView("CASHIER","收银员",List.of("商品查询","Web收银","收款与退货","销售单与库存只读")),
            new RoleView("MEMBER","普通会员",List.of("公开商品","本人购物车和订单","取消本人待付款订单"))));}
    @GetMapping("/audits") public ApiResponse<PageResult<Map<String,Object>>> audits(@RequestParam(required=false)String keyword,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int pageSize){int p=Math.max(1,page),s=Math.min(100,Math.max(1,pageSize));return ApiResponse.ok(PageResult.of(audits.page(keyword,(p-1)*s,s),audits.count(keyword),p,s));}
    public record RoleStatusRequest(@NotNull @Pattern(regexp="ADMIN|INVENTORY_MANAGER|CASHIER|MEMBER")String role,@NotNull @Pattern(regexp="ACTIVE|DISABLED")String status){}
    public record RoleView(String code,String name,List<String> permissions){}
}


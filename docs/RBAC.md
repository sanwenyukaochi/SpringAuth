# 数据表设计

## User表
```java
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@Table(name = "sys_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        })
@Comment("用户表")
@Schema(name = "User", title = "用户对象", description = "系统用户实体，包含认证和状态信息")
public class User extends BaseEntity {

    @Schema(title = "用户名", example = "alice", description = "系统登录名，唯一标识用户")
    @Column(name = "username", nullable = false, length = 20)
    @Comment("用户名")
    private String username;

    @Schema(title = "邮箱", example = "alice@example.com", description = "用户绑定邮箱，用于找回密码或登录")
    @Column(name = "email", nullable = false, length = 50)
    @Comment("邮箱")
    private String email;

    @Schema(title = "密码", description = "加密后的密码，不会在接口返回中显示", accessMode = Schema.AccessMode.WRITE_ONLY)
    @Column(name = "password", nullable = false, length = 120)
    @JsonIgnore
    @Comment("用户密码")
    private String password;

    @Schema(title = "账户是否未锁定", description = "true 表示正常，false 表示锁定")
    @Column(name = "account_non_locked", nullable = false)
    @Comment("账户是否未锁定（true=正常，false=锁定）")
    private Boolean accountNonLocked = true;

    @Schema(title = "账户是否未过期", description = "true 表示有效，false 表示过期")
    @Column(name = "account_non_expired", nullable = false)
    @Comment("账户是否未过期（true=有效，false=过期）")
    private Boolean accountNonExpired = true;

    @Schema(title = "密码是否未过期", description = "true 表示有效，false 表示密码已过期")
    @Column(name = "credentials_non_expired", nullable = false)
    @Comment("密码是否未过期（true=有效，false=已过期）")
    private Boolean credentialsNonExpired = true;

    @Schema(title = "是否启用", description = "true 表示账户启用，false 表示禁用")
    @Column(name = "enabled", nullable = false)
    @Comment("状态（true=启用，false=禁用）")
    private Boolean enabled = true;

    @Schema(title = "双因素认证密钥", example = "JBOSSWS3DPKG3PXP", description = "TOTP Secret，用于Google Authenticator等")
    @Column(name = "two_factor_secret", nullable = true, length = 64)
    @Comment("双因素认证密钥（TOTP Secret，用于Google Authenticator等）")
    private String twoFactorSecret;

    @Schema(title = "是否启用双因素认证", description = "true 表示启用，false 表示未启用")
    @Column(name = "two_factor_enabled", nullable = false)
    @Comment("是否启用双因素认证（true=启用，false=未启用）")
    private Boolean twoFactorEnabled = false;

    @Schema(title = "注册方式", description = "用户注册来源，如 EMAIL、GITHUB、GOOGLE、WECHAT、SYSTEM 等")
    @Column(name = "sign_up_method", nullable = false, length = 10)
    @Comment("注册方式（EMAIL/GITHUB/GOOGLE/WECHAT/SYSTEM等）")
    private String signUpMethod;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<UserRole> roles = new ArrayList<>();
}

```

## UserRole表
```java
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user_role_rel",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
        })
@Comment("用户角色关联表")
public class UserRole extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_role_user_id"))
    @Comment("用户Id")
    private User user;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_role_role_id"))
    @Comment("角色Id")
    private Role role;
}
```

## Role表
```java
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@Table(name = "sys_role",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_code", columnNames = "code")
        })
@Comment("角色表")
@Schema(name = "Role", title = "角色对象", description = "系统角色实体，用于定义权限分组和访问控制")
public class Role extends BaseEntity {

    @Schema(title = "角色名称", example = "管理员", description = "角色中文名称，用于显示")
    @Column(name = "name", length = 10, nullable = false)
    @Comment("角色名称")
    private String name;

    @Schema(title = "角色编码", example = "ROLE_ADMIN", description = "角色唯一编码，用于权限标识，如 ROLE_USER、ROLE_ADMIN")
    @Column(name = "code", length = 10, nullable = false)
    @Comment("权限标识")
    private String code;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<UserRole> users = new ArrayList<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<RolePermission> permissions = new ArrayList<>();
}
```

## RolePermission表
```java
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_role_permission_rel",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_permission", columnNames = {"role_id", "permission_id"})
        })
@Comment("角色权限关联表")
public class RolePermission extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_permission_role_id"))
    @Comment("角色Id")
    private Role role;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_permission_permission_id"))
    @Comment("权限Id")
    private Permission permission;
}
```

## Permission表
```java
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@Table(name = "sys_permission",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permission_code", columnNames = "code")
        })
@Comment("权限表")
@Schema(name = "Permission", title = "权限对象", description = "系统权限定义表，用于接口访问控制")
public class Permission extends BaseEntity {

    @Schema(title = "父级ID", description = "父权限ID，用于构建权限树结构", example = "0")
    @Column(name = "parent_id", nullable = false)
    @Comment("父级ID")
    private Long parentId;

    @Schema(title = "权限名称", description = "权限中文名称，用于界面显示", example = "创建用户")
    @Column(name = "name", length = 10, nullable = false)
    @Comment("权限名称")
    private String name;

    @Schema(title = "权限编码", description = "权限唯一标识（如 USER_CREATE、ROLE_DELETE 等）", example = "USER_CREATE")
    @Column(name = "code", length = 30, nullable = false)
    @Comment("权限编码")
    private String code;

    @Schema(title = "请求路径", description = "接口的请求路径（可用于精细化权限控制）", example = "/api/users")
    @Column(name = "request_url", length = 200)
    @Comment("请求路径")
    private String requestUrl;

    @Schema(title = "请求方法", description = "HTTP 方法（GET/POST/PUT/DELETE等）", example = "POST")
    @Column(name = "request_method", length = 10)
    @Enumerated(EnumType.STRING)
    @Comment("请求方法")
    private HttpMethod requestMethod;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<RolePermission> roles = new ArrayList<>();


    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
    }

}
```

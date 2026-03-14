create table sys_role
(
    id   bigint      not null,
    code varchar(10) not null,
    primary key (id),
    constraint uk_role_code unique (code)
);
comment
on table sys_role is '角色表';
comment
on column sys_role.id is '主键ID';
comment
on column sys_role.code is '权限标识';
create table sys_user
(
    account_non_expired     boolean      not null,
    account_non_locked      boolean      not null,
    credentials_non_expired boolean      not null,
    enabled                 boolean      not null,
    mfa_enabled             boolean      not null,
    id                      bigint       not null,
    username                varchar(20)  not null,
    email                   varchar(50)  not null,
    phone                   varchar(50)  not null,
    mfa_secret              varchar(64),
    password_hash           varchar(120) not null,
    primary key (id),
    constraint uk_user_username unique (username),
    constraint uk_user_email unique (email)
);
comment
on table sys_user is '用户表';
comment
on column sys_user.account_non_expired is '账户是否未过期（true=有效，false=过期）';
comment
on column sys_user.account_non_locked is '账户是否未锁定（true=正常，false=锁定）';
comment
on column sys_user.credentials_non_expired is '密码是否未过期（true=有效，false=已过期）';
comment
on column sys_user.enabled is '状态（true=启用，false=禁用）';
comment
on column sys_user.mfa_enabled is '是否启用双因素认证（true=启用，false=未启用）';
comment
on column sys_user.id is '主键ID';
comment
on column sys_user.username is '用户名';
comment
on column sys_user.email is '邮箱';
comment
on column sys_user.phone is '手机号';
comment
on column sys_user.mfa_secret is '双因素认证密钥';
comment
on column sys_user.password_hash is '用户密码';
create table sys_user_identity
(
    id               bigint      not null,
    provider_user_id bigint      not null,
    user_id          bigint      not null,
    provider         varchar(30) not null check ((provider in ('GITHUB', 'GOOGLE', 'WECHAT'))),
    primary key (id),
    constraint uk_user_identity_user_provider unique (user_id, provider)
);
comment
on table sys_user_identity is '用户第三方登录绑定表';
comment
on column sys_user_identity.id is '主键ID';
comment
on column sys_user_identity.provider_user_id is '第三方用户唯一ID';
comment
on column sys_user_identity.user_id is '用户名';
comment
on column sys_user_identity.provider is '登录提供商';
create table sys_user_role_rel
(
    id      bigint not null,
    role_id bigint not null,
    user_id bigint not null,
    primary key (id),
    constraint uk_user_role unique (user_id, role_id)
);
comment
on table sys_user_role_rel is '用户角色关联表';
comment
on column sys_user_role_rel.id is '主键ID';
comment
on column sys_user_role_rel.role_id is '角色Id';
comment
on column sys_user_role_rel.user_id is '用户Id';
alter table if exists sys_user_role_rel add constraint fk_user_role_role_id foreign key (role_id) references sys_role;
alter table if exists sys_user_role_rel add constraint fk_user_role_user_id foreign key (user_id) references sys_user;


INSERT INTO public.sys_user (id, account_non_expired, account_non_locked, credentials_non_expired, email, enabled,
                             password_hash, phone, mfa_enabled, mfa_secret, username)
VALUES (1978249459921846272, true, true, true, 'sanwenyukaochi@outlook.com', true,
        '$2a$10$ZJ3fmKhaAyFzn/p6yJnpb.YLQxLFhxqp.w/NN0I1mj4FlM1vRt0eu', '18867102143', false, null, 'sanwenyukaochi');

INSERT INTO public.sys_user_identity (id, provider_user_id, user_id, provider)
VALUES (1978249459921846273, 75579954, 1978249459921846272, 'GITHUB');

INSERT INTO public.sys_role (id, code)
VALUES (1978249459921846274, 'user'),
       (1978249459921846275, 'admin');

INSERT INTO public.sys_user_role_rel (id, role_id, user_id)
VALUES (1978249459921846276, 1978249459921846275, 1978249459921846272);

# Spring Security Project

<p align="center">
    <img src="assets/spring-security.svg" height="200" alt="Spring Security Logo"/>
</p>

<p align="center">
  <a href="https://github.com/sanwenyukaochi/SpringSecurity/stargazers"><img src="https://img.shields.io/github/stars/sanwenyukaochi/SpringSecurity?style=social" alt="GitHub Stars"></a>
  <a href="https://github.com/sanwenyukaochi/SpringSecurity/network/members"><img src="https://img.shields.io/github/forks/sanwenyukaochi/SpringSecurity" alt="GitHub Forks"></a>
  <a href="https://github.com/sanwenyukaochi/SpringSecurity/graphs/contributors"><img src="https://img.shields.io/github/contributors/sanwenyukaochi/SpringSecurity" alt="GitHub Contributors"></a>
  <a href="https://github.com/sanwenyukaochi/SpringSecurity/blob/main/LICENSE"><img src="https://img.shields.io/github/license/sanwenyukaochi/SpringSecurity" alt="GitHub License"></a>
  <a href="https://github.com/sanwenyukaochi/SpringSecurity"><img src="https://img.shields.io/github/languages/top/sanwenyukaochi/SpringSecurity" alt="Top Language"></a>
  <a href="https://github.com/sanwenyukaochi/SpringSecurity"><img src="https://img.shields.io/github/repo-size/sanwenyukaochi/SpringSecurity" alt="Repo Size"></a>
  <a href="https://github.com/sanwenyukaochi/SpringSecurity/issues"><img src="https://img.shields.io/github/issues/sanwenyukaochi/SpringSecurity" alt="GitHub Issues"></a>
</p>

---

## 🚀 项目简介

Spring Security Project 是一个基于 **Spring Boot** 的安全认证与授权服务，集成 **JWT (JSON Web Token)** 实现无状态身份验证。项目提供了完整的用户认证、授权和资源访问控制功能，同时集成了 **OpenAPI (Swagger)**，便于接口调试与开发。

* **技术栈**：Java | Spring Boot | Spring Security | JWT | Springdoc OpenAPI | Lombok
* **用途**：提供企业级安全认证与授权解决方案，支持用户登录、注册、权限管理等功能。
* **流程图**：
<p align="center">
    <img src="assets/spring-security-authentication-flow.svg" alt="Authentication Flow"/>
</p>
---

## ⚡ 快速开始

### 环境要求

* **JDK**
* **Gradle**
* **数据库** (PostgreSQL)

### 安装与运行

```bash
# 克隆项目
git clone https://github.com/sanwenyukaochi/Spring-Security.git
cd Spring-Security

# 构建项目
./gradlew build

# 启动项目
./gradlew bootRun
```

> 💡 Tip：你也可以使用 Gradle Wrapper `./gradlew` 来保证与项目一致的 Gradle 版本。

### 配置数据库

在 `src/main/resources/application.properties` 中配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/security_db
spring.datasource.username=<你的用户名>
spring.datasource.password=<你的密码>
```

---

## ✨ 主要功能

* **用户认证**：登录、注册、密码重置
* **JWT 认证**：基于 Token 的无状态身份验证
* **权限控制**：基于角色的访问控制 (RBAC)
* **安全过滤器**：请求过滤与安全检查
* **异常处理**：统一的安全异常处理机制
* **OpenAPI (Swagger) 在线接口文档**

### API 文档

访问 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) 查看所有接口。

---

## 📁 项目结构

```
Spring-Security/
├── src/main/java/com/secure/security/
│   ├── SecurityApplication.java       # 启动类
│   ├── authentication/                # 认证相关
│   │   ├── config/                    # 安全配置
│   │   ├── filter/                    # JWT过滤器
│   │   ├── handler/                   # 处理器
│   │   └── service/                   # 认证服务
│   ├── common/                        # 通用组件
│   │   └── web/                       # Web相关
│   ├── domain/                        # 领域模型
│   │   ├── model/                     # 数据模型
│   │   └── repository/                # 数据访问
│   └── test/                          # 测试示例
├── src/main/resources/
│   └── application.properties         # 配置文件
├── build.gradle                       # 构建脚本
```

---

## 💡 使用建议

* 生产环境部署前请修改 JWT 密钥配置
* 建议启用 HTTPS 以保证 Token 传输安全
* 可结合 CI/CD 自动化部署
* 根据业务需求扩展用户权限模型

---

## 📞 联系与支持

如需定制化开发或技术支持，请联系作者：

* GitHub: `https://github.com/sanwenyukaochi`
* 邮箱: `sanwenyukaochi@outlook.com`

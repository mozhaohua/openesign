# OpenESign

开源电子签名解决方案 - DocuSign 替代品

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2
- Spring Security
- MyBatis-Plus
- MySQL
- JWT

### 前端
- Vue 3
- Vite
- Pinia
- Element Plus

## 快速开始

### 前置要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 后端启动

1. 创建数据库:
```bash
mysql -u root -p < openesign-server/src/main/resources/schema.sql
```

2. 修改配置 (application.yml):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/openesign
    username: your_username
    password: your_password
```

3. 启动后端:
```bash
cd openesign-server
./mvnw spring-boot:run
```

后端将在 http://localhost:8080 启动

### 前端启动

```bash
cd openesign-web
npm install
npm run dev
```

前端将在 http://localhost:5173 启动

## API 文档

### 认证

- `POST /api/auth/register` - 注册
- `POST /api/auth/login` - 登录
- `GET /api/auth/me` - 获取当前用户

### 文档管理

- `POST /api/documents/upload` - 上传文档
- `POST /api/documents` - 创建文档
- `GET /api/documents` - 文档列表
- `GET /api/documents/{id}` - 文档详情
- `POST /api/documents/{id}/send` - 发送文档
- `POST /api/documents/sign` - 签署文档
- `GET /api/documents/{id}/download` - 下载文档

## 功能特性

- [x] 用户注册/登录
- [x] 文档上传
- [x] 文档管理
- [x] 发送签名请求
- [x] 电子签名（手写/文本）
- [x] 签名状态跟踪

## License

MIT

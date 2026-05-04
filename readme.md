<<<<<<< HEAD
# 世界杯论坛

一款极简的世界杯讨论论坛，用户可自由发帖、回复；管理员可管理内容。

---

## 一、架构总览

```
┌─────────────────────────────────────────────────────────┐
│                    用户浏览器 (Vue 3)                     │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP / JSON
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Nginx（可选）:80                             │
│   / → 前端静态文件  |  /api → 反向代理到后端:8080         │
└──────────────────────┬──────────────────────────────────┘
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot API :8080                       │
│                                                         │
│  ┌──────────────┐  ┌──────────────────┐                 │
│  │  Controller   │  │   Interceptor    │                 │
│  │  (接收请求)    │─▶│  (认证/权限校验)   │                 │
│  └──────┬───────┘  └──────────────────┘                 │
│         │                                                │
│         ▼                                                │
│  ┌──────────────┐  ┌──────────────────┐                 │
│  │   Service     │  │   Aspect(AOP)    │                 │
│  │  (业务逻辑)    │─▶│  (日志/监控)      │                 │
│  └──────┬───────┘  └──────────────────┘                 │
│         │                                                │
│         ▼                                                │
│  ┌──────────────┐                                        │
│  │   Mapper     │  (MyBatis-Plus 数据访问)               │
│  └──────┬───────┘                                        │
└─────────┼────────────────────────────────────────────────┘
          │                          │
          ▼                          ▼
┌──────────────────┐   ┌──────────────────┐
│    MySQL :3306    │   │   Redis :6379    │
│  (帖子/回复/用户)  │   │ (Session/限流)   │
└──────────────────┘   └──────────────────┘
```

---

## 二、请求全流程

以「用户发帖」为例，完整经过：

```
1. 浏览器 POST /api/posts  →  Nginx 转发
2. Controller 接收 JSON，@Valid 校验参数
3. Service.createPost()
   ├── IpUtils 取客户端 IP
   ├── RateLimitService.tryAcquire(ip)   ← Redis 计数限流
   ├── SensitiveWordUtils.filter()        ← 敏感词替换
   └── PostMapper.insert()               ← 写入 MySQL
4. AOP @Loggable 记录调用日志
5. 统一 Result 返回 JSON
```

以「管理员删除帖子」为例：

```
1. 浏览器 DELETE /api/posts/1
   Headers: Authorization: Bearer {uuid}
2. AdminAuthInterceptor 前置拦截
   ├── 取 Authorization header
   ├── TokenService.validate(token)      ← 查 Redis 有无此 key
   ├── 提取 role，校验是否为 "admin"
   └── 不通过则直接返回 401/503 JSON
3. Controller 接收 → Service 软删除
4. AOP 记录操作日志
5. 统一 Result 返回
```

---

## 三、后端代码结构

```
=======
我准备设计一款极简的网页，用来应对即将到来的世界杯，就一个赛前赛后讨论世界杯的论坛，里面可以自由发帖，自由发言那种，我准备设计一个前端和一个后端，后端有管理员，前端渲染论坛讨论
一、总体架构

用户浏览器
↓
Nginx (代理 + 静态文件)
↓
Spring Boot API (端口 8080)
↓
MySQL (数据存储) + Redis (限流/缓存)

(暂时小项目用这么点架构够了，还是多想想怎么把项目设计的有趣一点)
二、前端架构 (Vue 3，还没做,还有用户登录没加)
目录结构
text
src/
├── api/              # API 接口层

│   ├── client.js     # axios 实例

│   ├── post.js       # 帖子接口

│   └── reply.js      # 回复接口

├── components/       # 组件

│   ├── PostList.vue  # 帖子列表

│   ├── PostItem.vue  # 单个帖子

│   ├── PostForm.vue  # 发帖表单

│   ├── ReplyForm.vue # 回复表单

│   └── AdminPanel.vue# 管理员面板

├── views/            # 页面

│   └── Forum.vue     # 主论坛页面

├── stores/           # Pinia 状态

│   ├── forum.js      # 论坛状态

│   └── admin.js      # 管理员状态

├── router/           # 路由配置

├── utils/            # 工具函数

├── App.vue

└── main.js

核心页面
Forum.vue：唯一主页面，包含发帖区、帖子列表、回复区
状态管理
forum store：帖子列表、加载状态、发帖/回复操作
admin store：管理员登录状态、token 管理

三、后端架构 (Spring Boot)

包结构
text
>>>>>>> baf8ad744ddcd02a84ba81b4bbe49d32677c570a
com.worldcup.forum/
├── WorldCupForumApplication.java         # 启动入口 @MapperScan

├── controller/                           # HTTP 控制器
│   ├── PostController                    # GET/POST/DELETE /api/posts
│   ├── ReplyController                   # GET/POST/DELETE /api/replies
│   ├── UserController                    # POST /api/users/register|login|logout
│   ├── AdminController                   # POST /api/admin/login|logout
│   └── MonitorController                 # GET /api/monitor/snapshot（JVM 指标）

├── service/                              # 业务逻辑层
│   ├── PostService                       # 帖子 CRUD + 分页 + 回复摘要
│   ├── ReplyService                      # 回复 CRUD + 事务更新 reply_count
│   ├── UserService                       # 注册/登录(Bcrypt)/登出
│   ├── AdminService                      # 管理员认证（配置驱动）
│   ├── TokenService                      # UUID 生成 + Redis Session 管理
│   └── RateLimitService                  # IP 限流（Redis 计数窗口）

├── mapper/                               # MyBatis-Plus 接口
│   ├── PostMapper
│   ├── ReplyMapper
│   └── UserMapper

├── entity/                               # 数据实体
│   ├── Post                              # 帖子表映射
│   ├── Reply                             # 回复表映射
│   ├── User                              # 用户表映射
│   └── Admin                             # 管理员配置（非数据库）

├── dto/request/                          # 请求体
│   ├── PostCreateRequest
│   ├── ReplyCreateRequest
│   ├── RegisterRequest
│   ├── LoginRequest
│   └── AdminLoginRequest
├── dto/response/                         # 响应体
│   ├── PostVO                            # 帖子 + 最新 5 条回复
│   ├── ReplyVO
│   └── LoginResponse                     # token + 用户信息 + 角色

├── config/                               # 配置 + 插件
│   ├── WebConfig                         # CORS + 拦截器注册
│   ├── RedisConfig                       # RedisTemplate 序列化
│   ├── MyBatisPlusConfig                 # 分页插件
│   └── AopConfig                         # 开启 @Aspect

├── interceptor/
│   └── AdminAuthInterceptor              # 管理员权限拦截（Redis 校验 + 角色判断）

├── aspect/                               # AOP 切面
│   ├── Loggable.java                     # 自定义注解，标注需要日志的方法
│   └── LoggingAspect.java                # 前置/返回/异常三个通知，输出 info/warn 日志

├── common/                               # 通用组件
│   ├── Result                            # 统一响应 {code, message, data}
│   ├── GlobalExceptionHandler            # 全局异常 → 统一错误码
│   └── MyMetaObjectHandler               # 自动填充 createdAt

└── utils/
    ├── IpUtils                           # 获取真实 IP（穿透代理）
    └── SensitiveWordUtils                # 敏感词过滤（可扩展词库）
```

---

## 四、数据库设计

### post（帖子表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | PK 自增 |
| nickname | varchar(50) | 昵称 |
| content | text | 内容 |
| ip | varchar(45) | 发布 IP |
| reply_count | int | 回复数 |
| created_at | datetime | 创建时间 |
| is_deleted | tinyint | 软删除标识 |

### reply（回复表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | PK 自增 |
| post_id | bigint | 所属帖子 ID |
| nickname | varchar(50) | 昵称 |
| content | varchar(800) | 内容 |
| ip | varchar(45) | 回复 IP |
| created_at | datetime | 创建时间 |
| is_deleted | tinyint | 软删除标识 |

### user（用户表）

<<<<<<< HEAD
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | PK 自增 |
| username | varchar(30) | 用户名（唯一） |
| password | varchar(100) | BCrypt 加密后的密码 |
| nickname | varchar(50) | 显示昵称 |
| created_at | datetime | 注册时间 |
| is_deleted | tinyint | 软删除标识 |
=======

&#x20;   ├── IpUtils
>>>>>>> baf8ad744ddcd02a84ba81b4bbe49d32677c570a

---

<<<<<<< HEAD
## 五、API 清单

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/users/register` | 用户注册 | 否 |
| POST | `/api/users/login` | 登录，返回 UUID token | 否 |
| POST | `/api/users/logout` | 登出，撤销 Redis session | Bearer Token |
| POST | `/api/admin/login` | 管理员登录 | 否 |
| POST | `/api/admin/logout` | 管理员登出 | Bearer Token |
| GET | `/api/posts?page=1&size=20` | 帖子列表（分页，每帖含最新5条回复） | 否 |
| POST | `/api/posts` | 发布帖子 | 否 |
| DELETE | `/api/posts/{id}` | 管理员删除帖子 | Bearer Token(admin) |
| GET | `/api/replies/post/{postId}` | 获取某帖所有回复 | 否 |
| POST | `/api/replies` | 发布回复 | 否 |
| DELETE | `/api/replies/{id}` | 管理员删除回复 | Bearer Token(admin) |
| GET | `/api/monitor/snapshot` | JVM 指标快照（堆/线程/CPU） | 否 |
| GET | `/actuator/health` | 健康检查 | 否 |
| GET | `/actuator/info` | 应用信息 | 否 |

---

## 六、核心运行流程

### 6.1 认证与会话

```
用户/管理员登录
    │
    ├── 校验身份（用户→查MySQL+BCrypt；管理员→application.yml配置比对）
    │
    ├── TokenService.createSession(username, role)
    │       ├── 生成随机 UUID（32位，无横线）
    │       └── 写入 Redis → key=token:{uuid}, value={username}:{role}, TTL=86400s
    │
    └── 返回 { token, username, nickname, role }


每次请求携带 Authorization: Bearer {uuid}
    │
    ├── AdminAuthInterceptor.preHandle()   【仅拦截 /api/admin/** 路径】
    │       ├── 提取 header → 去掉 "Bearer " 前缀
    │       ├── TokenService.validate(token)
    │       │       └── GET token:{uuid} → 查到则返回 "username:role"，否则 null
    │       ├── TokenService.extractRole(value) → 提取 "admin"
    │       └── 非 admin → 401；Redis 异常 → 503
    │
    └── 通过 → Controller 正常处理


登出
    ├── TokenService.revoke(token)
    │       └── DEL token:{uuid}
    └── session 立即失效
```

### 6.2 帖子全生命周期

```
发帖
    ├── Controller @Valid 校验参数
    ├── Service
    │   ├── IpUtils.getClientIp()  ← 取 X-Forwarded-For / RemoteAddr
    │   ├── RateLimitService.tryAcquire(ip)  ← Redis INCR + EXPIRE 60s
    │   │       └── 超过 5 次/60s → 抛出 IllegalArgumentException
    │   ├── SensitiveWordUtils.filter(content)  ← 替换敏感词为 *
    │   ├── PostMapper.insert(post)  ← 写入 MySQL
    │   └── 返回 PostVO
    └── AOP → @Loggable 输出 "【发帖】PostService.createPost 调用" 日志


列表加载
    ├── Service
    │   ├── PostMapper.selectPage(page, wrapper)  ← MyBatis-Plus 分页
    │   └── 遍历每篇帖子
    │       └── ReplyService.getLatestRepliesByPostId(postId, 5)  ← 查最新5条
    └── 返回 Page<PostVO>（每帖含 replyCount + latestReplies）


回复
    ├── Service @Transactional
    │   ├── 校验帖子存在
    │   ├── IP 限流 + 敏感词过滤（同发帖）
    │   ├── ReplyMapper.insert(reply)
    │   ├── Post.setReplyCount(replyCount + 1) → PostMapper.updateById(post)
    │   └── 两条 SQL 在同一事务中
    └── 返回 ReplyVO


管理员删除
    ├── Interceptor 校验通过
    ├── Service
    │   ├── 查实体是否存在 → 不存在则 400
    │   └── MyBatis-Plus 逻辑删除（is_deleted = 1）
    └── 返回 Result.success()
```

### 6.3 异常处理链路

```
请求进入 → Controller → Service → Mapper
                                    │
                                    异常 ↓
                           GlobalExceptionHandler
                            │
                            ├── MethodArgumentNotValidException   → 400 (参数校验失败)
                            ├── HttpMessageNotReadableException  → 400 (JSON格式错误)
                            ├── IllegalArgumentException         → 400 (业务校验失败)
                            ├── IllegalStateException            → 503 (Redis不可用等)
                            ├── DataAccessException              → 503 (MySQL异常)
                            └── Exception                       → 500 (未预期错误)
                            │
                            └── 统一返回 Result{code, message, data}
```

### 6.4 AOP 日志切面

```
@Loggable("描述") 标注在 Service 方法上

执行顺序：
    @Before         → log.info("【描述】方法调用，参数：...")
    @AfterReturning → log.info("【方法名】方法返回：结果")
    @AfterThrowing  → log.warn("【方法名】方法异常：错误信息")
```

已标注 `@Loggable` 的方法：登录/注册/登出、发帖/删帖、回复/删回复、分页查询、限流检查、Token 创建/校验/撤销。

### 6.5 监控指标

```
GET /api/monitor/snapshot
    └── 聚合 Micrometer 指标，返回 JSON：
        {
            timestamp: 1714800000000,
            heapUsedMb: 128.5,
            heapMaxMb: 512.0,
            nonHeapUsedMb: 62.3,
            threadsLive: 15,
            threadsPeak: 22,
            cpuProcess: 0.02,
            cpuSystem: 0.15,
            uptimeSeconds: 3600.0
        }
```

底层数据与 `/actuator/metrics` 同源，前端可轮询绘图。

---

## 七、健壮性设计

| 场景 | 行为 |
|------|------|
| Redis 宕机（登录） | `TokenService` 捕获异常 → 503 "会话服务暂不可用" |
| Redis 宕机（限流） | `RateLimitService` 捕获异常 → **降级放行**，记录 warn 日志 |
| Redis 宕机（校验） | `AdminAuthInterceptor` 捕获 → 503，与 401 区分 |
| MySQL 宕机 | `DataAccessException` → 503 "数据服务暂不可用" |
| 参数校验失败 | `@Valid` + `GlobalExceptionHandler` → 400，拼接字段错误提示 |
| 请求体格式错误 | `HttpMessageNotReadableException` → 400 "请求体格式不正确" |
| Token 为空 | Interceptor 拦截 → 401 |
| 登出时 Header 缺失 | Controller `@RequestHeader` 自动 400 |
| 敏感词工具异常 | 捕获后回退为原文，不影响发帖 |
| IP 获取失败 | `IpUtils` 返回 `unknown` 兜底 |

---

## 八、部署架构

```
=======
API 设计(这里肯定也是不全面的)
方法	路径	说明
GET	/api/posts	帖子列表（分页+回复）
POST	/api/posts	发布帖子
DELETE	/api/posts/{id}	删除帖子（管理员）
POST	/api/replies	发布回复
DELETE	/api/replies/{id}	删除回复（管理员）
POST	/api/admin/login	管理员登录

四、数据库设计
表结构
post (帖子表)
字段	类型	说明
id	bigint	PK
nickname	varchar(50)	昵称
content	text	内容
ip	varchar(45)	发布IP
reply\_count	int	回复数
created\_at	datetime	创建时间
is\_deleted	tinyint	软删除



reply (回复表)
字段	类型	说明
id	bigint	PK
post\_id	bigint	所属帖子
nickname	varchar(50)	昵称
content	varchar(500)	内容
ip	varchar(45)	回复IP
created\_at	datetime	创建时间
is\_deleted	tinyint	软删除

五、核心流程
发帖流程
前端提交 → 后端接收 → IP限流检查 → 敏感词过滤 → 存入MySQL → 返回成功
帖子列表加载
前端请求 → 分页查询帖子 → 批量查询回复（每帖最新5条） → 组装VO → 返回
管理员删除
前端携带Token → 验证Token → 软删除 → 记录日志 → 返回成功
六、部署架构
>>>>>>> baf8ad744ddcd02a84ba81b4bbe49d32677c570a
Nginx :80
├── / → /var/www/forum/dist          (Vue 打包后的静态文件)
└── /api → http://localhost:8080     (反向代理到后端)

Spring Boot :8080
├── MySQL :3306                      (数据持久化)
└── Redis :6379                      (Session + 限流)
```

<<<<<<< HEAD
---
=======
└── /api → http://localhost:8080 (后端代理)
Spring Boot :8080 → MySQL :3306 + Redis :6379 暂时够用
>>>>>>> baf8ad744ddcd02a84ba81b4bbe49d32677c570a

## 九、技术栈

<<<<<<< HEAD
| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Pinia + Axios |
| 后端 | Spring Boot 3.5 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.0 |
| 缓存/Session | Redis |
| 密码加密 | BCrypt (`spring-security-crypto`) |
| 日志/AOP | Spring AOP + SLF4J |
| 监控 | Actuator + Micrometer |
| 部署 | Nginx + Jar 包 |

---

## 十、本地启动

### 前置条件

- JDK 17+
- MySQL 8.0
- Redis

### 步骤

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE worldcup_forum CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入表结构
mysql -u root -p worldcup_forum < soccer-forum/src/main/resources/sql/schema.sql

# 3. 修改配置
#   编辑 soccer-forum/src/main/resources/application.yml
#   spring.datasource.username / password（数据库账号）
#   spring.data.redis（Redis 地址）
#   admin.username / admin.password（管理员账号）
=======
七、技术栈总结
层级	技术
前端	Vue 3 + Vite + Pinia + Axios
后端	Spring Boot 3.x + MyBatis-Plus
数据库	MySQL 8.0
缓存/限流	Redis
八、待设计目标:表情包，内容添加
部署	Nginx + Jar包
>>>>>>> baf8ad744ddcd02a84ba81b4bbe49d32677c570a

# 4. 启动后端
cd soccer-forum
mvn spring-boot:run

# 5.（可选）启动前端
cd soccer-forum-frontend
npm install
npm run dev
```

默认管理员：`admin` / `admin123`（以 `application.yml` 中 `admin.*` 为准）。
API 地址：`http://localhost:8080`。

### 快速验证

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 帖子列表
curl http://localhost:8080/api/posts?page=1&size=5

# 注册用户
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","nickname":"测试用户"}'

# 登录
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 发帖
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"nickname":"测试用户","content":"世界杯预测！"}'
```

---

## 十一、项目目录

```
F:/code_bench/Soccer/
├── readme.md                         # 本文档
├── soccer-forum/                     # Spring Boot 后端
│   ├── pom.xml
│   ├── src/main/java/com/worldcup/forum/
│   └── src/main/resources/
│       ├── application.yml
│       └── sql/schema.sql
└── soccer-forum-frontend/            # Vue 3 前端（待开发）
    ├── package.json
    └── src/
```

我准备设计一款极简的网页，用来应对即将到来的世界杯，就一个赛前赛后讨论世界杯的论坛，里面可以自由发帖，自由发言那种，我准备设计一个前端和一个后端，后端有管理员，前端渲染比赛
一、总体架构

用户浏览器

&#x20;   ↓

Nginx (代理 + 静态文件)

&#x20;   ↓

Spring Boot API (端口 8080)

&#x20;   ↓

MySQL (数据存储) + Redis (限流/缓存)

二、前端架构 (Vue 3)

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

com.worldcup.forum/

├── controller/           # 控制器

│   ├── PostController

│   ├── ReplyController

│   └── AdminController

├── service/              # 业务逻辑

│   ├── PostService

│   ├── ReplyService

│   └── RateLimitService

├── mapper/               # MyBatis-Plus

│   ├── PostMapper

│   └── ReplyMapper

├── entity/               # 实体类

│   ├── Post

│   └── Reply

├── dto/                  # 数据传输对象

│   ├── request/

│   └── response/

├── config/               # 配置类

│   ├── WebConfig (CORS)

│   └── RedisConfig

├── common/               # 通用类

│   ├── Result (统一返回)

│   └── GlobalExceptionHandler

└── utils/                # 工具类

&#x20;   ├── IpUtils

&#x20;   └── SensitiveWordUtils

API 设计

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

text

前端提交 → 后端接收 → IP限流检查 → 敏感词过滤 → 存入MySQL → 返回成功

帖子列表加载

text

前端请求 → 分页查询帖子 → 批量查询回复（每帖最新5条） → 组装VO → 返回

管理员删除

text

前端携带Token → 验证Token → 软删除 → 记录日志 → 返回成功

六、部署架构

text

Nginx :80

├── / → /var/www/forum/dist (前端静态文件)

└── /api → http://localhost:8080 (后端代理)



Spring Boot :8080 → MySQL :3306 + Redis :6379

七、技术栈总结

层级	技术

前端	Vue 3 + Vite + Pinia + Axios

后端	Spring Boot 3.x + MyBatis-Plus

数据库	MySQL 8.0

缓存/限流	Redis

部署	Nginx + Jar包


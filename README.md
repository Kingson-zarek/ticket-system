# 客服工单系统

面向售前与售后协作的工单流转系统。售前提单、售后处理、状态留痕，把「客户问题靠聊天窗口转发」变成有编号、有责任人、有状态的一条流水线。

## 项目背景

售后岗真实的一天是这样的：售前在群里贴一段客户描述，售后在几十个会话里翻，问一句「哪个客户、什么环境、怎么复现」，等半天，问题没解决先把聊天记录刷走了。

痛点是三件事：

- 信息不全，提单全靠口头，缺现象、缺复现步骤、缺影响范围
- 责任不清，谁在处理全靠记，换个班就断线
- 状态不透明，售前追问进度，售后得回去翻聊天记录

这个项目用一张工单表把三件事钉住：提单必须填现象和复现步骤，处理人建单时就要指定，状态只有四个且流转受服务端校验。

## 演示

按顺序走一遍就能看到完整闭环，五步约五分钟：

```text
1. sales01 登录，首页看板显示四个状态计数和平均处理时长
2. 进工单列表，分页，只看到自己创建的工单
3. 点「新建工单」，留空标题提交，看校验提示；补全后提交，跳详情
4. 退出登录，换 tech01，列表里只有指派给自己的工单
5. 打开那条新工单点「接单」，状态变处理中；再换回 sales01 看「退回处理中」「关闭」
```

第 5 步里 tech01 看不到改派和删除，是因为那不是他的权限，这也是要演示的部分。

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Spring Boot 3.5.9 · Java 17 |
| 持久层 | MyBatis-Plus 3.5.7（逻辑删除、分页插件） |
| 数据库 | MySQL 8 |
| 鉴权 | JWT（jjwt 0.12.5）· BCrypt |
| 前端 | Vue 3 · Vite · Element Plus · axios · vue-router |

前端只写业务组件，样式全部用 Element Plus 现成能力。

## 已实现功能

### 售前

- 登录，拿到 token
- 工单列表（分页，只看自己创建的）
- 建单：标题、问题描述、优先级、客户名称、选择处理人
- 改派处理人，自己创建且未关闭的工单可改
- 确认解决后关闭工单，或退回处理中
- 删除自己创建且未关闭的工单

### 售后

- 登录
- 工单列表（只看指派给自己的）
- 接单、标记解决
- 处理中直接关闭

### 公共

- 工单详情，只有创建人和处理人可见
- 评论，按时间升序，显示昵称和角色
- 首页看板：四个状态计数、今日新增、平均处理时长

## 技术设计说明

**逻辑删除而不是物理删除。** `t_ticket` 和 `t_comment` 带 `deleted` 字段，MyBatis-Plus 全局配置后所有查询自动带 `deleted = 0`。工单被删之后工单号仍需可追溯，物理删除会让编号断档。

**不用外键。** 关联全靠应用层字段和校验，改表结构时不用先解约束。代价是脏数据要靠代码兜，所以 `created_by`、`assignee_id` 落库前都过一遍存在性和状态校验。

**状态流转做成常量表而不是 if 串。** `TRANSITIONS` 是 `Map<当前状态, 允许的目标状态集合>`，非法跳转在进入业务判断前就被拒。新增状态只改这张表。

**鉴权信息走 ThreadLocal。** 拦截器解 token 后写 `UserContext`，Service 直接取当前用户，不用把 userId 从 Controller 一路透传。请求结束在 `afterCompletion` 里清掉，避免线程池串号。

**列表的昵称用批量查询。** 先收集本页所有 `created_by` 和 `assignee_id`，一次 `selectBatchIds` 拿昵称再回填，避免每条记录各查一次。

**工单号按月递增。** 前缀 `TK + yyyyMM`，取当月最大号加一，补四位。并发下可能撞号，当前量级没加锁。

**权限比较用 `equals` 不用 `==`。** id 是 `Long`，`==` 比的是引用，超过 127 就恒不相等。测试账号里特意留了一个 `id = 200` 的 `tech99` 做回归。

## 已知限制与后续规划

| 功能 | 未实现原因 | 后续设计思路 |
|---|---|---|
| RBAC 权限系统 | 当前仅两个角色，用两个 if 足够，引入完整 RBAC 收益低于成本 | 建 `t_role` / `t_permission` / `t_role_permission`，用 `@RequiresPermission` 注解 + AOP 做按钮级鉴权 |
| 管理员角色 | 前期无跨角色管理需求 | 增加 admin 角色；列表查询不加 user 过滤，其余复用现有 Service |
| 附件上传 | 需要对象存储，超出 MVP 范围 | 接 MinIO / 阿里云 OSS，库里只存 objectKey，前端直传 |
| 客户自助门户 | 需要外部用户体系与独立鉴权 | 拆成两个前端，或加 customer 角色 + 短信验证码登录 |
| 知识库沉淀 | 优先级低于核心闭环 | 把「相似工单」升级为知识库：解决方案可标记为标准答案，下次直接推荐 |
| 邮件 / 短信通知 | 依赖第三方通道与费用 | 通知消费者里替换 Channel 实现，按 type 分发 |
| 抢单模式 | 当前业务是售前指派 | 加乐观锁（version）或 Redis 分布式锁 |
| 工作量均衡分配 | 数据量不足以支撑策略 | 按售后「未关闭工单数」加权，指派时推荐最空闲的人 |
| 列表筛选 | 后端接口已支持状态、优先级、关键词，前端未接 | 列表页加筛选条，参数走 `params` |
| 相似工单 | 优先级低于核心闭环 | 按标题分词做 `LIKE` 匹配，建单防重复、详情查历史共用一个接口 |
| 状态变更通知 | 需要异步链路，`t_notify` 表已建但未接 | 接 RabbitMQ，状态变更发消息，消费者落通知表 |
| 统计趋势图 | 饼图之外都靠卡片够用 | ECharts 折线，按天聚合 `t_ticket` |

## 本地运行

### 环境

- JDK 17 及以上
- Maven 3.9
- MySQL 8
- Node.js 20 及以上

### 数据库

```bash
mysql -u root -p < docs/schema.sql
```

脚本会建库、建表并插入四个测试账号。数据源配置在 `backend/src/main/resources/application.yaml`，默认 `root` / `123456`，按自己环境改。

### 启动后端

```bash
cd backend
mvn -o spring-boot:run
```

服务在 `http://127.0.0.1:8080`。

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

若提示 `spawn EPERM`（受限环境里 Vite 探测网络驱动器会失败），在命令前加一行环境变量再跑：

```bash
NODE_OPTIONS="--require ../scripts/vite-exec-stub.cjs"
```

Windows PowerShell：

```powershell
$env:NODE_OPTIONS="--require ../scripts/vite-exec-stub.cjs"
```

浏览器打开 `http://localhost:5173`，`/api` 已代理到 8080。

### 测试账号

密码统一 `123456`。

| 账号 | 角色 | 说明 |
|---|---|---|
| sales01 | 售前 | 有工单，可演示建单和改派 |
| tech01 | 售后 | 有指派工单，可演示接单和解决 |
| tech99 | 售后 | id 为 200，用于 Long 比较回归 |
| admin | 售后 | 无工单 |

## 数据库设计

| 表 | 用途 |
|---|---|
| `t_user` | 用户，含角色和启用标记 |
| `t_ticket` | 工单，含状态、优先级、创建人、处理人、解决时间 |
| `t_comment` | 评论，挂工单 |
| `t_notify` | 通知，已建表未接入 |

`t_ticket.status` 取值 `OPEN` / `PROCESSING` / `RESOLVED` / `CLOSED`，合法流转：

```text
OPEN        -> PROCESSING
PROCESSING  -> RESOLVED / CLOSED
RESOLVED    -> PROCESSING / CLOSED
CLOSED      -> 终态
```

## 接口文档

统一返回体：

```json
{ "code": 0, "message": "success", "data": {} }
```

`code` 为 0 表示成功，401 未登录，403 无权限，4000 参数校验失败，4001 业务状态不允许，404 路径不存在。除登录外全部需要 `Authorization: Bearer <token>`。

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 登录，返回 token、昵称、角色 |
| GET | `/api/auth/me` | 当前登录用户 |
| GET | `/api/tickets` | 工单列表，分页，可带 status / priority / keyword |
| POST | `/api/tickets` | 建单，仅售前 |
| GET | `/api/tickets/{id}` | 工单详情，仅参与人 |
| PUT | `/api/tickets/{id}/assign` | 改派，仅创建人且未关闭 |
| PUT | `/api/tickets/{id}/status` | 状态流转，按角色和当前状态校验 |
| DELETE | `/api/tickets/{id}` | 删除，仅创建人且未关闭，逻辑删除 |
| GET | `/api/tickets/{ticketId}/comments` | 评论列表，时间升序 |
| POST | `/api/tickets/{ticketId}/comments` | 发表评论，仅参与人 |
| GET | `/api/users/aftersales` | 可选处理人列表 |
| GET | `/api/report/summary` | 看板统计 |

字段约束见 `backend/src/main/java/com/hnty/ticketsystem/dto/` 上的校验注解。

## 目录结构

```text
backend/src/main/java/com/hnty/ticketsystem/
  controller/  收参、调 service、包 Result
  service/     业务规则，权限和状态判断在这里
  mapper/      MyBatis-Plus Mapper
  entity/      表映射
  dto/         入参
  vo/          出参
  security/    JWT 拦截器与 UserContext
  config/      MyBatis-Plus 与 Web 配置
  util/        工单号生成等工具
  common/      统一返回体、异常、状态和角色常量

frontend/src/
  api/         axios 实例与按 controller 分的接口封装
  views/       页面
  layouts/     主布局
  router/      路由与守卫
```

## 说明

个人练习项目，非生产系统。数据来自本地造数，没有接入真实客户。


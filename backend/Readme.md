# 后端项目

## 项目结构说明

### 目录结构

```bash
src/main/java/com/itheima/
├── anno/                # 自定义注解目录
│   └── State.java      # 状态相关的自定义注解
├── config/             # 配置目录
│   ├── OkHttpConfiguration.java  # OkHttp客户端配置
│   └── WebConfig.java  # Web相关配置，包括拦截器等
├── controller/         # 控制器目录
│   ├── ChatController.java  # 聊天相关接口
│   └── UserController.java  # 用户相关接口
├── exception/          # 异常处理目录
│   └── GlobalExceptionHandler.java  # 全局异常处理器
├── interceptors/       # 拦截器目录
│   └── LoginInterceptor.java  # 登录拦截器
├── mapper/            # 数据访问层目录
│   ├── ChatMessageMapper.java  # 聊天消息数据访问
│   ├── ChatSessionMapper.java  # 聊天会话数据访问
│   └── UserMapper.java  # 用户数据访问
├── pojo/              # 实体类目录
│   ├── ChatMessage.java  # 聊天消息实体
│   ├── ChatSession.java  # 聊天会话实体
│   ├── Result.java    # 统一返回结果实体
│   └── User.java      # 用户实体
├── service/           # 服务层目录
│   ├── impl/         # 服务实现类
│   ├── ChatService.java  # 聊天服务接口
│   └── UserService.java  # 用户服务接口
├── utils/             # 工具类目录
│   ├── AliOssUtil.java  # 阿里云OSS工具
│   ├── HttpClientUtil.java  # HTTP客户端工具
│   ├── JwtUtil.java   # JWT工具
│   ├── Md5Util.java   # MD5加密工具
│   ├── PythonScriptExecutor.java  # Python脚本执行器
│   └── ThreadLocalUtil.java  # 线程本地存储工具
└── validation/        # 验证目录
    └── StateValidation.java  # 状态验证器
```

## 配置说明

### 项目配置

项目使用`Maven`构建。因为后端文件夹不在项目根目录，所以需要先进入后端文件夹，再使用IDEA自动查找`pom.xml`文件来构建。

### 数据库配置

在`application.yml`中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/go_ztc?characterEncoding=utf8&useUnicode=true&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### Redis配置

在`application.yml`中配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 运行说明

### 终端运行

```bash
java -jar target/big-event-1.0-SNAPSHOT.jar
```

### IDEA 运行

在IDEA中，选择`Run` -> `Edit Configurations`，然后选择`Application`，再选择`BigEventApplication`，然后点击`Run`。

### 注意事项

1. 确保MySQL服务已启动且可以访问
2. 确保Redis服务已启动且可以访问
3. 确保数据库`go_ztc`已创建
4. 确保数据库表使用utf8mb4字符集
5. 根据实际环境修改数据库和Redis配置

## 接口测试

接口测试文件写在`messagetest.http`文件中，现在增加了`auther_token`全局变量，可以实时获取最新的Token。测试其他接口信息前请先登录。

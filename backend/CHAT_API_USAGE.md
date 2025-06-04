# 聊天系统API使用说明

## 新增功能：指定文件和文件夹进行RAG检索

### 概述

现在聊天系统支持在发送消息时指定特定的文件和文件夹进行RAG检索，而不是默认使用用户文件夹下的所有文件。

### API接口

#### 1. 发送聊天消息（支持文件选择）

**接口**: `POST /chat/messages`

**参数**:
- `sessionId` (Query参数): 会话ID
- Request Body: `ChatMessageRequest`

**ChatMessageRequest结构**:
```json
{
    "content": "你的问题内容",
    "fileIds": [1, 2, 3],      // 可选：文件ID列表
    "folderIds": [4, 5]        // 可选：文件夹ID列表
}
```

**行为说明**:
1. 如果 `fileIds` 和 `folderIds` 都为空或null，则使用默认的 `user_{userId}` 路径
2. 如果指定了文件ID，会直接使用这些文件的路径
3. 如果指定了文件夹ID，会递归获取文件夹下所有文件（包括子文件夹）
4. 系统会验证文件和文件夹的归属权限，只有属于当前用户的文件才会被使用

**示例请求**:
```bash
curl -X POST "http://localhost:8080/chat/messages?sessionId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token" \
  -d '{
    "content": "请分析这些文档的内容",
    "fileIds": [1, 2, 3],
    "folderIds": [4]
  }'
```

#### 2. 获取用户文件夹结构

**接口**: `GET /chat/folders`

**描述**: 获取用户的文件夹和文件结构，用于前端界面选择

**响应示例**:
```json
{
    "code": 0,
    "message": "操作成功",
    "data": {
        "folders": [
            {
                "id": 1,
                "parentId": 0,
                "userId": 1,
                "folderName": "研究资料",
                "createTime": "2024-01-01T10:00:00",
                "updateTime": "2024-01-01T10:00:00"
            }
        ],
        "files": [
            {
                "id": 1,
                "userId": 1,
                "folderId": 0,
                "fileName": "重要文档.pdf",
                "uploadTime": "2024-01-01T10:00:00"
            }
        ]
    }
}
```

#### 3. 预览文档路径（调试接口）

**接口**: `POST /chat/preview-paths`

**描述**: 预览指定文件和文件夹最终会使用的文档路径列表

**Request Body**: 同 `ChatMessageRequest`（不需要content字段）

**响应示例**:
```json
{
    "code": 0,
    "message": "操作成功",
    "data": [
        "/upload/user_1/folder1/document1.pdf",
        "/upload/user_1/folder1/document2.txt",
        "/upload/user_1/folder2/subfolder/document3.md"
    ]
}
```

### 使用场景示例

#### 场景1：使用默认设置
```json
{
    "content": "什么是人工智能？"
}
```
- 系统将使用 `user_{userId}` 路径下的所有文件

#### 场景2：指定特定文件
```json
{
    "content": "分析这个PDF文档的内容",
    "fileIds": [1, 2]
}
```
- 只使用ID为1和2的文件进行检索

#### 场景3：指定文件夹
```json
{
    "content": "总结研究资料文件夹的内容",
    "folderIds": [3]
}
```
- 使用ID为3的文件夹下所有文件（递归包含子文件夹）

#### 场景4：混合使用
```json
{
    "content": "基于这些特定文件和文件夹回答问题",
    "fileIds": [1, 2],
    "folderIds": [3, 4]
}
```
- 同时使用指定的文件和文件夹

### 技术实现说明

1. **路径收集**: 系统会递归收集文件夹下的所有文件路径
2. **权限验证**: 只有属于当前用户的文件和文件夹才会被处理
3. **RAG集成**: 收集到的路径会发送给Python RAG后端进行处理
4. **路径格式**: 
   - 单个路径时发送字符串
   - 多个路径时发送字符串数组
   - 空路径时使用默认的 `user_{userId}`

### 前端集成建议

1. **文件选择器**: 调用 `/chat/folders` 获取文件结构
2. **路径预览**: 使用 `/chat/preview-paths` 让用户确认选择
3. **消息发送**: 将选中的文件和文件夹ID随消息一起发送

### 注意事项

1. 文件和文件夹ID必须属于当前登录用户
2. 如果指定的文件夹很大，可能会影响RAG检索性能
3. 建议在前端提供路径预览功能，让用户了解将使用哪些文件
4. 系统会自动过滤无权限访问的文件和文件夹 
# RAG系统 API 接口文档

## 概述

本文档描述了基于LangChain和通义千问的RAG（检索增强生成）系统的API接口。系统提供文档查询、对话聊天、文档搜索等功能。

**基础URL**: `http://localhost:5000`

**支持的文档格式**: `.txt`, `.md`, `.pdf`, `.docx`

---

## 接口列表

### 1. 健康检查接口

**接口**: `GET /health`

**描述**: 检查系统运行状态和配置信息

#### 请求参数
无

#### 响应格式
```json
{
    "status": "healthy",
    "timestamp": "2024-01-01T12:00:00.000Z",
    "documents_root_path": "/path/to/documents",
    "message": "RAG系统运行正常"
}
```

#### 响应字段说明
- `status`: 系统状态 ("healthy" | "error")
- `timestamp`: 响应时间戳 (ISO 8601格式)
- `documents_root_path`: 文档根目录路径
- `message`: 状态描述信息

#### 示例
```bash
curl -X GET http://localhost:5000/health
```

---

### 2. 文档问答接口

**接口**: `POST /query`

**描述**: 基于指定文档回答用户问题

#### 请求格式
```json
{
    "question": "用户问题",
    "document_path": "相对文档路径" // 或 ["路径1", "路径2", "路径3"],
    "top_k": 3
}
```

#### 请求字段说明
- `question` (必需): 用户提出的问题
- `document_path` (必需): 文档路径，支持以下格式：
  - 字符串：单个文档文件或文件夹的相对路径
  - 字符串数组：多个文档文件或文件夹的相对路径列表
  - 如果是文件夹，会递归检索该文件夹下的所有子文件夹和支持的文件
- `top_k` (可选): 检索的文档片段数量，默认为3

#### 响应格式
```json
{
    "question": "用户问题",
    "answer": "AI生成的回答",
    "sources": [
        {
            "content": "相关文档内容片段",
            "source": "文档文件路径"
        }
    ]
}
```

#### 响应字段说明
- `question`: 原始用户问题
- `answer`: AI生成的回答内容
- `sources`: 引用的源文档信息数组
  - `content`: 相关文档内容片段
  - `source`: 源文档的文件路径

#### 错误响应
```json
{
    "error": "错误描述信息"
}
```

#### 示例

**单个路径查询：**
```bash
curl -X POST http://localhost:5000/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是机器学习？",
    "document_path": "ai",
    "top_k": 3
  }'
```

**多个路径查询：**
```bash
curl -X POST http://localhost:5000/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是机器学习？",
    "document_path": ["ai", "tech/ml.txt", "research"],
    "top_k": 3
  }'
```

---

### 3. 对话聊天接口

**接口**: `POST /chat`

**描述**: 支持对话历史的文档聊天功能

#### 请求格式
```json
{
    "message": "用户消息",
    "document_path": "相对文档路径", // 或 ["路径1", "路径2", "路径3"]
    "conversation_id": "对话会话ID",
    "history": [
        {
            "question": "历史问题",
            "answer": "历史回答",
            "sources": [...],
            "timestamp": "2024-01-01T12:00:00.000Z"
        }
    ]
}
```

#### 请求字段说明
- `message` (必需): 用户发送的消息
- `document_path` (必需): 文档路径，支持以下格式：
  - 字符串：单个文档文件或文件夹的相对路径
  - 字符串数组：多个文档文件或文件夹的相对路径列表
  - 如果是文件夹，会递归检索该文件夹下的所有子文件夹和支持的文件
- `conversation_id` (可选): 对话会话标识符，默认为"default"
- `history` (可选): 之前的对话历史数组

#### 响应格式
```json
{
    "question": "用户消息",
    "answer": "AI生成的回答",
    "response": "AI生成的回答",
    "sources": [
        {
            "content": "相关文档内容片段",
            "source": "文档文件路径"
        }
    ],
    "updated_history": [
        {
            "question": "问题",
            "answer": "回答",
            "sources": [...],
            "timestamp": "2024-01-01T12:00:00.000Z"
        }
    ],
    "conversation_id": "对话会话ID"
}
```

#### 响应字段说明
- `question`: 原始用户消息
- `answer` / `response`: AI生成的回答（两个字段内容相同，为兼容性保留）
- `sources`: 引用的源文档信息
- `updated_history`: 更新后的对话历史
- `conversation_id`: 对话会话标识符

#### 示例

**单个路径对话：**
```bash
curl -X POST http://localhost:5000/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "请介绍深度学习",
    "document_path": "ai",
    "conversation_id": "session_001"
  }'
```

**多个路径对话：**
```bash
curl -X POST http://localhost:5000/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "请介绍深度学习",
    "document_path": ["ai", "research", "tech/neural_networks.pdf"],
    "conversation_id": "session_001"
  }'
```

---

### 4. 文档搜索接口

**接口**: `POST /search`

**描述**: 在指定文档中搜索相关内容片段

#### 请求格式
```json
{
    "query": "搜索关键词",
    "document_path": "相对文档路径", // 或 ["路径1", "路径2", "路径3"]
    "top_k": 5
}
```

#### 请求字段说明
- `query` (必需): 搜索查询内容
- `document_path` (必需): 文档路径，支持以下格式：
  - 字符串：单个文档文件或文件夹的相对路径
  - 字符串数组：多个文档文件或文件夹的相对路径列表
  - 如果是文件夹，会递归检索该文件夹下的所有子文件夹和支持的文件
- `top_k` (可选): 返回的搜索结果数量，默认为5

#### 响应格式
```json
{
    "query": "搜索关键词",
    "results": [
        {
            "rank": 1,
            "content": "匹配的文档内容片段...",
            "source": "文档文件路径",
            "score": 0.95
        }
    ],
    "total": 5
}
```

#### 响应字段说明
- `query`: 原始搜索查询
- `results`: 搜索结果数组
  - `rank`: 结果排名（从1开始）
  - `content`: 匹配的文档内容片段
  - `source`: 源文档文件路径
  - `score`: 相似度分数（0-1之间）
- `total`: 找到的结果总数

#### 示例

**单个路径搜索：**
```bash
curl -X POST http://localhost:5000/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "神经网络",
    "document_path": "ai",
    "top_k": 5
  }'
```

**多个路径搜索：**
```bash
curl -X POST http://localhost:5000/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "神经网络",
    "document_path": ["ai", "tech", "research/papers"],
    "top_k": 5
  }'
```

---

### 5. 获取对话历史接口

**接口**: `GET /conversations/{conversation_id}/history`

**描述**: 获取指定对话的历史记录

#### 路径参数
- `conversation_id`: 对话会话标识符

#### 响应格式
```json
{
    "conversation_id": "对话会话ID",
    "history": [
        {
            "question": "历史问题",
            "answer": "历史回答",
            "sources": [
                {
                    "content": "文档内容",
                    "source": "文档路径"
                }
            ],
            "timestamp": "2024-01-01T12:00:00.000Z"
        }
    ]
}
```

#### 示例
```bash
curl -X GET http://localhost:5000/conversations/session_001/history
```

---

### 6. 清除对话历史接口

**接口**: `POST /conversations/{conversation_id}/clear`

**描述**: 清除指定对话的历史记录

#### 路径参数
- `conversation_id`: 对话会话标识符

#### 响应格式
```json
{
    "message": "已清除对话 session_001 的历史记录"
}
```

#### 示例
```bash
curl -X POST http://localhost:5000/conversations/session_001/clear
```

---

### 7. 系统统计信息接口

**接口**: `GET /stats`

**描述**: 获取系统运行统计信息

#### 响应格式
```json
{
    "embedding_model": "模型路径或名称",
    "llm_model": "qwen-plus",
    "chunk_size": 500,
    "chunk_overlap": 100,
    "active_conversations": 2,
    "cached_vectorstores": 3,
    "cache_keys": ["user_1", "ai", "research"],
    "persistent_vectorstores": ["user_1", "ai", "test_docs_ai"]
}
```

#### 响应字段说明
- `embedding_model`: 使用的嵌入模型
- `llm_model`: 使用的大语言模型
- `chunk_size`: 文档分割的块大小
- `chunk_overlap`: 文档块重叠大小
- `active_conversations`: 当前活跃对话数量
- `cached_vectorstores`: 内存中缓存的向量存储数量
- `cache_keys`: 缓存的向量存储键名列表
- `persistent_vectorstores`: 磁盘上持久化的向量存储列表

#### 示例
```bash
curl -X GET http://localhost:5000/stats
```

---

## 向量存储管理接口

### 8. 列出向量存储接口

**接口**: `GET /vectorstores`

**描述**: 列出所有持久化到磁盘的向量存储

#### 请求参数
无

#### 响应格式
```json
{
    "status": "success",
    "vectorstores": [
        {
            "name": "user_1",
            "path": "/path/to/vector_stores/user_1",
            "metadata": {
                "document_path": "user_1",
                "document_count": 5,
                "chunk_count": 42,
                "created_at": 1703145600.0,
                "embedding_model": "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
            }
        }
    ],
    "total": 1
}
```

#### 响应字段说明
- `status`: 操作状态 ("success" | "error")
- `vectorstores`: 向量存储列表
  - `name`: 向量存储名称（基于文档路径生成）
  - `path`: 磁盘存储路径
  - `metadata`: 元数据信息
    - `document_path`: 原始文档路径
    - `document_count`: 处理的文档数量
    - `chunk_count`: 文本分块数量
    - `created_at`: 创建时间戳
    - `embedding_model`: 使用的嵌入模型
- `total`: 向量存储总数

#### 示例
```bash
curl -X GET http://localhost:5000/vectorstores
```

---

### 9. 构建向量存储接口

**接口**: `POST /vectorstores`

**描述**: 构建并保存向量存储到磁盘

#### 请求格式
```json
{
    "document_path": "相对文档路径",
    "force_rebuild": false
}
```

#### 请求字段说明
- `document_path` (必需): 文档路径，支持文件或文件夹
- `force_rebuild` (可选): 是否强制重新构建，默认为false

#### 响应格式

**成功创建：**
```json
{
    "status": "created",
    "message": "向量存储构建并保存成功: user_1",
    "path": "/path/to/vector_stores/user_1",
    "metadata": {
        "document_path": "user_1",
        "document_count": 5,
        "chunk_count": 42,
        "created_at": 1703145600.0,
        "embedding_model": "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    }
}
```

**已存在（未强制重建）：**
```json
{
    "status": "exists",
    "message": "向量存储已存在: user_1",
    "path": "/path/to/vector_stores/user_1"
}
```

**错误响应：**
```json
{
    "status": "error",
    "message": "保存向量存储失败: 错误详情"
}
```

#### HTTP状态码
- `201`: 成功创建新的向量存储
- `200`: 向量存储已存在或操作成功
- `400`: 请求参数错误
- `500`: 服务器内部错误

#### 示例

**构建新的向量存储：**
```bash
curl -X POST http://localhost:5000/vectorstores \
  -H "Content-Type: application/json" \
  -d '{
    "document_path": "user_123",
    "force_rebuild": false
  }'
```

**强制重新构建：**
```bash
curl -X POST http://localhost:5000/vectorstores \
  -H "Content-Type: application/json" \
  -d '{
    "document_path": "user_123",
    "force_rebuild": true
  }'
```

---

### 10. 重新构建向量存储接口

**接口**: `POST /vectorstores/rebuild`

**描述**: 强制重新构建指定的向量存储

#### 请求格式
```json
{
    "document_path": "相对文档路径"
}
```

#### 请求字段说明
- `document_path` (必需): 要重新构建的文档路径

#### 响应格式
```json
{
    "status": "created",
    "message": "向量存储构建并保存成功: user_1",
    "path": "/path/to/vector_stores/user_1",
    "metadata": {
        "document_path": "user_1",
        "document_count": 5,
        "chunk_count": 42,
        "created_at": 1703145600.0,
        "embedding_model": "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    }
}
```

#### 示例
```bash
curl -X POST http://localhost:5000/vectorstores/rebuild \
  -H "Content-Type: application/json" \
  -d '{
    "document_path": "user_123"
  }'
```

---

### 11. 清除向量存储缓存接口

**接口**: `POST /vectorstores/clear-cache`

**描述**: 清除内存中的向量存储缓存

#### 请求格式

**清除特定路径的缓存：**
```json
{
    "document_path": "相对文档路径"
}
```

**清除所有缓存：**
```json
{}
```

#### 请求字段说明
- `document_path` (可选): 要清除缓存的文档路径，如果不提供则清除所有缓存

#### 响应格式

**清除特定缓存：**
```jsonx
{
    "message": "已清除 user_1 的缓存"
}
```

**清除所有缓存：**
```json
{
    "message": "已清除所有缓存"
}
```

#### 示例

**清除特定路径缓存：**
```bash
curl -X POST http://localhost:5000/vectorstores/clear-cache \
  -H "Content-Type: application/json" \
  -d '{
    "document_path": "user_123"
  }'
```

**清除所有缓存：**
```bash
curl -X POST http://localhost:5000/vectorstores/clear-cache \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

## 错误处理

### HTTP状态码
- `200`: 请求成功
- `400`: 客户端请求错误（参数缺失或无效）
- `404`: 接口不存在
- `500`: 服务器内部错误

### 错误响应格式
```json
{
    "error": "详细错误描述信息"
}
```

### 常见错误
1. **参数缺失**: `缺少必需参数：question 和 document_path`
2. **路径无效**: `无效的文档路径`
3. **文档不存在**: `文档路径不存在: /path/to/document`
4. **API调用失败**: `查询失败: 具体错误信息`

---

## 使用示例

### Python客户端示例
```python
import requests

# 基础配置
base_url = "http://localhost:5000"
headers = {"Content-Type": "application/json"}

# 问答示例
def ask_question(question, doc_path):
    """
    doc_path 可以是字符串或列表
    - 字符串：单个路径 "ai"
    - 列表：多个路径 ["ai", "tech", "research"]
    """
    data = {
        "question": question,
        "document_path": doc_path,
        "top_k": 3
    }
    response = requests.post(f"{base_url}/query", json=data, headers=headers)
    return response.json()

# 对话示例
def chat_with_documents(message, doc_path, conversation_id="default"):
    """
    doc_path 可以是字符串或列表
    """
    data = {
        "message": message,
        "document_path": doc_path,
        "conversation_id": conversation_id
    }
    response = requests.post(f"{base_url}/chat", json=data, headers=headers)
    return response.json()

# 使用示例
# 单个路径
result1 = ask_question("什么是人工智能？", "ai")
print(f"回答: {result1['answer']}")

# 多个路径
result2 = ask_question("深度学习的应用有哪些？", ["ai", "research", "tech"])
print(f"回答: {result2['answer']}")
```

### JavaScript客户端示例
```javascript
// 问答请求 - 支持多路径
async function askQuestion(question, docPath) {
    // docPath 可以是字符串或数组
    const response = await fetch('http://localhost:5000/query', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            question: question,
            document_path: docPath,
            top_k: 3
        })
    });
    return await response.json();
}

// 使用示例
// 单个路径
askQuestion("什么是机器学习？", "ai")
    .then(result => console.log(result.answer));

// 多个路径
askQuestion("深度学习的应用？", ["ai", "research", "tech"])
    .then(result => console.log(result.answer));
```

---

## 配置说明

### 文档目录结构
```
documents_root/
├── ai/
│   ├── 机器学习基础.txt
│   └── 深度学习介绍.md
├── tech/
│   └── 云计算技术.pdf
├── research/
│   ├── papers/
│   │   └── 研究论文.pdf
│   └── reports/
└── other/
    └── 文档.docx
```

### 支持的路径格式
1. **单个文件**: `"tech/云计算技术.pdf"`
2. **单个文件夹**: `"ai"` (会递归检索所有子文件夹)
3. **多个路径**: `["ai", "tech", "research/papers"]`
4. **混合路径**: `["ai", "tech/云计算技术.pdf", "research"]`

### 环境变量
- `DASHSCOPE_API_KEY`: 通义千问API密钥
- `FLASK_HOST`: Flask服务主机地址（默认0.0.0.0）
- `FLASK_PORT`: Flask服务端口（默认5000）
- `FLASK_DEBUG`: 调试模式开关（默认True）

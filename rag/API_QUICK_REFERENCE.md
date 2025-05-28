# RAG系统 API 快速参考

**基础URL**: `http://localhost:5000`

## 接口概览

| 方法 | 接口 | 描述 |
|------|------|------|
| GET | `/health` | 健康检查 |
| POST | `/query` | 文档问答 |
| POST | `/chat` | 对话聊天 |
| POST | `/search` | 文档搜索 |
| GET | `/conversations/{id}/history` | 获取对话历史 |
| POST | `/conversations/{id}/clear` | 清除对话历史 |
| GET | `/stats` | 系统统计 |

## 核心接口示例

### 1. 问答接口
```bash
# 单个路径请求
POST /query
{
    "question": "什么是机器学习？",
    "document_path": "ai",
    "top_k": 3
}

# 多个路径请求
POST /query
{
    "question": "什么是机器学习？",
    "document_path": ["ai", "tech", "research"],
    "top_k": 3
}

# 响应
{
    "question": "什么是机器学习？",
    "answer": "机器学习是...",
    "sources": [
        {
            "content": "相关内容...",
            "source": "文档路径"
        }
    ]
}
```

### 2. 对话接口
```bash
# 单个路径请求
POST /chat
{
    "message": "请介绍深度学习",
    "document_path": "ai",
    "conversation_id": "session_001"
}

# 多个路径请求
POST /chat
{
    "message": "请介绍深度学习",
    "document_path": ["ai", "research", "tech"],
    "conversation_id": "session_001"
}

# 响应
{
    "question": "请介绍深度学习",
    "answer": "深度学习是...",
    "response": "深度学习是...",
    "sources": [...],
    "updated_history": [...],
    "conversation_id": "session_001"
}
```

### 3. 搜索接口
```bash
# 单个路径请求
POST /search
{
    "query": "神经网络",
    "document_path": "ai",
    "top_k": 5
}

# 多个路径请求
POST /search
{
    "query": "神经网络",
    "document_path": ["ai", "tech", "research"],
    "top_k": 5
}

# 响应
{
    "query": "神经网络",
    "results": [
        {
            "rank": 1,
            "content": "匹配内容...",
            "source": "文档路径",
            "score": 0.95
        }
    ],
    "total": 5
}
```

## Python 客户端示例

```python
import requests

class RAGClient:
    def __init__(self, base_url="http://localhost:5000"):
        self.base_url = base_url
    
    def ask(self, question, doc_path="ai"):
        """
        doc_path 可以是字符串或列表
        - 字符串：单个路径
        - 列表：多个路径
        """
        response = requests.post(f"{self.base_url}/query", json={
            "question": question,
            "document_path": doc_path
        })
        return response.json()
    
    def chat(self, message, doc_path="ai", conversation_id="default"):
        """
        doc_path 可以是字符串或列表
        """
        response = requests.post(f"{self.base_url}/chat", json={
            "message": message,
            "document_path": doc_path,
            "conversation_id": conversation_id
        })
        return response.json()
    
    def search(self, query, doc_path="ai", top_k=5):
        """
        doc_path 可以是字符串或列表
        """
        response = requests.post(f"{self.base_url}/search", json={
            "query": query,
            "document_path": doc_path,
            "top_k": top_k
        })
        return response.json()

# 使用示例
client = RAGClient()

# 单个路径
result1 = client.ask("什么是人工智能？", "ai")
print(result1["answer"])

# 多个路径
result2 = client.ask("深度学习应用？", ["ai", "research", "tech"])
print(result2["answer"])
```

## 错误处理

所有错误响应格式：
```json
{
    "error": "错误描述信息"
}
```

常见状态码：
- `200`: 成功
- `400`: 参数错误
- `500`: 服务器错误

## 注意事项

1. **文档路径支持**：
   - 单个路径：字符串格式 `"ai"`
   - 多个路径：数组格式 `["ai", "tech", "research"]`
   - 混合类型：`["ai", "tech/file.pdf", "research"]`
2. **文件夹递归**：文件夹路径会自动递归检索所有子文件夹
3. **路径安全**：系统自动验证路径，跳过无效路径
4. **支持格式**：`.txt`, `.md`, `.pdf`, `.docx`
5. **对话历史**：存储在内存中，重启后丢失
6. **文件编码**：使用UTF-8编码的文档文件 
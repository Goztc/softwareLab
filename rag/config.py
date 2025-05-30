import os
from pathlib import Path

# 基础路径配置
BASE_DIR = Path(__file__).parent
MODEL_DIR = BASE_DIR / "all-MiniLM-L6-v2"
VECTOR_STORE_PATH = BASE_DIR / "vector_store"

# 文档根目录配置 - 用于动态检索文档的绝对路径前缀
DOCUMENTS_ROOT_PATH = Path("E:/files")  # 修改为您的文档根路径

# 创建必要的目录
VECTOR_STORE_PATH.mkdir(exist_ok=True)

# 模型配置
EMBEDDING_MODEL_PATH = str(MODEL_DIR)
# 如果本地模型存在，使用本地路径；否则使用在线模型名称
if MODEL_DIR.exists():
    EMBEDDING_MODEL_NAME = str(MODEL_DIR)  # 使用本地路径
    print(f"✅ 使用本地embedding模型: {EMBEDDING_MODEL_NAME}")
else:
    EMBEDDING_MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"  # 使用在线模型
    print(f"⚠️  本地模型不存在，将尝试下载: {EMBEDDING_MODEL_NAME}")
    print(f"   本地模型路径: {MODEL_DIR}")

# QwenAPI配置
QWEN_API_KEY = os.getenv("DASHSCOPE_API_KEY", "sk-1a7fdb5c23f448608ba92d299e043ef5")
QWEN_BASE_URL = "https://dashscope-intl.aliyuncs.com/compatible-mode/v1"
QWEN_MODEL_NAME = "qwen-plus"

# Flask配置
FLASK_HOST = "0.0.0.0"
FLASK_PORT = 5000
FLASK_DEBUG = True

# RAG配置
CHUNK_SIZE = 500
CHUNK_OVERLAP = 50
TOP_K_RETRIEVAL = 5
MAX_CONTEXT_LENGTH = 3000

# 向量存储配置
VECTOR_DIMENSION = 384  # all-MiniLM-L6-v2的向量维度

# 日志配置
LOG_LEVEL = "INFO"
LOG_FORMAT = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"

# 支持的文档格式
SUPPORTED_EXTENSIONS = [".txt", ".md", ".pdf", ".docx"]

# 生成配置
GENERATION_CONFIG = {
    "temperature": 0.7,
    "max_tokens": 1024,
    "top_p": 0.9,
    "stream": False
}

# RAG流水线启动配置
STARTUP_INIT_RAG = os.getenv("STARTUP_INIT_RAG", "false").lower() == "true"  # 是否在启动时初始化RAG流水线
WARMUP_ON_STARTUP = os.getenv("WARMUP_ON_STARTUP", "true").lower() == "true"  # 是否在启动时进行预热 
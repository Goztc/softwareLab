#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
RAG系统服务启动脚本
"""

import os
import sys
import logging
from pathlib import Path

# 添加项目根目录到Python路径
current_dir = Path(__file__).parent
sys.path.insert(0, str(current_dir))

# 导入应用
from app import app
import config

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

def check_environment():
    """检查运行环境"""
    print("🔍 检查运行环境...")
    
    # 检查文档根目录
    if not config.DOCUMENTS_ROOT_PATH.exists():
        print(f"⚠️  文档根目录不存在: {config.DOCUMENTS_ROOT_PATH}")
        config.DOCUMENTS_ROOT_PATH.mkdir(parents=True, exist_ok=True)
        print(f"✅ 已创建文档根目录: {config.DOCUMENTS_ROOT_PATH}")
    
    # 检查API密钥
    if config.QWEN_API_KEY == "sk-1a7fdb5c23f448608ba92d299e043ef5":
        print("⚠️  使用默认API密钥，请设置您的真实DASHSCOPE_API_KEY")
        print("   获取API密钥：https://help.aliyun.com/zh/dashscope/developer-reference/api-details")
    
    print("✅ 环境检查完成")

def main():
    """主函数"""
    try:
        print("🚀 启动RAG系统服务...")
        
        # 检查环境
        check_environment()
        
        print("🔧 初始化RAG流水线...")
        # RAG流水线将在第一次请求时自动初始化
        
        print("🌐 启动Flask服务器...")
        print(f"📍 服务地址: http://{config.FLASK_HOST}:{config.FLASK_PORT}")
        print("📚 API文档:")
        print("   POST /query      - 问答查询")
        print("   POST /chat       - 对话聊天") 
        print("   POST /search     - 文档搜索")
        print("   GET  /health     - 健康检查")
        print("=" * 50)
        
        # 启动Flask应用
        app.run(
            host=config.FLASK_HOST,
            port=config.FLASK_PORT,
            debug=config.FLASK_DEBUG,
            use_reloader=False  # 避免重复初始化
        )
        
    except KeyboardInterrupt:
        print("\n🛑 用户终止服务")
    except Exception as e:
        logger.error(f"启动失败: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main() 
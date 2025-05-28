 #!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
RAG系统修复验证脚本
"""

import os
import sys
from pathlib import Path

# 添加项目根目录到Python路径
current_dir = Path(__file__).parent
sys.path.insert(0, str(current_dir))

import config
from core.rag_pipeline import RAGPipeline

def test_rag_pipeline():
    """测试RAG流水线"""
    try:
        print("🧪 开始测试RAG流水线...")
        
        # 初始化pipeline
        pipeline = RAGPipeline()
        
        # 测试stats
        print("📊 测试统计信息...")
        stats = pipeline.get_stats()
        print(f"Stats: {stats}")
        
        # 测试文档加载
        print("📄 测试文档加载...")
        test_doc_path = os.path.join(config.DOCUMENTS_ROOT_PATH, "ai")
        
        if os.path.exists(test_doc_path):
            docs = pipeline._load_documents_from_path(test_doc_path)
            print(f"成功加载 {len(docs)} 个文档")
            
            if docs:
                # 测试查询
                print("❓ 测试查询功能...")
                result = pipeline.query("什么是机器学习？", test_doc_path)
                print(f"查询结果: {result['answer'][:100]}...")
                
        print("✅ 测试完成")
        
    except Exception as e:
        print(f"❌ 测试失败: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_rag_pipeline()
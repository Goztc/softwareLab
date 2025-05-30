#!/usr/bin/env python3
"""
测试对话历史功能
"""

import sys
import os
sys.path.append(os.path.dirname(__file__))

from core.rag_pipeline import RAGPipeline

def test_conversation_history():
    """测试对话历史功能"""
    print("🧪 测试对话历史功能")
    print("=" * 50)
    
    try:
        # 初始化RAG Pipeline
        rag = RAGPipeline()
        print("✅ RAG Pipeline 初始化成功")
        
        # 测试目录
        test_path = "test_docs/ai"
        
        if not os.path.exists(test_path):
            print(f"⚠️  测试目录 {test_path} 不存在，跳过测试")
            return
        
        conversation_id = "test_conversation"
        
        # 第一轮对话
        print("\n💬 第一轮对话...")
        result1 = rag.chat("记住，我喜欢猫", test_path, conversation_id=conversation_id)
        print(f"问题1: {result1['question']}")
        print(f"回答1: {result1['answer'][:100]}...")
        print(f"对话历史长度: {len(result1['updated_history'])}")
        
        # 第二轮对话
        print("\n💬 第二轮对话...")
        result2 = rag.chat("我喜欢什么？", test_path, 
                          history=result1['updated_history'], conversation_id=conversation_id)
        print(f"问题2: {result2['question']}")
        print(f"回答2: {result2['answer'][:100]}...")
        print(f"对话历史长度: {len(result2['updated_history'])}")
        
        # 第三轮对话
        print("\n💬 第三轮对话...")
        result3 = rag.chat("请举个例子说明", test_path, 
                          history=result2['updated_history'], conversation_id=conversation_id)
        print(f"问题3: {result3['question']}")
        print(f"回答3: {result3['answer'][:100]}...")
        print(f"对话历史长度: {len(result3['updated_history'])}")
        
        # 测试获取对话历史
        print("\n📚 测试获取对话历史...")
        history = rag.get_conversation_history(conversation_id)
        print(f"保存的对话历史长度: {len(history)}")
        
        for i, exchange in enumerate(history):
            print(f"  对话 {i+1}:")
            print(f"    问题: {exchange['question']}")
            print(f"    回答: {exchange['answer'][:50]}...")
            print(f"    时间: {exchange['timestamp']}")
        
        # 测试清除对话历史
        print("\n🗑️  测试清除对话历史...")
        rag.clear_conversation_history(conversation_id)
        history_after_clear = rag.get_conversation_history(conversation_id)
        print(f"清除后的对话历史长度: {len(history_after_clear)}")
        
        print("\n✅ 对话历史功能测试完成！")
        
    except Exception as e:
        print(f"❌ 测试过程中发生错误: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_conversation_history() 
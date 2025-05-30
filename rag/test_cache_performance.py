#!/usr/bin/env python3
"""
测试RAG缓存性能
"""

import time
import os
import sys
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from core.rag_pipeline import RAGPipeline

def test_cache_performance():
    """测试缓存性能改进"""
    print("🧪 开始测试RAG缓存性能...")
    print("=" * 60)
    
    # 创建RAG实例
    rag = RAGPipeline()
    
    # 测试用的文档路径
    test_path = "test_docs/ai"
    
    # 测试问题
    questions = [
        "什么是机器学习？",
        "深度学习有什么应用？",
        "人工智能的发展历史如何？"
    ]
    
    print("📊 第一轮查询（会构建向量存储）:")
    print("-" * 40)
    
    first_round_times = []
    for i, question in enumerate(questions, 1):
        start_time = time.time()
        result = rag.query(question, test_path)
        end_time = time.time()
        
        duration = end_time - start_time
        first_round_times.append(duration)
        
        print(f"问题 {i}: {question}")
        print(f"耗时: {duration:.2f}秒")
        print(f"回答: {result['answer'][:100]}...")
        print()
    
    print("🚀 第二轮查询（使用缓存）:")
    print("-" * 40)
    
    second_round_times = []
    for i, question in enumerate(questions, 1):
        start_time = time.time()
        result = rag.query(question, test_path)
        end_time = time.time()
        
        duration = end_time - start_time
        second_round_times.append(duration)
        
        print(f"问题 {i}: {question}")
        print(f"耗时: {duration:.2f}秒")
        print(f"回答: {result['answer'][:100]}...")
        print()
    
    # 性能对比
    print("📈 性能对比:")
    print("-" * 40)
    
    avg_first = sum(first_round_times) / len(first_round_times)
    avg_second = sum(second_round_times) / len(second_round_times)
    improvement = ((avg_first - avg_second) / avg_first) * 100
    
    print(f"第一轮平均耗时: {avg_first:.2f}秒")
    print(f"第二轮平均耗时: {avg_second:.2f}秒")
    print(f"性能提升: {improvement:.1f}%")
    
    # 显示缓存状态
    stats = rag.get_stats()
    print("\n📊 缓存状态:")
    print("-" * 40)
    print(f"缓存的向量存储数量: {stats['cached_vectorstores']}")
    print(f"缓存的路径: {stats['cache_keys']}")
    
    # 测试缓存清除
    print("\n🧹 测试缓存清除:")
    print("-" * 40)
    rag.clear_cache(test_path)
    stats_after_clear = rag.get_stats()
    print(f"清除后缓存数量: {stats_after_clear['cached_vectorstores']}")
    
    print("\n✅ 缓存性能测试完成！")

if __name__ == "__main__":
    test_cache_performance() 
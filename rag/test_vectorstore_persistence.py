#!/usr/bin/env python3
"""
测试向量存储持久化功能
"""

import os
import sys
import time
import requests
import json
from pathlib import Path

# 添加项目路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from core.rag_pipeline import RAGPipeline

def test_vectorstore_persistence():
    """测试向量存储持久化功能"""
    print("🧪 开始测试向量存储持久化功能...")
    print("=" * 60)
    
    # 创建RAG实例
    rag = RAGPipeline()
    
    # 测试用的文档路径
    test_path = "test_docs/ai"
    
    print("📊 1. 测试构建并保存向量存储")
    print("-" * 40)
    
    # 构建并保存向量存储
    start_time = time.time()
    result = rag.save_vectorstore(test_path, force_rebuild=True)
    build_time = time.time() - start_time
    
    print(f"构建结果: {result['status']}")
    print(f"消息: {result['message']}")
    print(f"构建耗时: {build_time:.2f}秒")
    print()
    
    print("📂 2. 测试列出持久化的向量存储")
    print("-" * 40)
    
    # 列出所有向量存储
    vectorstores = rag.list_vectorstores()
    print(f"状态: {vectorstores['status']}")
    print(f"向量存储数量: {vectorstores['total']}")
    
    if vectorstores['vectorstores']:
        for vs in vectorstores['vectorstores']:
            print(f"  - 名称: {vs['name']}")
            if vs['metadata']:
                print(f"    文档数量: {vs['metadata'].get('document_count', 'Unknown')}")
                print(f"    文本块数量: {vs['metadata'].get('chunk_count', 'Unknown')}")
    print()
    
    print("🔄 3. 测试清除缓存后从磁盘加载")
    print("-" * 40)
    
    # 清除内存缓存
    rag.clear_cache()
    print("已清除内存缓存")
    
    # 测试查询（应该从磁盘加载）
    start_time = time.time()
    query_result = rag.query("什么是机器学习？", test_path)
    load_time = time.time() - start_time
    
    print(f"查询耗时: {load_time:.2f}秒")
    print(f"回答: {query_result['answer'][:100]}...")
    print()
    
    print("⚡ 4. 测试二次查询（使用缓存）")
    print("-" * 40)
    
    # 第二次查询（应该使用缓存）
    start_time = time.time()
    query_result2 = rag.query("深度学习有什么应用？", test_path)
    cache_time = time.time() - start_time
    
    print(f"查询耗时: {cache_time:.2f}秒")
    print(f"回答: {query_result2['answer'][:100]}...")
    print()
    
    print("📈 5. 性能对比")
    print("-" * 40)
    print(f"首次构建耗时: {build_time:.2f}秒")
    print(f"从磁盘加载耗时: {load_time:.2f}秒")
    print(f"缓存查询耗时: {cache_time:.2f}秒")
    
    speedup_load = build_time / load_time if load_time > 0 else 0
    speedup_cache = load_time / cache_time if cache_time > 0 else 0
    
    print(f"磁盘加载性能提升: {speedup_load:.1f}x")
    print(f"缓存性能提升: {speedup_cache:.1f}x")
    print()
    
    print("📊 6. 显示统计信息")
    print("-" * 40)
    stats = rag.get_stats()
    print(f"缓存的向量存储: {stats['cached_vectorstores']}")
    print(f"持久化的向量存储: {len(stats['persistent_vectorstores'])}")
    print(f"缓存键: {stats['cache_keys']}")
    print()
    
    print("✅ 向量存储持久化功能测试完成！")
    
    return {
        'build_time': build_time,
        'load_time': load_time,
        'cache_time': cache_time,
        'speedup_load': speedup_load,
        'speedup_cache': speedup_cache
    }

def test_api_endpoints():
    """测试API端点"""
    print("\n🌐 测试API端点...")
    print("=" * 60)
    
    base_url = "http://localhost:5000"
    test_path = "ai"
    
    try:
        # 测试健康检查
        print("1. 测试健康检查")
        response = requests.get(f"{base_url}/health")
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")
        print()
        
        # 测试构建向量存储
        print("2. 测试构建向量存储API")
        payload = {
            "document_path": test_path,
            "force_rebuild": True
        }
        response = requests.post(f"{base_url}/vectorstores", json=payload)
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")
        print()
        
        # 测试列出向量存储
        print("3. 测试列出向量存储API")
        response = requests.get(f"{base_url}/vectorstores")
        print(f"状态码: {response.status_code}")
        result = response.json()
        print(f"向量存储数量: {result.get('total', 0)}")
        print()
        
        # 测试清除缓存
        print("4. 测试清除缓存API")
        payload = {"document_path": test_path}
        response = requests.post(f"{base_url}/vectorstores/clear-cache", json=payload)
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")
        print()
        
        # 测试查询（验证从磁盘加载）
        print("5. 测试查询API（从磁盘加载）")
        payload = {
            "question": "什么是人工智能？",
            "document_path": test_path
        }
        response = requests.post(f"{base_url}/query", json=payload)
        print(f"状态码: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"回答: {result['answer'][:100]}...")
        print()
        
        print("✅ API端点测试完成！")
        
    except requests.exceptions.ConnectionError:
        print("❌ 无法连接到Flask应用，请确保应用正在运行")
    except Exception as e:
        print(f"❌ API测试失败: {e}")

if __name__ == "__main__":
    # 测试核心功能
    performance = test_vectorstore_persistence()
    
    # 测试API（可选）
    print("\n是否测试API端点？(需要Flask应用运行) [y/N]: ", end="")
    choice = input().strip().lower()
    if choice in ['y', 'yes']:
        test_api_endpoints()
    
    print(f"\n🎯 测试总结:")
    print(f"构建时间: {performance['build_time']:.2f}秒")
    print(f"加载时间: {performance['load_time']:.2f}秒") 
    print(f"缓存时间: {performance['cache_time']:.2f}秒")
    print(f"总体性能提升: {performance['speedup_load']:.1f}x (构建 vs 磁盘加载)") 
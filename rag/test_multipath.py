#!/usr/bin/env python3
"""
多路径功能测试脚本
测试RAG系统是否能正确处理多个文档路径
"""

import os
import json
import requests
import time
from pathlib import Path

# 配置
BASE_URL = "http://localhost:5000"
HEADERS = {"Content-Type": "application/json"}

def test_health():
    """测试健康检查接口"""
    print("🔍 测试健康检查...")
    try:
        response = requests.get(f"{BASE_URL}/health")
        if response.status_code == 200:
            data = response.json()
            print(f"✅ 系统状态: {data['status']}")
            print(f"📁 文档根路径: {data['documents_root_path']}")
            return True
        else:
            print(f"❌ 健康检查失败: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ 连接失败: {e}")
        return False

def test_single_path():
    """测试单个路径查询"""
    print("\n🔍 测试单个路径查询...")
    try:
        data = {
            "question": "什么是人工智能？",
            "document_path": "ai",
            "top_k": 2
        }
        response = requests.post(f"{BASE_URL}/query", json=data, headers=HEADERS)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ 单路径查询成功")
            print(f"📄 找到文档源: {len(result.get('sources', []))} 个")
            return True
        else:
            print(f"❌ 单路径查询失败: {response.status_code}")
            print(f"错误信息: {response.text}")
            return False
    except Exception as e:
        print(f"❌ 单路径查询异常: {e}")
        return False

def test_multiple_paths():
    """测试多个路径查询"""
    print("\n🔍 测试多个路径查询...")
    try:
        data = {
            "question": "请介绍技术发展趋势",
            "document_path": ["ai", "tech", "docs"],  # 多个路径
            "top_k": 3
        }
        response = requests.post(f"{BASE_URL}/query", json=data, headers=HEADERS)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ 多路径查询成功")
            print(f"📄 找到文档源: {len(result.get('sources', []))} 个")
            # 打印部分结果
            if result.get('sources'):
                print("📋 文档来源:")
                for i, source in enumerate(result['sources'][:2]):
                    print(f"  {i+1}. {source.get('source', 'Unknown')}")
            return True
        else:
            print(f"❌ 多路径查询失败: {response.status_code}")
            print(f"错误信息: {response.text}")
            return False
    except Exception as e:
        print(f"❌ 多路径查询异常: {e}")
        return False

def test_chat_multiple_paths():
    """测试多路径对话"""
    print("\n🔍 测试多路径对话...")
    try:
        data = {
            "message": "请比较不同技术的优缺点",
            "document_path": ["ai", "tech"],
            "conversation_id": "multipath_test"
        }
        response = requests.post(f"{BASE_URL}/chat", json=data, headers=HEADERS)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ 多路径对话成功")
            print(f"🤖 AI回答: {result.get('answer', '')[:100]}...")
            return True
        else:
            print(f"❌ 多路径对话失败: {response.status_code}")
            print(f"错误信息: {response.text}")
            return False
    except Exception as e:
        print(f"❌ 多路径对话异常: {e}")
        return False

def test_search_multiple_paths():
    """测试多路径搜索"""
    print("\n🔍 测试多路径搜索...")
    try:
        data = {
            "query": "技术",
            "document_path": ["ai", "tech", "docs"],
            "top_k": 3
        }
        response = requests.post(f"{BASE_URL}/search", json=data, headers=HEADERS)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ 多路径搜索成功")
            print(f"🔍 找到结果: {result.get('total', 0)} 个")
            return True
        else:
            print(f"❌ 多路径搜索失败: {response.status_code}")
            print(f"错误信息: {response.text}")
            return False
    except Exception as e:
        print(f"❌ 多路径搜索异常: {e}")
        return False

def test_invalid_paths():
    """测试无效路径处理"""
    print("\n🔍 测试无效路径处理...")
    try:
        data = {
            "question": "测试问题",
            "document_path": ["nonexistent1", "nonexistent2"],
            "top_k": 2
        }
        response = requests.post(f"{BASE_URL}/query", json=data, headers=HEADERS)
        
        if response.status_code == 400:
            print("✅ 正确处理了无效路径（返回400错误）")
            return True
        elif response.status_code == 200:
            result = response.json()
            if "没有找到相关文档" in result.get('answer', ''):
                print("✅ 正确处理了无效路径（返回无文档消息）")
                return True
        
        print(f"⚠️  无效路径处理可能有问题: {response.status_code}")
        print(f"响应: {response.text}")
        return False
    except Exception as e:
        print(f"❌ 无效路径测试异常: {e}")
        return False

def main():
    """主测试函数"""
    print("🚀 开始多路径功能测试")
    print("=" * 50)
    
    tests = [
        ("健康检查", test_health),
        ("单个路径查询", test_single_path),
        ("多个路径查询", test_multiple_paths),
        ("多路径对话", test_chat_multiple_paths),
        ("多路径搜索", test_search_multiple_paths),
        ("无效路径处理", test_invalid_paths),
    ]
    
    results = []
    for test_name, test_func in tests:
        try:
            result = test_func()
            results.append((test_name, result))
            time.sleep(1)  # 避免请求过快
        except Exception as e:
            print(f"❌ {test_name} 测试出现异常: {e}")
            results.append((test_name, False))
    
    # 总结结果
    print("\n" + "=" * 50)
    print("📊 测试结果总结:")
    
    passed = 0
    for test_name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"  {test_name}: {status}")
        if result:
            passed += 1
    
    print(f"\n🎯 测试通过率: {passed}/{len(results)} ({passed/len(results)*100:.1f}%)")
    
    if passed == len(results):
        print("🎉 所有测试通过！多路径功能正常工作")
        return True
    else:
        print("⚠️  部分测试失败，请检查系统配置")
        return False

if __name__ == "__main__":
    main() 
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
RAG系统测试客户端 - LangChain版本
"""

import requests

class RAGTestClient:
    def __init__(self, base_url="http://localhost:5000"):
        self.base_url = base_url
        self.session = requests.Session()
    
    def test_health(self):
        """测试健康检查"""
        print("🔍 测试健康检查...")
        try:
            response = self.session.get(f"{self.base_url}/health")
            if response.status_code == 200:
                result = response.json()
                print(f"✅ 服务状态: {result['status']}")
                print(f"📁 文档根目录: {result.get('documents_root_path', 'N/A')}")
                return True
            else:
                print(f"❌ 健康检查失败: {response.status_code}")
                return False
        except Exception as e:
            print(f"❌ 无法连接到服务: {str(e)}")
            return False
    
    def test_query(self, question, document_path, conversation_id="test"):
        """测试问答功能"""
        print(f"❓ 测试问答: {question}")
        print(f"📂 文档路径: {document_path}")
        try:
            data = {
                "question": question,
                "document_path": document_path,
                "conversation_id": conversation_id
            }
            response = self.session.post(f"{self.base_url}/query", json=data)
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ 回答: {result['answer'][:200]}...")
                print(f"   来源数量: {len(result.get('sources', []))}")
                if result.get('sources'):
                    print("   主要来源:")
                    for i, source in enumerate(result['sources'][:2]):
                        print(f"   {i+1}. {source['content'][:100]}...")
                return result
            else:
                print(f"❌ 问答失败: {response.text}")
                return None
        except Exception as e:
            print(f"❌ 问答异常: {str(e)}")
            return None
    
    def test_chat(self, message, document_path, conversation_id="test", history=None):
        """测试对话功能"""
        print(f"💬 测试对话: {message}")
        print(f"📂 文档路径: {document_path}")
        try:
            data = {
                "message": message,
                "document_path": document_path,
                "conversation_id": conversation_id,
                "history": history or []
            }
            response = self.session.post(f"{self.base_url}/chat", json=data)
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ 回复: {result['response'][:200]}...")
                print(f"   对话历史长度: {len(result.get('updated_history', []))}")
                return result
            else:
                print(f"❌ 对话失败: {response.text}")
                return None
        except Exception as e:
            print(f"❌ 对话异常: {str(e)}")
            return None
    
    def test_search(self, query, document_path, top_k=3):
        """测试搜索功能"""
        print(f"🔍 测试搜索: {query}")
        print(f"📂 文档路径: {document_path}")
        try:
            data = {
                "query": query,
                "document_path": document_path,
                "top_k": top_k
            }
            response = self.session.post(f"{self.base_url}/search", json=data)
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ 找到 {result['total']} 个相关片段")
                for i, doc in enumerate(result['results'][:2]):
                    print(f"   {i+1}. 相似度: {doc['score']:.3f}")
                    print(f"      内容: {doc['content'][:100]}...")
                return True
            else:
                print(f"❌ 搜索失败: {response.text}")
                return False
        except Exception as e:
            print(f"❌ 搜索异常: {str(e)}")
            return False
    
    def test_conversation_history(self, conversation_id="test"):
        """测试对话历史"""
        print(f"📚 测试获取对话历史: {conversation_id}")
        try:
            response = self.session.get(f"{self.base_url}/conversations/{conversation_id}/history")
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ 对话历史长度: {len(result['history'])}")
                return result
            else:
                print(f"❌ 获取对话历史失败: {response.text}")
                return None
        except Exception as e:
            print(f"❌ 获取对话历史异常: {str(e)}")
            return None
    
    def test_stats(self):
        """测试统计信息"""
        print("📊 测试获取统计信息...")
        try:
            response = self.session.get(f"{self.base_url}/stats")
            
            if response.status_code == 200:
                result = response.json()
                print("✅ 系统统计信息:")
                for key, value in result.items():
                    print(f"   {key}: {value}")
                return result
            else:
                print(f"❌ 获取统计信息失败: {response.text}")
                return None
        except Exception as e:
            print(f"❌ 获取统计信息异常: {str(e)}")
            return None
    
    def run_demo(self, document_path="ai"):
        """运行演示测试"""
        print("🚀 开始RAG系统演示测试")
        print("=" * 60)
        
        # 1. 健康检查
        if not self.test_health():
            print("❌ 服务不可用，测试终止")
            return False
        
        print()
        
        # 2. 测试搜索
        print("🔍 测试搜索功能")
        print("-" * 30)
        self.test_search("人工智能是什么", document_path)
        print()
        
        # 3. 测试问答
        print("❓ 测试问答功能")
        print("-" * 30)
        query_result = self.test_query("什么是人工智能？请详细解释", document_path, "demo_conversation")
        print()
        
        # 4. 测试多轮对话
        print("💬 测试多轮对话")
        print("-" * 30)
        chat1 = self.test_chat("请介绍机器学习的基本概念", document_path, "demo_conversation")
        print()
        
        if chat1:
            chat2 = self.test_chat(
                "机器学习和深度学习有什么区别？", 
                document_path, 
                "demo_conversation", 
                chat1.get('updated_history')
            )
            print()
        
        # 5. 获取对话历史
        print("📚 测试对话历史")
        print("-" * 30)
        self.test_conversation_history("demo_conversation")
        print()
        
        # 6. 获取统计信息
        print("📊 测试统计信息")
        print("-" * 30)
        self.test_stats()
        print()
        
        print("✅ 演示测试完成！")
        return True

def main():
    """主函数"""
    import argparse
    
    parser = argparse.ArgumentParser(description="RAG系统测试客户端")
    parser.add_argument("--url", default="http://localhost:5000", help="服务器URL")
    parser.add_argument("--path", default="ai", help="测试文档路径")
    parser.add_argument("--query", help="测试查询")
    parser.add_argument("--action", choices=[
        "health", "query", "chat", "search", "demo"
    ], default="demo", help="测试动作")
    
    args = parser.parse_args()
    
    client = RAGTestClient(args.url)
    
    if args.action == "health":
        client.test_health()
    elif args.action == "query":
        query = args.query or "什么是人工智能？"
        client.test_query(query, args.path)
    elif args.action == "chat":
        message = args.query or "请介绍一下机器学习"
        client.test_chat(message, args.path)
    elif args.action == "search":
        query = args.query or "人工智能"
        client.test_search(query, args.path)
    elif args.action == "demo":
        print(f"使用文档路径: {args.path}")
        print("请确保该路径在配置的文档根目录下存在文档文件")
        print()
        client.run_demo(args.path)

if __name__ == "__main__":
    main() 
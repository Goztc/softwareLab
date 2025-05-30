# RAG流水线 - 基于LangChain实现
# Author: AI Assistant
# Version: 3.0 - 基于RAG-Tongyi.py优化

import os
from pathlib import Path
from typing import Optional, List

# LangChain imports - 使用最新的导入方式
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_community.document_loaders import (
    TextLoader, UnstructuredMarkdownLoader, PyPDFLoader, Docx2txtLoader, DirectoryLoader
)
from langchain_core.documents import Document
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate
from langchain_community.chat_models import ChatTongyi

# 本地导入
from config import QWEN_API_KEY, QWEN_MODEL_NAME, EMBEDDING_MODEL_NAME

class RAGPipeline:
    """RAG流水线类 - 基于LangChain实现，参考RAG-Tongyi.py优化"""
    
    def __init__(self):
        """初始化RAG流水线"""
        print("🔧 初始化RAG流水线...")
        
        # 设置通义千问API密钥
        os.environ["DASHSCOPE_API_KEY"] = QWEN_API_KEY
        
        # 初始化通义千问模型 - 参考RAG-Tongyi.py的实现
        self.llm = ChatTongyi(
            model=QWEN_MODEL_NAME,  # 使用配置中的模型名称
            temperature=0.1,  # 控制生成文本的随机性
            top_p=0.9,  # 控制词汇选择的多样性
        )
        
        # 初始化embedding模型
        self.embeddings = HuggingFaceEmbeddings(
            model_name=EMBEDDING_MODEL_NAME,
            model_kwargs={'device': 'cpu'},
            encode_kwargs={'normalize_embeddings': True}
        )
        
        # 初始化文本分割器
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=500,
            chunk_overlap=100,  # 增加重叠以保持上下文
            separators=["\n\n", "\n", "。", ".", " ", ""]
        )
        
        # 初始化提示模板 - 参考RAG-Tongyi.py的设计
        self.prompt_template = PromptTemplate(
            input_variables=["context", "question"],
            template="""
根据以下检索到的相关文档内容回答问题。请确保回答准确、简洁、有帮助。
如果提供的文档内容不足以回答问题，请如实说明。

相关文档:
{context}

问题: {question}

回答:"""
        )
        
        # 初始化其他属性
        self.conversation_histories = {}
        
        print("✅ RAG流水线初始化成功")
    
    def _load_documents_from_path(self, document_paths) -> List[Document]:
        """从指定路径加载文档，支持多个路径"""
        # 如果传入的是字符串，转换为列表
        if isinstance(document_paths, str):
            document_paths = [document_paths]
        
        all_documents = []
        
        for document_path in document_paths:
            if not os.path.exists(document_path):
                print(f"⚠️  警告：文档路径不存在，跳过: {document_path}")
                continue
            
            documents = []
            
            if os.path.isfile(document_path):
                # 单个文件
                documents.extend(self._load_single_file(document_path))
                print(f"📄 加载文件: {document_path}")
            elif os.path.isdir(document_path):
                # 目录 - 递归遍历所有支持的文件类型
                print(f"📁 加载目录: {document_path}")
                file_count = 0
                for file_path in Path(document_path).rglob("*"):
                    if file_path.is_file() and file_path.suffix.lower() in ['.txt', '.md', '.pdf', '.docx']:
                        file_documents = self._load_single_file(str(file_path))
                        documents.extend(file_documents)
                        if file_documents:  # 只有成功加载的文件才计数
                            file_count += 1
                            print(f"  📄 已加载: {file_path.name}")
                print(f"📁 目录 {document_path} 共加载 {file_count} 个文件")
            
            all_documents.extend(documents)
        
        print(f"📚 总共加载了 {len(all_documents)} 个文档")
        return all_documents
    
    def _load_single_file(self, file_path: str) -> List[Document]:
        """加载单个文件"""
        file_extension = Path(file_path).suffix.lower()
        
        try:
            if file_extension == '.txt':
                loader = TextLoader(file_path, encoding='utf-8')
            elif file_extension == '.md':
                loader = UnstructuredMarkdownLoader(file_path)
            elif file_extension == '.pdf':
                loader = PyPDFLoader(file_path)
            elif file_extension in ['.docx', '.doc']:
                loader = Docx2txtLoader(file_path)
            else:
                # 默认作为文本文件处理
                loader = TextLoader(file_path, encoding='utf-8')
            
            return loader.load()
        except Exception as e:
            print(f"Error loading file {file_path}: {e}")
            return []
    
    def query(self, question: str, document_path, top_k: int = 3) -> dict:
        """
        查询文档并返回答案
        
        Args:
            question: 用户问题
            document_path: 文档路径（可以是字符串、列表。支持文件路径、文件夹路径或混合）
            top_k: 返回最相关的文档数量
            
        Returns:
            包含答案和来源文档的字典
        """
        try:
            # 加载文档 - 现在支持多个路径
            documents = self._load_documents_from_path(document_path)
            if not documents:
                return {
                    "question": question,
                    "answer": "抱歉，没有找到相关文档。",
                    "sources": []
                }
            
            print(f"📊 已加载 {len(documents)} 个文档用于查询")
            
            # 分割文档 - 参考RAG-Tongyi.py的实现
            doc_splits = self.text_splitter.split_documents(documents)
            print(f"🔪 已将文档分割为 {len(doc_splits)} 个文本块")
            
            # 创建向量存储
            vectorstore = FAISS.from_documents(doc_splits, self.embeddings)
            print("🗄️  向量数据库创建成功！")
            
            # 创建检索器
            retriever = vectorstore.as_retriever(
                search_kwargs={"k": top_k}  # 检索前k个最相关的块
            )
            
            # 创建RetrievalQA链 - 参考RAG-Tongyi.py的实现
            retrieval_qa = RetrievalQA.from_chain_type(
                llm=self.llm,
                chain_type="stuff",  # 将所有检索到的文档放入提示中
                retriever=retriever,
                return_source_documents=True,  # 在响应中包含源文档
                chain_type_kwargs={"prompt": self.prompt_template}  # 使用自定义提示
            )
            
            # 执行查询
            result = retrieval_qa.invoke({"query": question})
            
            # 提取源文档信息
            sources = []
            source_files = []
            for doc in result.get("source_documents", []):
                source_info = doc.metadata.get("source", "Unknown")
                if source_info not in source_files:
                    source_files.append(source_info)
                    sources.append({
                        "content": doc.page_content,
                        "source": source_info
                    })
            
            return {
                "question": question,
                "answer": result["result"],
                "sources": sources
            }
            
        except Exception as e:
            print(f"❌ 查询过程中发生错误: {e}")
            return {
                "question": question,
                "answer": f"查询过程中发生错误: {str(e)}",
                "sources": []
            }
    
    def chat(self, question: str, document_path, history: Optional[List] = None, conversation_id: str = "default") -> dict:
        """
        带历史对话的查询 - 真正的对话式RAG实现
        
        Args:
            question: 用户问题
            document_path: 文档路径（可以是字符串、列表。支持文件路径、文件夹路径或混合）
            history: 对话历史
            conversation_id: 对话ID，用于标识不同的对话会话
            
        Returns:
            包含答案和来源文档的字典
        """
        try:
            # 获取或初始化对话历史
            if conversation_id not in self.conversation_histories:
                self.conversation_histories[conversation_id] = []
            
            current_history = self.conversation_histories[conversation_id]
            
            # 如果传入了history参数，使用它来更新当前历史
            if history is not None:
                current_history = history.copy()
                self.conversation_histories[conversation_id] = current_history
            
            # 加载文档
            documents = self._load_documents_from_path(document_path)
            if not documents:
                return {
                    "question": question,
                    "answer": "抱歉，没有找到相关文档。",
                    "response": "抱歉，没有找到相关文档。",
                    "sources": [],
                    "updated_history": current_history,
                    "conversation_id": conversation_id
                }
            
            print(f"📊 已加载 {len(documents)} 个文档用于对话查询")
            
            # 分割文档
            doc_splits = self.text_splitter.split_documents(documents)
            print(f"🔪 已将文档分割为 {len(doc_splits)} 个文本块")
            
            # 创建向量存储
            vectorstore = FAISS.from_documents(doc_splits, self.embeddings)
            print("🗄️  向量数据库创建成功！")
            
            # 创建检索器
            retriever = vectorstore.as_retriever(search_kwargs={"k": 3})
            
            # 检索相关文档
            retrieved_docs = retriever.get_relevant_documents(question)
            
            # 构建上下文
            context = "\n\n".join([doc.page_content for doc in retrieved_docs])
            
            # 构建包含历史对话的提示
            conversation_context = ""
            if current_history:
                print(f"💬 使用 {len(current_history)} 轮历史对话")
                conversation_context = "\n以下是之前的对话历史:\n"
                for i, exchange in enumerate(current_history[-5:], 1):  # 只使用最近5轮对话
                    conversation_context += f"第{i}轮对话:\n"
                    conversation_context += f"用户: {exchange.get('question', '')}\n"
                    conversation_context += f"助手: {exchange.get('answer', '')}\n\n"
                conversation_context += "---\n"
            
            # 创建增强的提示模板，包含历史对话
            enhanced_prompt = f"""
你是一个基于文档的智能助手。请根据检索到的相关文档内容和对话历史来回答用户的问题。

{conversation_context}

相关文档内容:
{context}

当前用户问题: {question}

请根据上述信息回答用户问题。如果问题涉及之前的对话内容，请结合历史对话来回答。如果文档内容不足以回答问题，请如实说明。

回答:"""
            
            # 直接调用LLM生成回答
            print("🤖 正在生成带历史对话的回答...")
            response = self.llm.invoke(enhanced_prompt)
            
            # 提取回答内容
            if hasattr(response, 'content'):
                answer = response.content
            else:
                answer = str(response)
            
            # 提取源文档信息
            sources = []
            source_files = []
            for doc in retrieved_docs:
                source_info = doc.metadata.get("source", "Unknown")
                if source_info not in source_files:
                    source_files.append(source_info)
                    sources.append({
                        "content": doc.page_content,
                        "source": source_info
                    })
            
            # 添加当前对话到历史
            current_exchange = {
                "question": question,
                "answer": answer,
                "sources": sources,
                "timestamp": __import__('datetime').datetime.now().isoformat()
            }
            
            current_history.append(current_exchange)
            self.conversation_histories[conversation_id] = current_history
            
            print(f"✅ 对话回答生成成功，历史记录已更新 (共{len(current_history)}轮)")
            
            return {
                "question": question,
                "answer": answer,
                "response": answer,  # 为了兼容性
                "sources": sources,
                "updated_history": current_history.copy(),
                "conversation_id": conversation_id
            }
            
        except Exception as e:
            print(f"❌ 对话查询过程中发生错误: {e}")
            return {
                "question": question,
                "answer": f"对话查询过程中发生错误: {str(e)}",
                "response": f"对话查询过程中发生错误: {str(e)}",
                "sources": [],
                "updated_history": current_history,
                "conversation_id": conversation_id
            }
    
    def search(self, query: str, document_path, top_k: int = 5) -> dict:
        """
        搜索相关文档片段
        
        Args:
            query: 搜索查询
            document_path: 文档路径（可以是字符串、列表。支持文件路径、文件夹路径或混合）
            top_k: 返回结果数量
            
        Returns:
            包含搜索结果的字典
        """
        try:
            # 加载文档
            documents = self._load_documents_from_path(document_path)
            if not documents:
                return {
                    "query": query,
                    "results": [],
                    "message": "没有找到相关文档"
                }
            
            # 分割文档
            doc_splits = self.text_splitter.split_documents(documents)
            
            # 创建向量存储
            vectorstore = FAISS.from_documents(doc_splits, self.embeddings)
            
            # 执行搜索
            retriever = vectorstore.as_retriever(search_kwargs={"k": top_k})
            docs = retriever.get_relevant_documents(query)
            
            results = []
            for i, doc in enumerate(docs):
                # 如果文档有score属性则使用，否则使用基于排名的默认分数
                score = getattr(doc, 'score', None)
                if score is None:
                    # 基于排名生成一个模拟分数，排名越靠前分数越高
                    score = 1.0 - (i * 0.1)  # 第一个结果1.0，第二个0.9，以此类推
                
                results.append({
                    "rank": i + 1,
                    "content": doc.page_content[:200] + "..." if len(doc.page_content) > 200 else doc.page_content,
                    "source": doc.metadata.get("source", "Unknown"),
                    "score": score
                })
            
            return {
                "query": query,
                "results": results,
                "total": len(results)
            }
            
        except Exception as e:
            print(f"Error in search: {e}")
            return {
                "query": query,
                "results": [],
                "error": str(e)
            }
    
    def get_conversation_history(self, conversation_id: str) -> List[dict]:
        """获取对话历史"""
        return self.conversation_histories.get(conversation_id, [])
    
    def clear_conversation_history(self, conversation_id: str):
        """清除对话历史"""
        if conversation_id in self.conversation_histories:
            del self.conversation_histories[conversation_id]
            print(f"Cleared conversation history for {conversation_id}")
    
    def get_stats(self) -> dict:
        """获取统计信息"""
        return {
            'embedding_model': EMBEDDING_MODEL_NAME,
            'llm_model': QWEN_MODEL_NAME,  # 直接使用配置中的模型名称
            'chunk_size': self.text_splitter._chunk_size,
            'chunk_overlap': self.text_splitter._chunk_overlap,
            'active_conversations': len(self.conversation_histories)
        } 
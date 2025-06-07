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
from config import QWEN_API_KEY, QWEN_MODEL_NAME, EMBEDDING_MODEL_NAME, DOCUMENTS_ROOT_PATH, TEXT_SPLITTER_CONFIG

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
        
        # 初始化文本分割器 - 使用配置文件中的参数
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=TEXT_SPLITTER_CONFIG["chunk_size"],
            chunk_overlap=TEXT_SPLITTER_CONFIG["chunk_overlap"],
            separators=TEXT_SPLITTER_CONFIG["separators"]
        )
        print(f"📋 文本分割器配置: chunk_size={TEXT_SPLITTER_CONFIG['chunk_size']}, chunk_overlap={TEXT_SPLITTER_CONFIG['chunk_overlap']}")
        
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
        
        # 初始化缓存相关属性 - 优化性能
        self.vectorstore_cache = {}  # 向量存储缓存 {path_key: vectorstore}
        self.retriever_cache = {}    # 检索器缓存 {path_key: retriever}
        self.document_cache = {}     # 文档缓存 {path_key: documents}
        self.cache_timestamps = {}   # 缓存时间戳 {path_key: timestamp}
        
        # 持久化向量存储配置
        self.vector_store_root = Path("vector_stores")  # 向量存储根目录
        self.vector_store_root.mkdir(exist_ok=True)     # 确保目录存在
        
        # 文档根路径配置
        self.documents_root = Path(DOCUMENTS_ROOT_PATH)
        
        # 初始化其他属性
        self.conversation_histories = {}
        
        print("✅ RAG流水线初始化成功（带缓存优化 + 持久化支持）")
    
    def _get_relative_path(self, absolute_path: str) -> str:
        """
        将绝对路径转换为相对于文档根目录的相对路径
        
        Args:
            absolute_path: 绝对路径
            
        Returns:
            相对路径字符串
        """
        try:
            abs_path = Path(absolute_path)
            # 如果路径在文档根目录下，返回相对路径
            if abs_path.is_absolute() and self.documents_root in abs_path.parents or abs_path == self.documents_root:
                relative_path = abs_path.relative_to(self.documents_root)
                return str(relative_path).replace('\\', '/')  # 统一使用正斜杠
            else:
                # 如果不在文档根目录下，返回文件名
                return abs_path.name
        except (ValueError, OSError):
            # 如果转换失败，返回原始路径的文件名部分
            return Path(absolute_path).name
    
    def _extract_sources_from_docs(self, docs: List[Document]) -> List[dict]:
        """
        从文档列表中提取源文档信息，保留所有相关片段并转换为相对路径
        
        Args:
            docs: 文档列表
            
        Returns:
            源文档信息列表，包含content和source字段
        """
        sources = []
        source_file_counts = {}  # 记录每个文件的片段数量
        
        for doc in docs:
            source_info = doc.metadata.get("source", "Unknown")
            relative_source = self._get_relative_path(source_info)
            
            
            sources.append({
                "content": doc.page_content[:50],  # 增加内容预览长度
                "source": relative_source  # 保留原始文件路径
            })
        
        return sources
    
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
    
    def _get_path_key(self, document_path) -> str:
        """生成文档路径的唯一标识符"""
        if isinstance(document_path, str):
            return document_path
        elif isinstance(document_path, list):
            return "|".join(sorted(document_path))
        else:
            return str(document_path)
    
    def _get_or_create_vectorstore(self, document_path) -> tuple:
        """
        获取或创建向量存储和检索器（带缓存和持久化）
        
        Returns:
            tuple: (vectorstore, retriever, sources_info)
        """
        import time
        
        path_key = self._get_path_key(document_path)
        current_time = time.time()
        
        # 1. 检查内存缓存是否存在且有效（缓存有效期：1小时）
        if (path_key in self.vectorstore_cache and 
            path_key in self.cache_timestamps and 
            current_time - self.cache_timestamps[path_key] < 3600):
            
            print(f"🚀 使用内存缓存的向量存储: {path_key}")
            vectorstore = self.vectorstore_cache[path_key]
            retriever = self.retriever_cache[path_key]
            
            # 重新生成源文档信息（这部分比较轻量）
            documents = self.document_cache[path_key]
            sources_info = self._extract_sources_from_docs(documents)
            
            return vectorstore, retriever, sources_info
        
        # 2. 尝试从磁盘加载持久化的向量存储
        persistent_path = self._get_persistent_path(document_path)
        if persistent_path.exists():
            print(f"📂 从磁盘加载持久化向量存储: {path_key}")
            try:
                vectorstore = FAISS.load_local(
                    str(persistent_path), 
                    self.embeddings,
                    allow_dangerous_deserialization=True
                )
                
                # 更新内存缓存
                retriever = vectorstore.as_retriever(search_kwargs={"k": 3})
                self.vectorstore_cache[path_key] = vectorstore
                self.retriever_cache[path_key] = retriever
                self.cache_timestamps[path_key] = current_time
                
                # 尝试重新加载文档信息
                documents = self._load_documents_from_path(document_path)
                if documents:
                    self.document_cache[path_key] = documents
                    sources_info = self._extract_sources_from_docs(documents)
                    return vectorstore, retriever, sources_info
                
                print(f"✅ 从磁盘加载向量存储成功: {path_key}")
                return vectorstore, retriever, []
                
            except Exception as e:
                print(f"⚠️  从磁盘加载向量存储失败，将重新构建: {e}")
        
        # 3. 缓存不存在且磁盘没有，重新构建
        print(f"🔄 重新构建向量存储: {path_key}")
        
        # 加载文档
        documents = self._load_documents_from_path(document_path)
        if not documents:
            raise ValueError("没有找到相关文档")
        
        print(f"📊 已加载 {len(documents)} 个文档用于向量化")
        
        # 分割文档
        doc_splits = self.text_splitter.split_documents(documents)
        print(f"🔪 已将文档分割为 {len(doc_splits)} 个文本块")
        
        # 创建向量存储
        vectorstore = FAISS.from_documents(doc_splits, self.embeddings)
        print("🗄️  向量数据库创建成功！")
        
        # 创建检索器
        retriever = vectorstore.as_retriever(search_kwargs={"k": 3})
        
        # 生成源文档信息
        sources_info = self._extract_sources_from_docs(documents)
        
        # 缓存结果
        self.vectorstore_cache[path_key] = vectorstore
        self.retriever_cache[path_key] = retriever
        self.document_cache[path_key] = documents
        self.cache_timestamps[path_key] = current_time
        
        print(f"💾 向量存储已缓存: {path_key}")
        
        return vectorstore, retriever, sources_info
    
    def clear_cache(self, document_path=None):
        """清除缓存"""
        if document_path is None:
            # 清除所有缓存
            self.vectorstore_cache.clear()
            self.retriever_cache.clear()
            self.document_cache.clear()
            self.cache_timestamps.clear()
            print("🧹 已清除所有向量存储缓存")
        else:
            # 清除特定路径的缓存
            path_key = self._get_path_key(document_path)
            for cache_dict in [self.vectorstore_cache, self.retriever_cache, 
                             self.document_cache, self.cache_timestamps]:
                cache_dict.pop(path_key, None)
            print(f"🧹 已清除缓存: {path_key}")
    
    def query(self, question: str, document_path, top_k: int = 5) -> dict:
        """
        查询文档并返回答案（使用缓存优化）
        
        Args:
            question: 用户问题
            document_path: 文档路径（可以是字符串、列表。支持文件路径、文件夹路径或混合）
            top_k: 返回最相关的文档数量
            
        Returns:
            包含答案和来源文档的字典
        """
        try:
            # 使用缓存获取向量存储和检索器
            vectorstore, retriever, all_sources = self._get_or_create_vectorstore(document_path)
            
            # 更新检索器的top_k参数
            retriever = vectorstore.as_retriever(search_kwargs={"k": top_k})
            
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
            
            # 获取源文档并添加调试信息
            source_documents = result.get("source_documents", [])
            print(f"🔍 检索到 {len(source_documents)} 个相关文档片段")
            
            # 提取源文档信息
            sources = self._extract_sources_from_docs(source_documents)
            
            return {
                "question": question,
                "answer": result["result"],
                "sources": sources
            }
        
        except ValueError as e:
            print(f"❌ 查询过程中发生错误: {e}")
            return {
                "question": question,
                "answer": f"查询过程中发生错误: {str(e)}",
                "sources": []
            }
            
        except Exception as e:
            print(f"❌ 查询过程中发生错误: {e}")
            return {
                "question": question,
                "answer": f"查询过程中发生错误: {str(e)}",
                "sources": []
            }
    
    def chat(self, question: str, document_path, history: Optional[List] = None, conversation_id: str = "default", top_k: int = 5) -> dict:
        """
        带历史对话的查询 - 真正的对话式RAG实现
        
        Args:
            question: 用户问题
            document_path: 文档路径（可以是字符串、列表。支持文件路径、文件夹路径或混合）
            history: 对话历史
            conversation_id: 对话ID，用于标识不同的对话会话
            top_k: 返回最相关的文档数量
            
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
            
            # 使用缓存获取向量存储和检索器
            try:
                vectorstore, retriever, all_sources = self._get_or_create_vectorstore(document_path)
            except ValueError as e:
                return {
                    "question": question,
                    "answer": str(e),
                    "response": str(e),
                    "sources": [],
                    "updated_history": current_history,
                    "conversation_id": conversation_id
                }
            
            # 使用指定的top_k更新检索器并检索相关文档
            retriever = vectorstore.as_retriever(search_kwargs={"k": top_k})
            retrieved_docs = retriever.get_relevant_documents(question)
            
            print(f"🔍 检索到 {len(retrieved_docs)} 个相关文档片段")
            
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
            sources = self._extract_sources_from_docs(retrieved_docs)
            
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
            # 使用缓存获取向量存储
            vectorstore, _, all_sources = self._get_or_create_vectorstore(document_path)
            
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
                    "source": self._get_relative_path(doc.metadata.get("source", "Unknown")),
                    "score": score
                })
            
            return {
                "query": query,
                "results": results,
                "total": len(results)
            }
            
        except ValueError as e:
            return {
                "query": query,
                "results": [],
                "error": str(e)
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
            'active_conversations': len(self.conversation_histories),
            'cached_vectorstores': len(self.vectorstore_cache),
            'cache_keys': list(self.cache_timestamps.keys()),
            'persistent_vectorstores': self._list_persistent_vectorstores()
        }  
    
    def _list_persistent_vectorstores(self) -> List[str]:
        """列出所有持久化的向量存储"""
        if not self.vector_store_root.exists():
            return []
        return [item.name for item in self.vector_store_root.iterdir() if item.is_dir()]
    
    def _get_persistent_path(self, document_path) -> Path:
        """获取持久化向量存储的路径"""
        path_key = self._get_path_key(document_path)
        safe_path_key = path_key.replace('/', '_').replace('\\', '_').replace('|', '_')
        return self.vector_store_root / safe_path_key
    
    def save_vectorstore(self, document_path, force_rebuild: bool = False) -> dict:
        """
        构建并保存向量存储到磁盘
        
        Args:
            document_path: 文档路径
            force_rebuild: 是否强制重新构建
            
        Returns:
            保存结果信息
        """
        try:
            path_key = self._get_path_key(document_path)
            persistent_path = self._get_persistent_path(document_path)
            
            # 检查是否已存在且不强制重建
            if persistent_path.exists() and not force_rebuild:
                return {
                    "status": "exists",
                    "message": f"向量存储已存在: {path_key}",
                    "path": str(persistent_path)
                }
            
            print(f"🔨 开始构建向量存储: {path_key}")
            
            # 加载文档
            documents = self._load_documents_from_path(document_path)
            if not documents:
                raise ValueError("没有找到相关文档")
            
            print(f"📊 已加载 {len(documents)} 个文档用于向量化")
            
            # 分割文档
            doc_splits = self.text_splitter.split_documents(documents)
            print(f"🔪 已将文档分割为 {len(doc_splits)} 个文本块")
            
            # 创建向量存储
            vectorstore = FAISS.from_documents(doc_splits, self.embeddings)
            print("🗄️  向量数据库创建成功！")
            
            # 保存到磁盘
            vectorstore.save_local(str(persistent_path))
            print(f"💾 向量存储已保存到: {persistent_path}")
            
            # 同时更新内存缓存
            import time
            retriever = vectorstore.as_retriever(search_kwargs={"k": 3})
            self.vectorstore_cache[path_key] = vectorstore
            self.retriever_cache[path_key] = retriever
            self.document_cache[path_key] = documents
            self.cache_timestamps[path_key] = time.time()
            
            # 保存元数据
            metadata = {
                "document_path": document_path,
                "document_count": len(documents),
                "chunk_count": len(doc_splits),
                "created_at": time.time(),
                "embedding_model": EMBEDDING_MODEL_NAME
            }
            
            import json
            metadata_path = persistent_path / "metadata.json"
            with open(metadata_path, 'w', encoding='utf-8') as f:
                json.dump(metadata, f, ensure_ascii=False, indent=2)
            
            return {
                "status": "created",
                "message": f"向量存储构建并保存成功: {path_key}",
                "path": str(persistent_path),
                "metadata": metadata
            }
            
        except Exception as e:
            print(f"❌ 保存向量存储失败: {e}")
            return {
                "status": "error",
                "message": f"保存向量存储失败: {str(e)}"
            }
    
    def load_vectorstore(self, document_path) -> dict:
        """
        从磁盘加载向量存储
        
        Args:
            document_path: 文档路径
            
        Returns:
            加载结果信息
        """
        try:
            path_key = self._get_path_key(document_path)
            persistent_path = self._get_persistent_path(document_path)
            
            if not persistent_path.exists():
                return {
                    "status": "not_found",
                    "message": f"向量存储不存在: {path_key}"
                }
            
            print(f"📂 从磁盘加载向量存储: {path_key}")
            
            # 从磁盘加载向量存储
            vectorstore = FAISS.load_local(
                str(persistent_path), 
                self.embeddings,
                allow_dangerous_deserialization=True
            )
            
            # 加载元数据
            metadata_path = persistent_path / "metadata.json"
            metadata = {}
            if metadata_path.exists():
                import json
                with open(metadata_path, 'r', encoding='utf-8') as f:
                    metadata = json.load(f)
            
            # 更新内存缓存
            import time
            retriever = vectorstore.as_retriever(search_kwargs={"k": 3})
            self.vectorstore_cache[path_key] = vectorstore
            self.retriever_cache[path_key] = retriever
            self.cache_timestamps[path_key] = time.time()
            
            # 如果有元数据中的文档信息，可以重新加载文档（可选）
            if "document_path" in metadata:
                try:
                    documents = self._load_documents_from_path(metadata["document_path"])
                    self.document_cache[path_key] = documents
                except:
                    pass  # 如果文档加载失败，不影响向量存储的使用
            
            print(f"✅ 向量存储加载成功: {path_key}")
            
            return {
                "status": "loaded",
                "message": f"向量存储加载成功: {path_key}",
                "path": str(persistent_path),
                "metadata": metadata
            }
            
        except Exception as e:
            print(f"❌ 加载向量存储失败: {e}")
            return {
                "status": "error",
                "message": f"加载向量存储失败: {str(e)}"
            }
    
    def list_vectorstores(self) -> dict:
        """
        列出所有持久化的向量存储
        
        Returns:
            向量存储列表信息
        """
        try:
            vectorstores = []
            
            if self.vector_store_root.exists():
                for item in self.vector_store_root.iterdir():
                    if item.is_dir():
                        metadata_path = item / "metadata.json"
                        metadata = {}
                        if metadata_path.exists():
                            import json
                            try:
                                with open(metadata_path, 'r', encoding='utf-8') as f:
                                    metadata = json.load(f)
                            except:
                                pass
                        
                        vectorstores.append({
                            "name": item.name,
                            "path": str(item),
                            "metadata": metadata
                        })
            
            return {
                "status": "success",
                "vectorstores": vectorstores,
                "total": len(vectorstores)
            }
            
        except Exception as e:
            return {
                "status": "error",
                "message": f"列出向量存储失败: {str(e)}"
            }
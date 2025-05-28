import os
import logging
from flask import Flask, request, jsonify
from flask_cors import CORS
import config
from core.rag_pipeline import RAGPipeline
from datetime import datetime
from pathlib import Path

# 配置日志
logging.basicConfig(
    level=getattr(logging, config.LOG_LEVEL),
    format=config.LOG_FORMAT
)
logger = logging.getLogger(__name__)

# 创建Flask应用
app = Flask(__name__)
CORS(app)  # 允许跨域请求

# 初始化RAG流水线（延迟初始化）
rag_pipeline = None

def get_rag_pipeline():
    """获取RAG流水线实例（单例模式）"""
    global rag_pipeline
    if rag_pipeline is None:
        rag_pipeline = RAGPipeline()
    return rag_pipeline

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    return jsonify({
        'status': 'healthy',
        'timestamp': datetime.utcnow().isoformat(),
        'documents_root_path': str(config.DOCUMENTS_ROOT_PATH),
        'message': 'RAG系统运行正常'
    })

@app.route('/query', methods=['POST'])
def query_documents():
    """查询文档接口"""
    try:
        data = request.json
        
        # 参数验证
        if not data or 'question' not in data or 'document_path' not in data:
            return jsonify({
                'error': '缺少必需参数：question 和 document_path'
            }), 400
        
        question = data['question'].strip()
        document_paths = data['document_path']
        top_k = data.get('top_k', 3)
        
        if not question:
            return jsonify({'error': '问题不能为空'}), 400
        
        if not document_paths:
            return jsonify({'error': '文档路径不能为空'}), 400
        
        # 处理文档路径
        full_document_paths = _process_document_paths(document_paths)
        if not full_document_paths:
            return jsonify({'error': '文档路径不能为空或无效'}), 400
        
        # 执行查询
        pipeline = get_rag_pipeline()
        result = pipeline.query(question, full_document_paths, top_k)
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({
            'error': f'查询失败: {str(e)}'
        }), 500

@app.route('/chat', methods=['POST'])
def chat_with_documents():
    """与文档聊天接口"""
    try:
        data = request.json
        
        # 参数验证
        if not data or 'message' not in data or 'document_path' not in data:
            return jsonify({
                'error': '缺少必需参数：message 和 document_path'
            }), 400
        
        message = data['message'].strip()
        document_paths = data['document_path']
        history = data.get('history', [])
        conversation_id = data.get('conversation_id', 'default')  # 获取conversation_id
        
        if not message:
            return jsonify({'error': '消息不能为空'}), 400
        
        if not document_paths:
            return jsonify({'error': '文档路径不能为空'}), 400
        
        # 处理文档路径
        full_document_paths = _process_document_paths(document_paths)
        if not full_document_paths:
            return jsonify({'error': '文档路径不能为空或无效'}), 400
        
        # 执行聊天
        pipeline = get_rag_pipeline()
        result = pipeline.chat(message, full_document_paths, history, conversation_id)
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({
            'error': f'聊天失败: {str(e)}'
        }), 500

@app.route('/search', methods=['POST'])
def search_documents():
    """搜索文档接口"""
    try:
        data = request.json
        
        # 参数验证
        if not data or 'query' not in data or 'document_path' not in data:
            return jsonify({
                'error': '缺少必需参数：query 和 document_path'
            }), 400
        
        query = data['query'].strip()
        document_paths = data['document_path']
        top_k = data.get('top_k', 5)
        
        if not query:
            return jsonify({'error': '查询不能为空'}), 400
        
        if not document_paths:
            return jsonify({'error': '文档路径不能为空'}), 400
        
        # 处理文档路径
        full_document_paths = _process_document_paths(document_paths)
        if not full_document_paths:
            return jsonify({'error': '文档路径不能为空或无效'}), 400
        
        # 执行搜索
        pipeline = get_rag_pipeline()
        result = pipeline.search(query, full_document_paths, top_k)
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({
            'error': f'搜索失败: {str(e)}'
        }), 500

def _process_document_paths(document_paths):
    """处理文档路径（支持单个路径或多个路径）"""
    if not document_paths:
        return []
    
    # 如果是字符串，转换为列表
    if isinstance(document_paths, str):
        paths = [document_paths.strip()]
    elif isinstance(document_paths, list):
        paths = [path.strip() for path in document_paths if isinstance(path, str) and path.strip()]
    else:
        return []
    
    # 过滤空路径
    paths = [path for path in paths if path]
    
    # 拼接完整路径并进行安全检查
    full_paths = []
    for path in paths:
        full_path = os.path.join(config.DOCUMENTS_ROOT_PATH, path)
        if _is_safe_path(full_path):
            full_paths.append(full_path)
        else:
            print(f"⚠️  跳过无效路径: {path}")
    
    return full_paths

def _is_safe_path(path):
    """检查路径是否安全"""
    try:
        # 确保路径在文档根目录内
        full_path = Path(path).resolve()
        root_path = Path(config.DOCUMENTS_ROOT_PATH).resolve()
        full_path.relative_to(root_path)
        return True
    except (ValueError, OSError):
        return False

@app.route('/conversations/<conversation_id>/history', methods=['GET'])
def get_conversation_history(conversation_id):
    """获取对话历史"""
    try:
        pipeline = get_rag_pipeline()  # 使用函数获取实例
        history = pipeline.get_conversation_history(conversation_id)
        return jsonify({
            "conversation_id": conversation_id,
            "history": history
        })
    except Exception as e:
        logger.error(f"Get history error: {str(e)}")
        return jsonify({"error": f"获取历史失败: {str(e)}"}), 500

@app.route('/conversations/<conversation_id>/clear', methods=['POST'])
def clear_conversation(conversation_id):
    """清除对话历史"""
    try:
        pipeline = get_rag_pipeline()  # 使用函数获取实例
        pipeline.clear_conversation_history(conversation_id)
        return jsonify({
            "message": f"已清除对话 {conversation_id} 的历史记录"
        })
    except Exception as e:
        logger.error(f"Clear conversation error: {str(e)}")
        return jsonify({"error": f"清除历史失败: {str(e)}"}), 500

@app.route('/stats', methods=['GET'])
def get_stats():
    """获取系统统计信息"""
    try:
        pipeline = get_rag_pipeline()  # 使用函数获取实例
        stats = pipeline.get_stats()
        return jsonify(stats)
    except Exception as e:
        logger.error(f"Get stats error: {str(e)}")
        return jsonify({"error": f"获取统计信息失败: {str(e)}"}), 500

@app.errorhandler(404)
def not_found(error):
    return jsonify({"error": "接口不存在"}), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({"error": "内部服务器错误"}), 500

if __name__ == '__main__':
    try:
        app.run(
            host=config.FLASK_HOST,
            port=config.FLASK_PORT,
            debug=config.FLASK_DEBUG
        )
    except Exception as e:
        logger.error(f"Failed to start application: {str(e)}")
        exit(1) 
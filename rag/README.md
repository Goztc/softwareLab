# RAG

## 项目结构

```bash
├───📁 all-MiniLM-L6-v2/
│   ├───📄 config.json
│   ├───📄 model.safetensors
│   ├───📄 special_tokens_map.json
│   ├───📄 tokenizer.json
│   ├───📄 tokenizer_config.json
│   └───📄 vocab.txt
├───📁 core/
│   └───📄 rag_pipeline.py           # 核心逻辑
├───📁 test_docs/
│   ├───📁 ai/
│   │   └───...
│   └───📁 technology/
│       └───...
├───📁 vector_store/
├───📁 vector_stores/
├───📁 _files_user_1/
├───📄 API_DOCUMENTATION.md
├───📄 API_QUICK_REFERENCE.md
├───📄 app.py                         # 主程序
├───📄 config.py                      # 配置文件
├───📄 README.md
├───📄 requirements.txt
├───📄 start_server.py                # 启动服务
├───📄 test_cache_performance.py      # 后面全是chat写的测试，chat特别爱写测试
├───📄 test_client.py 
├───📄 test_conversation_history.py
├───📄 test_fix.py
├───📄 test_multipath.py
└───📄 test_vectorstore_persistence.py
```

## 配置

那个requirements.txt文件是chat写的，不一定对，缺啥下啥就行。能跑RAG的那个环境应该就能跑这个

## 运行

```bash
python start_server.py
```

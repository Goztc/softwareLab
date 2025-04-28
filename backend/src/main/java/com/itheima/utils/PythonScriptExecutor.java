package com.itheima.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Python 脚本执行工具类
 *
 * <p>提供执行 Python 脚本的通用方法，支持参数传递和输出捕获。</p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * {@code
 * PythonScriptExecutor executor = new PythonScriptExecutor();
 *
 * // 执行脚本并获取输出
 * List<String> output = executor.executeScript(
 *     "/usr/bin/python3",
 *     "scripts/process_data.py",
 *     List.of("input.txt", "output.txt")
 * );
 *
 * // 处理输出
 * output.forEach(System.out::println);
 * }
 * </pre>
 *
 * @author zsh
 * @version 1.0
 * @since 2025-4-26
 */
@Component
public class PythonScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PythonScriptExecutor.class);

    /**
     * 执行 Python 脚本并返回输出结果
     *
     * @param pythonInterpreter Python 解释器路径 (如 "/usr/bin/python3")
     * @param scriptPath        Python 脚本路径 (相对于项目根目录或绝对路径)
     * @param arguments         传递给脚本的参数列表
     * @return 脚本执行输出的行列表，如果执行失败则返回空列表
     * @throws IllegalArgumentException 如果 pythonInterpreter 或 scriptPath 为空
     * @throws IOException              如果脚本执行过程中发生 I/O 错误
     * @throws InterruptedException     如果脚本执行被中断
     */
    public List<String> executeScript(String pythonInterpreter, String scriptPath, List<String> arguments)
            throws IOException, InterruptedException {
        // 参数校验
        if (pythonInterpreter == null || pythonInterpreter.trim().isEmpty()) {
            throw new IllegalArgumentException("Python interpreter path cannot be null or empty");
        }
        if (scriptPath == null || scriptPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Script path cannot be null or empty");
        }

        // 构建命令
        List<String> command = new ArrayList<>();
        command.add(pythonInterpreter);
        command.add(scriptPath);
        if (arguments != null) {
            command.addAll(arguments);
        }

        logger.info("Executing Python script: {}", String.join(" ", command));

        // 创建进程构建器
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出

        // 执行脚本
        Process process = processBuilder.start();
        List<String> outputLines = new ArrayList<>();

        // 读取输出
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
                logger.debug("Python script output: {}", line);
            }
        }

        // 等待进程完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorMessage = String.format(
                    "Python script execution failed with exit code %d. Output: %s",
                    exitCode, String.join("\n", outputLines));
            logger.error(errorMessage);
            throw new IOException(errorMessage);
        }

        logger.info("Python script executed successfully");
        return outputLines;
    }

    /**
     * 执行 Python 脚本并返回输出结果（简化版）
     *
     * @param scriptPath Python 脚本路径
     * @param arguments  传递给脚本的参数列表
     * @return 脚本执行输出的行列表
     * @throws IOException          如果脚本执行过程中发生 I/O 错误
     * @throws InterruptedException 如果脚本执行被中断
     */
    public List<String> executeScript(String scriptPath, List<String> arguments)
            throws IOException, InterruptedException {
        return executeScript("python", scriptPath, arguments);
    }

    /**
     * 执行 Python 脚本并返回输出结果（无参数版）
     *
     * @param scriptPath Python 脚本路径
     * @return 脚本执行输出的行列表
     * @throws IOException          如果脚本执行过程中发生 I/O 错误
     * @throws InterruptedException 如果脚本执行被中断
     */
    public List<String> executeScript(String scriptPath)
            throws IOException, InterruptedException {
        return executeScript(scriptPath, null);
    }

    /**
     * 执行 Python 脚本并返回输出结果（安全版，捕获所有异常）
     *
     * @param pythonInterpreter Python 解释器路径
     * @param scriptPath        Python 脚本路径
     * @param arguments         传递给脚本的参数列表
     * @return 脚本执行输出的行列表，如果执行失败则返回空列表
     */
    public List<String> executeScriptSafely(String pythonInterpreter, String scriptPath, List<String> arguments) {
        try {
            return executeScript(pythonInterpreter, scriptPath, arguments);
        } catch (Exception e) {
            logger.error("Failed to execute Python script: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
package org.jeecg.modules.wms.ai.chat.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.service.IAiragAppService;
import org.jeecg.modules.airag.app.vo.ChatSendParams;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/ai")
public class ChatController {


    //    @Resource(name = "chatClientOllama")
    @Resource(name = "chatClientOpenAi")
    private ChatClient chatClientOpenAi;

    @Autowired
    private IAiragAppService airagAppService;

//    @RequestMapping("/chat")
//    public String chat(String prompt) {
//        // 调用大模型
//        String response = chatClientOllama
//                .prompt(prompt)
//                .call() // 同步调用
//                .content();
//        return response;
//    }

    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")   // 加produces是为了防止乱码
    public Flux<String> chat(String prompt) {
        // 调用大模型
        Flux<String> content = chatClientOpenAi
                .prompt(prompt)
                .stream() // 流式调用
                .content();
        return content;
    }

    @RequestMapping("/chat/send")
    public SseEmitter send(@RequestBody ChatSendParams chatSendParams, HttpServletRequest httpRequest) {
        AssertUtils.assertNotEmpty("参数异常", chatSendParams);
        String userMessage = chatSendParams.getContent();
        AssertUtils.assertNotEmpty("至少发送一条消息", userMessage);

        // 获取会话信息
        String conversationId = chatSendParams.getConversationId();
        String topicId = oConvertUtils.getString(chatSendParams.getTopicId(), UUIDGenerator.generate());
        // 每次会话都生成一个新的,用来缓存emitter
        String requestId = UUIDGenerator.generate();
        SseEmitter emitter = new SseEmitter(-0L);

        AtomicBoolean isThinking = new AtomicBoolean(false);
        //系统提示词
        String systemPrompt = null;

        //当前端是通过app应用发起聊天，得到appid
        AiragApp app = null;
        String appId = chatSendParams.getAppId();
        if(StringUtils.isNotEmpty(appId)){
            app = airagAppService.getById(appId);
            systemPrompt=app.getPrompt();
        }
        Flux<String> content = null;
        if (app == null) {
            content = chatClientOpenAi
                    .prompt(userMessage)
                    .stream() // 流式调用
                    .content();
        }else {
            //将Flux对象转成SseEmitter
            // 调用大模型
            content = chatClientOpenAi
                    .prompt(userMessage)
                    .user(userMessage)
                    .system(systemPrompt)   // 系统提示词
                    .stream() // 流式调用
                    .content();
        }

        // 向前端发送两条消息
        try {
            sendMessage(emitter, conversationId, topicId, requestId, ">", "MESSAGE");
            sendMessage(emitter, conversationId, topicId, requestId, "\n> ", "MESSAGE");
        } catch (IOException e) {
            emitter.completeWithError(e);//将错误信息发送给客户端，并结束SSE连接。
       }

        content.subscribe(data -> {
            //java接收到了大模型的响应
            //开始向前端发送数据
            try {
                // 兼容推理模型
                if ("<think>".equals(data)) {
                    isThinking.set(true);
                    data = "> ";
                }
                if ("</think>".equals(data)) {
                    isThinking.set(false);
                    data = "\n\n";
                }
                if (isThinking.get()) {
                    if (null != data && data.contains("\n")) {
                        data = "\n> ";
                    }
                }
                sendMessage(emitter, conversationId, topicId, requestId, data, "MESSAGE");
            } catch (IOException e) {
                emitter.completeWithError(e);//将错误信息发送给客户端，并结束SSE连接。
            }

        }, err -> {
            emitter.completeWithError(err);//将错误信息发送给客户端，并结束SSE连接。
        }, () -> {
            // 完成处理
            try {
                // 发送两个结果
                sendEndMessage(emitter, conversationId, topicId, requestId);
                sendEndMessage(emitter, conversationId, topicId, requestId);
            } catch (IOException e) {
                emitter.completeWithError(e);//将错误信息发送给客户端，并结束SSE连接。
            }
        });
        return emitter;
    }


    // 辅助方法：向客户端发送消息
    private void sendMessage(SseEmitter emitter, String conversationId, String topicId,
                             String requestId, String message, String event) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("conversationId", conversationId);
        response.put("topicId", topicId);
        response.put("requestId", requestId);
        response.put("event", event);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message);
        response.put("data", messageData);

        String jsonData = JSONObject.toJSONString(response);
        emitter.send(SseEmitter.event().data(jsonData));
    }

    // 辅助方法：发送结束消息
    private void sendEndMessage(SseEmitter emitter, String conversationId, String topicId,
                                String requestId) throws IOException {
        Map<String, Object> endResponse = new HashMap<>();
        endResponse.put("event", "MESSAGE_END");
        endResponse.put("flowId", null);
        endResponse.put("requestId", requestId);
        endResponse.put("conversationId", conversationId);
        endResponse.put("topicId", topicId);
        endResponse.put("data", null);

        String endJson = JSONObject.toJSONString(endResponse);
        emitter.send(SseEmitter.event().data(endJson));
    }
}
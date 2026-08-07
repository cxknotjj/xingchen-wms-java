package org.jeecg.modules.wms.ai.chat.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mchange.v1.util.ListUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.*;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.service.IAiragAppService;
import org.jeecg.modules.airag.app.vo.ChatSendParams;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.config.RedisChatMemory;
import org.jeecg.modules.wms.ai.chat.tools.WmsInventoryTools;
import org.jeecg.modules.wms.ai.chat.util.VectorDistanceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@Slf4j
@RequestMapping("/ai")
public class ChatController {


    //    @Resource(name = "chatClientOllama")
    @Resource(name = "chatClientOpenAi")
    private ChatClient chatClientOpenAi;

    @Autowired
    private WmsInventoryTools wmsInventoryTools;

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

    /*
    *获取questionAnswerAdvisor 对象
    * @param knowIds 知识库ids
        @return
    */
    private QuestionAnswerAdvisor getQuestionAnswerAdvisor(List<String> knowIds) {
        // 拼接knowIds，将knowid拼接成 knowledgeId=='1958811713520881665' || knowledgeId=='1958811713520881666'
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = knowIds.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append("knowledgeId=='" + iterator.next()).append("'");
            if (iterator.hasNext()) {
                stringBuilder.append(" || ");
            }
        }

        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(
                pgVectorStore,// 向量库
                SearchRequest.builder()//向量检索的请求参数
                        .similarityThreshold(0.5d) //相似度阈值
                        .filterExpression(stringBuilder.toString())
                        .topK(2) //返回的文档片段数量
                        .build());
        return questionAnswerAdvisor;
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
        // 获取用户名称
        String username = getUsername(httpRequest);
        chatSendParams.setUsername(username);
        // 将chatsendparams转换为json字符串
        String json = JSONObject.toJSONString(chatSendParams);

        AtomicBoolean isThinking = new AtomicBoolean(false);
        //系统提示词
        String systemPrompt = null;

        //当前端是通过app应用发起聊天，得到appid
        AiragApp app = null;
        List<String> knowIds = null;
        String appId = chatSendParams.getAppId();
        if (StringUtils.isNotEmpty(appId)) {
            app = airagAppService.getById(appId);
            systemPrompt = app.getPrompt();
            // 获取知识库id
            knowIds = app.getKnowIds();
        }




        Flux<String> content = null;
        if (app == null) {
            content = chatClientOpenAi
                    .prompt(userMessage)
                    .user(userMessage)
                    .system(systemPrompt)   // 系统提示词
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, json)) // 会话管理
                    .stream() // 流式调用
                    .content();
        } else {
            //将Flux对象转成SseEmitter
            // 如果knowIds不为空
            if (knowIds != null && knowIds.size() > 0) {
                // 获取questionAnswerAdvisor 对象
                QuestionAnswerAdvisor questionAnswerAdvisor = getQuestionAnswerAdvisor(knowIds);
                // 调用大模型
                content = chatClientOpenAi
                        .prompt(userMessage)
                        .user(userMessage)
                        .system(systemPrompt)   // 系统提示词
                        .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, json))
                        .advisors(questionAnswerAdvisor) // 知识库检索
                        .tools(wmsInventoryTools)
                        .stream() // 流式调用
                        .content();
            } else {
                content = chatClientOpenAi
                        .prompt(userMessage)
                        .user(userMessage)
                        .system(systemPrompt)   // 系统提示词
                        .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, json)) // 会话管理
                        .tools(wmsInventoryTools)
                        .stream() // 流式调用
                        .content();
            }

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

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ISysBaseAPI sysBaseApi;

    private String getUsername(HttpServletRequest httpRequest) {
        try {
            TokenUtils.getTokenByRequest();
            String token;
            if (null != httpRequest) {
                token = TokenUtils.getTokenByRequest(httpRequest);
            } else {
                token = TokenUtils.getTokenByRequest();
            }
            if (TokenUtils.verifyToken(token, sysBaseApi, redisUtil)) {
                return JwtUtil.getUsername(token);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Resource
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    // 加载指定的资源文件
//    @Value("classpath:1.txt")
//    private org.springframework.core.io.Resource resource;
//    @Value("classpath:2.txt")
//    private org.springframework.core.io.Resource resource2;

    @Resource
    private VectorStore pgVectorStore;

    @GetMapping(value = "/embedtest")
    public void embedtest(String query, String t1, String t2, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        // 先将查询文本向量化
        float[] queryVector = openAiEmbeddingModel.embed(query);
        // 读取文本文件
//        TextReader textReader = new TextReader(this.resource);
        // 读取文本文件
//        TextReader textReader2 = new TextReader(this.resource2);
        // 获取Document对象

        Document document = new Document(t1);
        Document document2 = new Document(t2);
        // 将两个文件加入list
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        documents.add(document2);
        // 借助向量模型先生成向量
        pgVectorStore.add(documents);
        // 向量化处理
        for (Document item : documents) {
            writer.println("--------------------------------");
            //原始文本
            writer.println(item.getText());
            //转为向量
            float[] embed = openAiEmbeddingModel.embed(item);
            // 打印向量化后的数据
            System.out.println(Arrays.toString(embed));
            //比较欧氏距离
            writer.println("比较欧氏距离" + VectorDistanceUtils.euclideanDistance(queryVector, embed));
            //余弦距离
            writer.println("余弦距离" + VectorDistanceUtils.cosineDistance(queryVector, embed));
        }

    }
}
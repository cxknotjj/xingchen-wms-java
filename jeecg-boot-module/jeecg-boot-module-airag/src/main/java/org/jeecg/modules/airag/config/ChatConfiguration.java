package org.jeecg.modules.airag.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class ChatConfiguration {

    // 注意参数中的model就是使用的模型，这里用了Ollama
    @Bean
    public ChatClient chatClientOllama(OllamaChatModel model) {
        return ChatClient.builder(model) // 创建ChatClient工厂
                .build(); // 构建ChatClient实例

    }
    @Bean           // 通过spirngboot框架自动注入ChatMemory实例，因为chatMemory容器已经通过@Component注册为了一个bean
    public ChatClient chatClientOpenAi(OpenAiChatModel model, ChatMemory memory) {
        return ChatClient
                .builder(model)
                // advisor作为一个中转站，大模型输出的结果和java后端发送向大模型的结果都会被拦截记录，
                // 因此可以很容易的实现日志记录以及会话记忆，而advisor是通过aop思想实现的
                .defaultAdvisors(new SimpleLoggerAdvisor()) // 日志记录
                .defaultAdvisors(new MessageChatMemoryAdvisor(memory)) // 会话记忆
                .build();
    }

    //spring ai提供，使用大模型，生成向量，将向量保存到数据库
    @Bean
    public VectorStore pgVectorStore(
            @Qualifier("vectorJdbcTemplate") JdbcTemplate jdbcTemplate,
            @Qualifier("openAiEmbeddingModel") OpenAiEmbeddingModel openAiEmbeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, openAiEmbeddingModel)
                .dimensions(768)  // 与嵌入模型维度对齐
                .distanceType(COSINE_DISTANCE)  // 余弦相似度计算
                .schemaName("public")// 指定表所属的schema
                .vectorTableName("vector_store")// 存储向量的表名
                .indexType(HNSW)  // 高效近似最近邻搜索
                .initializeSchema(false)  // 自动初始化表结构
                .idType(PgVectorStore.PgIdType.UUID)
                .build();
    }

    //用来访问postgresql
    @Bean("vectorJdbcTemplate")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        DataSource postgresql = ds.getDataSource("postgresql");
        return new JdbcTemplate(postgresql);
    }
}

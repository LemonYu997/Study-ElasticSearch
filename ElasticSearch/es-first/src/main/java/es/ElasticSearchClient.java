package es;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

public class ElasticSearchClient {

    private TransportClient client;

    @Before
    public void init() throws Exception {
        //1. 创建一个settings对象，相当于一个配置信息，主要配置集群的名称（put方法）
        Settings settings = Settings.builder().put("cluster.name", "my-elasticsearch").build();

        //2. 创建客户端client对象
        client = new PreBuiltTransportClient(settings);
        //指定集群节点    IP地址和端口号，这里的端口号应该用外部访问的集群端口号
        //为了保证高可用，每个端口号都设置以下
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9300));
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9301));
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9302));

    }

    @Test
    public void createIndex() throws Exception {
        //3. 使用client对象创建一个索引库
        //创建名为index_hello的索引库 客户端.管理员.索引.准备创建.get执行操作
        client.admin().indices().prepareCreate("index_hello")   //设置参数
                .get();     //执行操作
        //4. 关闭client对象
        client.close();
    }

    @Test
    public void setMappings() throws Exception {
    //3、创建一个mappings信息
        /*{
            "article": {
            "properties": {
                "id": {
                    "type": "long",
                    "store": true
                },
                "title": {
                    "type": "text",
                    "store": true,
                    "analyzer": "ik_smart"
                },
                "content": {
                    "type": "text",
                    "store": true,
                    "analyzer": "ik_smart"
                }
            }
        }*/
        //使用XContentBuilder类创建mappings对象
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("article")
                        .startObject("properties")
                            .startObject("id")
                                .field("type", "long")
                                .field("store", true)
                            .endObject()
                            .startObject("title")
                                .field("type", "text")
                                .field("store", true)
                                .field("analyzer", "ik_smart")
                            .endObject()
                            .startObject("content")
                                .field("type", "text")
                                .field("store", true)
                                .field("analyzer", "ik_smart")
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject();
        //4、使用客户端将mapping信息设置到索引库中
        client.admin().indices()
                .preparePutMapping("index_hello")   //设置要执行的索引名称
                .setType("article")     //设置要执行的type
                .setSource(builder)     //设置mapping信息，可以是XContentBuilder对象，也可以是json字符串
                .get();                 //执行
        //5、关闭连接
        client.close();
    }

    @Test
    public void testAddDocument() throws Exception {
        //创建一个client对象
        //创建一个文档对象
        XContentBuilder builder = new XContentFactory().jsonBuilder()
                .startObject()
                    .field("id", 2L)
                    .field("title", "使用java创建的文档22222")
                    .field("content", "222222")
                .endObject();
        //使用client，把文档对象添加到索引库
        client.prepareIndex("index_hello", "article", "2")  //设置索引库、type、文档id
                .setSource(builder)     //添加文档
                .get();                 //执行
        //关闭client
        client.close();
    }

    @Test
    public void testAddDocument2() throws Exception {
        //创建article对象
        Article article = new Article();
        //设置对象属性
        article.setId(3L);
        article.setTitle("使用jackson创建地文档");
        article.setContent("这是一个内容测试");
        //把article对象转换成json格式字符串，使用Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonDocument = objectMapper.writeValueAsString(article);
        //使用client对象把文档写入索引库
        client.prepareIndex("index_hello", "article", "3")  //设置索引库、type、文档id
                .setSource(jsonDocument, XContentType.JSON)     //添加Json文档
                .get();                                         //执行
        //关闭客户端
        client.close();
    }

    @Test
    public void testAddDocument3() throws Exception {
        //批量添加文档
        for (int i = 4; i < 100; i++) {
            //创建article对象
            Article article = new Article();
            //设置对象属性
            article.setId(i);
            article.setTitle(i + "批量创建文档" + i);
            article.setContent(i + "批量生成内容" + i);
            //把article对象转换成json格式字符串，使用Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = objectMapper.writeValueAsString(article);
            //使用client对象把文档写入索引库    id需要字符串形式，所以加一个""
            client.prepareIndex("index_hello", "article", i + "")  //设置索引库、type、文档id
                    .setSource(jsonDocument, XContentType.JSON)     //添加Json文档
                    .get();                                         //执行
        }

        //关闭客户端
        client.close();
    }
}


package es;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

public class SearchIndex {

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

    //封装查询方法
    private void search(QueryBuilder queryBuilder) throws Exception {
        //执行查询
        SearchResponse searchResponse = client
                .prepareSearch("index_hello")   //查询的索引
                .setTypes("article")                    //查询的type
                .setQuery(queryBuilder)                 //查询的方式
                //设置分页信息
                .setFrom(0)                             //分页起使号
                .setSize(5)                             //每页显示多少条数据
                .get();                                 //执行查询
        //取查询结果
        SearchHits searchHits = searchResponse.getHits();
        //取查询结果总记录数
        System.out.println("查询结果总记录数：" + searchHits.getTotalHits());
        //查询结果列表，用迭代器形式
        Iterator<SearchHit> iterator = searchHits.iterator();
        //遍历迭代器
        while(iterator.hasNext()) {
            //取值，为文档对象
            SearchHit searchHit = iterator.next();
            //以string格式输出文档对象
            System.out.println(searchHit.getSourceAsString());
            //取文档的属性，单个属性取值
            System.out.println("-------文档的属性--------");
            Map<String, Object> document = searchHit.getSourceAsMap();
            System.out.println("id: " + document.get("id"));
            System.out.println("title: " + document.get("title"));
            System.out.println("content: " + document.get("content"));
        }
        //关闭client
        client.close();
    }

    @Test
    public void testSearchById() throws Exception {
        //创建查询对象  _id为1和2的文档
        QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds("1", "2");
        //执行查询
        search(queryBuilder);
    }

    @Test
    public void testQueryByTerm() throws Exception {
        //创建一个QueryBuilder对象
        //参数1：要搜索的字段
        //参数2：要搜索的关键词
        QueryBuilder queryBuilder = QueryBuilders.termQuery("title", "java");
        //执行查询
        search(queryBuilder);
    }

    @Test
    public void testQueryStringQuery() throws Exception {
        //创建一个QueryBuilder对象
        //会自动分词，根据分词结果查询
        QueryBuilder queryBuilder = QueryBuilders
                .queryStringQuery("使用和废弃")   //要分词的字符串
                .defaultField("title");          //默认搜索域，不指定会在所有域上查询
        //执行查询
        search(queryBuilder);
    }

    @Test
    public void testQueryByPage() throws Exception {
        //分页设置在search方法中的client对象里
        QueryBuilder queryBuilder = QueryBuilders
                .queryStringQuery("批量")         //要分词的字符串
                .defaultField("title");          //默认搜索域，不指定会在所有域上查询
        //执行查询
        search(queryBuilder);
    }


    //封装查询方法，高亮结果
    //highlightField为高亮显示的字段
    private void search(QueryBuilder queryBuilder, String highlightField) throws Exception {
        //高亮对象
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮显示的字段
        highlightBuilder.field(highlightField);
        //高亮显示的前缀
        highlightBuilder.preTags("<em>");
        //高亮显示的后缀
        highlightBuilder.postTags("</em>");

        //执行查询
        SearchResponse searchResponse = client
                .prepareSearch("index_hello")   //查询的索引
                .setTypes("article")                    //查询的type
                .setQuery(queryBuilder)                 //查询的方式
                //设置分页信息
                .setFrom(0)                             //分页起使号
                .setSize(5)                             //每页显示多少条数据
                //设置高亮信息
                .highlighter(highlightBuilder)
                .get();                                 //执行查询

        //取查询结果
        SearchHits searchHits = searchResponse.getHits();
        //取查询结果总记录数
        System.out.println("查询结果总记录数：" + searchHits.getTotalHits());
        //查询结果列表，用迭代器形式
        Iterator<SearchHit> iterator = searchHits.iterator();
        //遍历迭代器
        while(iterator.hasNext()) {
            //取值，为文档对象
            SearchHit searchHit = iterator.next();
            //以string格式输出文档对象
            System.out.println(searchHit.getSourceAsString());
            //取文档的属性，单个属性取值
            System.out.println("-------文档的属性--------");
            Map<String, Object> document = searchHit.getSourceAsMap();
            System.out.println("id: " + document.get("id"));
            System.out.println("title: " + document.get("title"));
            System.out.println("content: " + document.get("content"));
            //输出高亮结果
            System.out.println("***********高亮结果***********");
            //key为字段名，value为fragments即高亮结果
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            System.out.println(highlightFields);
            //取title高亮显示的结果
            HighlightField value = highlightFields.get(highlightField);
            Text[] fragments = value.getFragments();
            if (fragments != null) {
                //内容
                String s = fragments[0].toString();
                System.out.println(s);
            }
        }

        //关闭client
        client.close();
    }

    @Test
    public void testQueryHighlight() throws Exception {
        //分页设置在search方法中的client对象里
        QueryBuilder queryBuilder = QueryBuilders
                .queryStringQuery("批量")         //要分词的字符串
                .defaultField("title");          //默认搜索域，不指定会在所有域上查询
        //执行高亮查询，参数2为高亮显示字段
        search(queryBuilder, "title");
    }
}

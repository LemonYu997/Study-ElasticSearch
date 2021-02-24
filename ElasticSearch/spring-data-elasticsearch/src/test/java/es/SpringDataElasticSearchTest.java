package es;

import es.entity.Article;
import es.repositories.ArticleRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

//SpringBoot测试类
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringDataElasticSearchTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ElasticsearchTemplate template;

    //创建索引
    @Test
    public void createIndex() {
        //创建索引，并配置映射关系
        template.createIndex(Article.class);
        //如果没有配置映射关系，可以手动配置映射关系
        //template.putMapping(Article.class);
    }

    //添加文档
    @Test
    public void addDocument() {
        //创建一个Article对象
        Article article = new Article();
        article.setId(2L);
        article.setTitle("222使用spring-data创建的文档");
        article.setContent("2222使用spring-data添加的内容");
        //把文档写入索引库
        articleRepository.save(article);
    }

    //删除文档
    @Test
    public void deleteDocumentById() {
        //删除对应id的文档，Id类型为Long
        articleRepository.deleteById(2L);
        //全部删除
        //articleRepository.deleteAll();
    }

    //更新文档
    @Test
    public void updateDocument() {
        //创建一个Article对象
        Article article = new Article();
        article.setId(2L);
        article.setTitle("更新，使用spring-data创建的文档");
        article.setContent("更新，使用spring-data更新的内容");
        //把文档写入索引库
        articleRepository.save(article);
    }

    //批量添加文档
    @Test
    public void addDocuments() {
        Article article = new Article();
        for (int i = 2; i < 12; i++) {
            article.setId(i);
            article.setTitle(i+ "批量文档" + i);
            article.setContent(i + "批量添加的文档内容" + i);
            //把文档写入索引库
            articleRepository.save(article);
        }
    }

    //简单查询，查询全部
    @Test
    public void findAll() {
        Iterable<Article> articles = articleRepository.findAll();
        //迭代器，输出每一个元素
        articles.forEach(a -> System.out.println(a));
    }

    //简单查询，根据Id查询
    @Test
    public void findById() {
        Optional<Article> optional = articleRepository.findById(1L);
        //获得article对象
        Article article = optional.get();
        System.out.println(article);
    }

    //自定义查询，根据title
    @Test
    public void testFindByTitle() {
        //根据title查询，关键词为"测试"
        List<Article> articles = articleRepository.findByTitle("测试");
        for (Article article : articles) {
            System.out.println(article);
        }
    }

    //自定义查询，根据title和content
    @Test
    public void testFindByTitleOrContent() {
        //设置分页，从0开始，15条数据
        Pageable pageable = PageRequest.of(0 ,15);
        //title中含有"测试"或者content中含有"批量"
        List<Article> articles = articleRepository.findByTitleOrContent("测试", "批量", pageable);
        for (Article article : articles) {
            System.out.println(article);
        }
    }

    //原生查询
    @Test
    public void testNativeSearchQuery() {
        //创建一个查询对象
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                //设置查询条件
                .withQuery(QueryBuilders.queryStringQuery("如果包含测试这个词").defaultField("title"))
                //设置分页信息
                .withPageable(PageRequest.of(0, 15))
                //创建对象
                .build();
        //执行查询
        List<Article> articles = template.queryForList(query, Article.class);
        for (Article article : articles) {
            System.out.println(article);
        }
    }
}

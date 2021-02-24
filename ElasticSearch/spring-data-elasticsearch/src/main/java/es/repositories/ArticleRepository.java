package es.repositories;

import es.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

//<Article, Long>   对Article进行操作，主键是Long类型
//方法不用定义，常用方法在父类中已经定义好了
public interface ArticleRepository extends ElasticsearchRepository<Article, Long> {
    //自定义方法查询，不需要实现，只需要根据命名规范命名

    //根据标题查询
    List<Article> findByTitle(String title);

    //根据title或content查询
    List<Article> findByTitleOrContent(String title, String content);

    //设置分页信息
    List<Article> findByTitleOrContent(String title, String content, Pageable pageable);
}

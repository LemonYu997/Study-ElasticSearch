package es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

//lombok相关设置，自动生成get/set/构造/toString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ToString
//映射的文档  设置索引和type
@Document(indexName = "data_blog", type = "article")
public class Article {

    @Id
    //设置当前字段    字段类型        是否存储    分析器
    @Field(type = FieldType.Long, store = true)
    private long id;
    @Field(type = FieldType.Text, store = true, analyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.Text, store = true, analyzer = "ik_smart")
    private String content;
}

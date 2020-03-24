package com.xiaotiao.elasticsearch;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class Book implements Serializable {

    private static final long serialVersionUID = 3998259515618365475L;

    private String id;
    private String author;
    private String title;
    private Integer word_count;
    private String publish_date;

    //复杂查询
    private Integer gt_word_count;
    private Integer lt_word_count;
}

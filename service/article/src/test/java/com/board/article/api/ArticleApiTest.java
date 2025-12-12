package com.board.article.api;

import com.board.article.service.response.ArticlePageResponse;
import com.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class ArticleApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse articleResponse = create(new ArticleCreateRequest(
                "hi", "my content", 256342627421904896L, 256342627421904896L
        ));
        System.out.println("articleResponse.toString() : " + articleResponse.toString());
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }
    
    @Test
    void readTest() {
        ArticleResponse response = read(256342627421904896L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }
    

    @Test
    void updateTest() {
        update(256342627421904896L);
        ArticleResponse response = read(256342627421904896L);
        System.out.println("response = " + response);
    }

    void update(Long articleId) {
        restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("hi 2", "my content 2"))
                .retrieve();
    }


    @Test
    void deleteTest() {
        delete(256342627421904896L);
    }

    void delete(Long articleId) {
        restClient.delete()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&pageSize=30&page=50000")
                .retrieve()
                .body(ArticlePageResponse.class);

        System.out.println("response.getArticleCount() = " + response.getArticleCount());

        for(ArticleResponse article : response.getArticles()) {
            System.out.println("articleId = " + article.getArticleId());
        }

    }



    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {

        private String title;
        private String content;
        private Long boardId;
        private Long writerId;

    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {

        private String title;
        private String content;

    }

}

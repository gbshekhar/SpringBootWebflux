package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    static String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList);
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    //Junit Test for
    @Test
    public void addReview(){
        //given - precondition or setup
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when  & then
         webTestClient.post()
                 .uri(REVIEWS_URL)
                 .bodyValue(review)
                 .exchange()
                 .expectStatus()
                 .isCreated()
                 .expectBody(Review.class)
                 .consumeWith(reviewEntityExchangeResult -> {
                     var savedReview =reviewEntityExchangeResult.getResponseBody();
                     assert savedReview != null;
                     assert savedReview.getReviewId() != null;
                 });
    }
}

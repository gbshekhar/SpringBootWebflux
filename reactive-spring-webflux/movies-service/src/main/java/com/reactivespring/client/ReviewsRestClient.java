package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewsRestClient {

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    private WebClient webClient;

    public ReviewsRestClient(WebClient webClient){
        this.webClient = webClient;
    }

    public Flux<Review> getReviewsByMovieId(String movieId){
        var url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                        .queryParam("movieInfoId", movieId)
                                .build().toUriString();
        return   webClient.get()
                  .uri(url)
                  .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.error("Status Code : {}", clientResponse.statusCode().value());
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)){
                        return Mono.error(new ReviewsClientException(
                                "There is no MovieInfo Available for the passed in Id : "+movieId,
                                clientResponse.statusCode().value()
                        ));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(responseMessage,
                                    clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.error("Status Code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException(
                                    "Server Exception in MovieInfoService : "+ responseMessage)));
                })
                  .bodyToFlux(Review.class);
    }
}

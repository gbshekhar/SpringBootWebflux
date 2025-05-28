package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {

    ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest serverRequest) {
         return serverRequest.bodyToMono(Review.class)
                .flatMap(review -> {
                    return  reviewReactiveRepository.save(review);
                })
                 .flatMap(savedReview -> {
                     return ServerResponse.status(HttpStatus.CREATED)
                             .bodyValue(savedReview);
                 });
    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var movieInfoId = serverRequest.queryParam("movieInfoId");
        if(movieInfoId.isPresent()){
            var reviewsFlux = reviewReactiveRepository.findReviewsByMovieInfoId((Long.valueOf(movieInfoId.get())));
            return ServerResponse.ok().body(reviewsFlux, Review.class);
        } else{
            var reviewFlux = reviewReactiveRepository.findAll();
            return ServerResponse.ok().body(reviewFlux, Review.class);
        }

    }

    public Mono<ServerResponse> updateReviews(ServerRequest serverRequest) {
        var reviewId = serverRequest.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
        return  existingReview
                .flatMap(review ->
                    serverRequest.bodyToMono(Review.class)
                            .map(reqReview -> {
                                review.setComment(reqReview.getComment());
                                review.setRating(reqReview.getRating());
                                return  review;
                            })
                            .flatMap(reviewReactiveRepository::save)
                            .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
        var reviewId = serverRequest.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
        return existingReview.flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }
}

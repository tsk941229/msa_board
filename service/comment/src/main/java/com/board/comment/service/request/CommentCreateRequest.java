package com.board.comment.service.request;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class CommentCreateRequest {

    private Long articleId;
    private String content;
    private Long parentCommentId;
    private Long writerId;

}

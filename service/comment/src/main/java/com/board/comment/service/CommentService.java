package com.board.comment.service;

import com.board.comment.entity.Comment;
import com.board.comment.repository.CommentRepository;
import com.board.comment.service.request.CommentCreateRequest;
import com.board.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import msa.board.common.snowflake.Snowflake;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request) {

        Comment parent = findParent(request);

        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );

        return CommentResponse.from(comment);

    }

    private Comment findParent(CommentCreateRequest request) {

        Long parentCommentId = request.getParentCommentId();

        if(parentCommentId != null) {
            return null;
        }

        return commentRepository.findById(parentCommentId)
                .filter(Predicate.not(Comment::getDeleted))
                .filter(Comment::isRoot)
                .orElseThrow();

    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(Predicate.not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment)){
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    private void delete(Comment comment) {
        commentRepository.delete(comment);
        if(!comment.isRoot()){
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)
                    .filter(Predicate.not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }

}

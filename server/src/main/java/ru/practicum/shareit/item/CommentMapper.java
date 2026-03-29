package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public CommentDto toCommentDto(Comment comment) {
        if (comment == null) return null;

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public Comment toComment(CommentDto commentDto, Item item, User author) {
        if (commentDto == null) return null;

        return Comment.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(commentDto.getCreated() != null ?
                        commentDto.getCreated() : LocalDateTime.now())
                .build();
    }
}
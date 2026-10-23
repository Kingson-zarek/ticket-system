package com.hnty.ticketsystem.controller;

import com.hnty.ticketsystem.common.Result;
import com.hnty.ticketsystem.dto.CommentCreateRequest;
import com.hnty.ticketsystem.service.CommentService;
import com.hnty.ticketsystem.vo.CommentVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工单评论接口
 *
 * @author Crown
 */
@RestController
@RequestMapping("/api/tickets")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{ticketId}/comments")
    public Result<List<CommentVo>> getComments(@PathVariable Long ticketId) {
        return Result.ok(commentService.getComments(ticketId));
    }

    @PostMapping("/{ticketId}/comments")
    public Result<Void> addComment(@PathVariable Long ticketId,
                                   @Valid @RequestBody CommentCreateRequest request) {
        commentService.addComment(ticketId, request.getContent());
        return Result.ok();
    }

}

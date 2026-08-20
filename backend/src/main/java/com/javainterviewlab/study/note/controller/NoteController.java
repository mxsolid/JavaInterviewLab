package com.javainterviewlab.study.note.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.common.content.ContentTargetType;
import com.javainterviewlab.study.note.dto.CreateNoteRequest;
import com.javainterviewlab.study.note.dto.NoteResponse;
import com.javainterviewlab.study.note.dto.SaveNoteRequest;
import com.javainterviewlab.study.note.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 笔记 HTTP 边界；乐观锁和目标校验由 Service 统一执行。 */
@Tag(name = "学习笔记", description = "内容笔记与乐观锁保存")
@RestController
@RequestMapping({"/api/study/notes", "/api/v1/study/notes"})
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /** 读取一个内容目标的当前笔记。 */
    @Operation(summary = "读取笔记")
    @GetMapping
    public ApiResponse<NoteResponse> find(
            @RequestParam ContentTargetType targetType,
            @RequestParam Long targetId
    ) {
        return ApiResponse.success(noteService.find(targetType, targetId));
    }

    /** 首次创建一个内容笔记。 */
    @Operation(summary = "创建笔记")
    @PostMapping
    public ApiResponse<NoteResponse> create(@Valid @RequestBody CreateNoteRequest request) {
        return ApiResponse.success(noteService.create(request));
    }

    /** 带版本更新已有笔记。 */
    @Operation(summary = "保存笔记")
    @PutMapping("/{id}")
    public ApiResponse<NoteResponse> update(@PathVariable Long id, @Valid @RequestBody SaveNoteRequest request) {
        return ApiResponse.success(noteService.update(id, request));
    }
}

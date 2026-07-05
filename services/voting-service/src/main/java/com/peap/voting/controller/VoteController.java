package com.peap.voting.controller;

import com.peap.voting.dto.CastVoteRequest;
import com.peap.voting.dto.ScoreResponse;
import com.peap.voting.dto.VoteResponse;
import com.peap.voting.service.VoteService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public ResponseEntity<VoteResponse> castVote(@Valid @RequestBody CastVoteRequest request) {
        return ResponseEntity.ok(VoteResponse.from(voteService.castVote(request)));
    }

    @GetMapping("/entity/{entityId}/score")
    public ResponseEntity<ScoreResponse> getScore(@PathVariable UUID entityId) {
        return ResponseEntity.ok(voteService.getScore(entityId));
    }
}

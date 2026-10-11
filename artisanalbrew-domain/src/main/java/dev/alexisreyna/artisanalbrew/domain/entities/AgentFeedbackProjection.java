package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code AgentFeedbackProjection.cs}. */
public class AgentFeedbackProjection {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String registryAddress = "";
    private long agentId;
    private long jobId;
    private String reviewerAddress = "";
    private long score;
    private String commentUri = "";
    private Instant createdAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getRegistryAddress() { return registryAddress; }
    public void setRegistryAddress(String v) { this.registryAddress = v; }
    public long getAgentId() { return agentId; }
    public void setAgentId(long v) { this.agentId = v; }
    public long getJobId() { return jobId; }
    public void setJobId(long v) { this.jobId = v; }
    public String getReviewerAddress() { return reviewerAddress; }
    public void setReviewerAddress(String v) { this.reviewerAddress = v; }
    public long getScore() { return score; }
    public void setScore(long v) { this.score = v; }
    public String getCommentUri() { return commentUri; }
    public void setCommentUri(String v) { this.commentUri = v; }
    public Instant getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(Instant v) { this.createdAtUtc = v; }
}

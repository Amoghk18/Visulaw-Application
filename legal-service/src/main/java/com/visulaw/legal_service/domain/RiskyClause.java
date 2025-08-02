package com.visulaw.legal_service.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskyClause {
    private String clause;
    private String tag;
    private String confidence;
    private String startIndex;
    private String endIndex;
    private String riskScore;
}

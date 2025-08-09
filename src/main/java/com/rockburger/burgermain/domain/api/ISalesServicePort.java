package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.SalesSummaryModel;
import java.time.LocalDate;

public interface ISalesServicePort {
    SalesSummaryModel getDailySummary(LocalDate date);
}

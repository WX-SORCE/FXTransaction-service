package com.alxy.marketanalysisservice.Repository;

import com.alxy.marketanalysisservice.Entity.CurrencyRealtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRealtimeRepository extends JpaRepository<CurrencyRealtime, String> {
}

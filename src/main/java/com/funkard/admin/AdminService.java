package com.funkard.admin;

import com.funkard.admin.dto.PendingItemDTO;
import com.funkard.market.trend.Trend;
import com.funkard.market.trend.TrendRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final TrendRepository trendRepo;

    public AdminService(TrendRepository trendRepo) {
        this.trendRepo = trendRepo;
    }

    public List<PendingItemDTO> getPendingItems() {
        List<Trend> all = trendRepo.findAll();
        List<PendingItemDTO> pending = new ArrayList<>();

        for (Trend t : all) {
            if (t.isManualCheck() || (t.getDataPoints() == null || t.getDataPoints().length() < 10)) {
                PendingItemDTO dto = new PendingItemDTO();
                dto.itemName = t.getItemName();
                dto.category = t.getCategory();
                dto.rangeType = t.getRangeType();
                dto.updatedAt = t.getUpdatedAt();
                dto.source = t.getSource();
                pending.add(dto);
            }
        }
        return pending;
    }
}

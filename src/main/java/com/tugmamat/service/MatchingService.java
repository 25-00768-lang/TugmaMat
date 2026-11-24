package com.tugmamat.service;

import com.tugmamat.model.Enums.Status;
import com.tugmamat.model.Locations;
import com.tugmamat.model.Material;
import com.tugmamat.model.MaterialRequest;
import com.tugmamat.repo.MaterialRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Compatibility Matching System.
 *
 * 100 points in total:
 *   Category 20 | Name/type 20 | Size 20 | Quantity 20 | Condition 10 | Location 10
 *
 * score >= 70  -> "Close match"
 * 40 - 69      -> "Alternative" (Alternative Material Suggestion)
 * < 40         -> not shown
 */
@Service
public class MatchingService {
    public static final int EXACT_THRESHOLD = 70;
    public static final int MIN_THRESHOLD = 40;

    private final MaterialRepository materials;

    public MatchingService(MaterialRepository materials) {
        this.materials = materials;
    }

    public List<MatchResult> match(MaterialRequest r) {
        List<MatchResult> out = new ArrayList<>();
        for (Material m : materials.findByStatusOrderByCreatedAtDesc(Status.AVAILABLE)) {
            if (m.owner != null && r.requester != null && m.owner.id.equals(r.requester.id)) continue;
            MatchResult res = score(m, r);
            if (res.score >= MIN_THRESHOLD) out.add(res);
        }
        out.sort((a, b) -> Integer.compare(b.score, a.score));
        return out;
    }

    public MatchResult score(Material m, MaterialRequest r) {
        MatchResult res = new MatchResult();
        res.material = m;
        int total = 0;

        // 1. Category (20)
        if (m.category == r.category) {
            total += 20;
            res.met.add("Same category: " + m.category.getLabel());
        } else {
            res.unmet.add("Different category (" + m.category.getLabel() + "; looking for: " + r.category.getLabel() + ")");
        }

        // 2. Name / type (20)
        double sim = nameSimilarity(m.name, m.description, r.name);
        total += (int) Math.round(sim * 20);
        if (sim >= 0.5) res.met.add("Name/type matches: \"" + m.name + "\"");
        else res.unmet.add("Name/type is not a strong match (looking for: \"" + r.name + "\")");

        // 3. Size (20)
        double lenRatio = dimRatio("Length", m.lengthCm, r.lengthCm, res);
        double widRatio = dimRatio("Width", m.widthCm, r.widthCm, res);
        double sizePts;
        if (lenRatio < 0 && widRatio < 0) {
            sizePts = 20;
            res.met.add("No specific size requested");
        } else {
            double sum = 0; int n = 0;
            if (lenRatio >= 0) { sum += lenRatio; n++; }
            if (widRatio >= 0) { sum += widRatio; n++; }
            sizePts = (sum / n) * 20;
        }
        total += (int) Math.round(sizePts);

        // 4. Quantity (20)
        int need = Math.max(r.quantity, 1);
        double qRatio = Math.min((double) m.quantity / need, 1.0);
        total += (int) Math.round(qRatio * 20);
        if (qRatio >= 1.0) res.met.add("Enough quantity (" + m.quantity + " available, " + need + " needed)");
        else res.unmet.add("Not enough quantity: only " + m.quantity + " available, " + need + " needed");

        // 5. Condition (10) - lower ordinal = better
        int diff = m.matCondition.ordinal() - r.desiredCondition.ordinal();
        if (diff <= 0) {
            total += 10;
            res.met.add("Condition meets the requirement (" + m.matCondition.getLabel() + ")");
        } else {
            total += Math.max(0, 10 - diff * 4);
            res.unmet.add("Lower condition: " + m.matCondition.getLabel()
                    + " (looking for: " + r.desiredCondition.getLabel() + " or better)");
        }

        // 6. Location (10)
        String want = r.location == null ? "" : r.location.trim().toLowerCase();
        String have = m.location == null ? "" : m.location.trim().toLowerCase();
        if (want.isEmpty() || Locations.isAll(r.location) || Locations.isAll(m.location)) {
            total += 10;
            res.met.add("Any location in Batangas");
        } else if (!have.isEmpty() && (have.contains(want) || want.contains(have))) {
            total += 10;
            res.met.add("Nearby / same location: " + m.location);
        } else {
            res.unmet.add("Different location (" + (m.location == null || m.location.isBlank() ? "not specified" : m.location) + ")");
        }

        res.score = Math.min(total, 100);
        return res;
    }

    /** What fraction of the requested words appear in the listing name or description. */
    private double nameSimilarity(String listingName, String desc, String requestName) {
        String hay = (listingName + " " + (desc == null ? "" : desc)).toLowerCase();
        int total = 0, found = 0;
        for (String t : requestName.toLowerCase().split("[^\\p{L}\\p{N}]+")) {
            if (t.length() < 2) continue;
            total++;
            if (hay.contains(t)) found++;
        }
        return total == 0 ? 0 : (double) found / total;
    }

    /** Returns -1 if no size was requested; otherwise 0.0 to 1.0. */
    private double dimRatio(String label, Double have, Double need, MatchResult res) {
        if (need == null || need <= 0) return -1;
        if (have == null) {
            res.unmet.add(label + ": not stated in the listing (" + Material.fmt(need) + " cm needed)");
            return 0;
        }
        double ratio = Math.min(have / need, 1.0);
        if (ratio >= 1.0) res.met.add(label + " fits: " + Material.fmt(have) + " cm (" + Material.fmt(need) + " cm needed)");
        else res.unmet.add(label + " too short: only " + Material.fmt(have) + " cm (" + Material.fmt(need) + " cm needed)");
        return ratio;
    }
}

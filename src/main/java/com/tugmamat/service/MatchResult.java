package com.tugmamat.service;

import com.tugmamat.model.Material;
import java.util.ArrayList;
import java.util.List;

/** Result of comparing one listing against one request. */
public class MatchResult {
    public Material material;
    public int score;
    public List<String> met = new ArrayList<>();
    public List<String> unmet = new ArrayList<>();

    public Material getMaterial() { return material; }
    public int getScore() { return score; }
    public List<String> getMet() { return met; }
    public List<String> getUnmet() { return unmet; }
    public boolean isExact() { return score >= MatchingService.EXACT_THRESHOLD; }
}

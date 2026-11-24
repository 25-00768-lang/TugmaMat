package com.tugmamat.web;

import com.tugmamat.model.Enums.*;
import com.tugmamat.model.*;
import com.tugmamat.repo.*;
import com.tugmamat.service.MatchResult;
import com.tugmamat.service.MatchingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MainController {

    private final UserRepository users;
    private final MaterialRepository materials;
    private final MaterialRequestRepository requests;
    private final MessageRepository messages;
    private final ClaimRepository claims;
    private final MatchingService matching;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public MainController(UserRepository users, MaterialRepository materials, MaterialRequestRepository requests,
                          MessageRepository messages, ClaimRepository claims, MatchingService matching) {
        this.users = users;
        this.materials = materials;
        this.requests = requests;
        this.messages = messages;
        this.claims = claims;
        this.matching = matching;
    }

    // ---------- helpers ----------
    private User me(HttpSession s) {
        Long id = (Long) s.getAttribute("userId");
        return id == null ? null : users.findById(id).orElse(null);
    }

    private static String nz(String s) { return s == null ? "" : s; }

    /** Zero or negative sizes are not accepted. */
    private static Double dim(Double d) { return d == null || d <= 0 ? null : d; }

    /** New session on every login/register to prevent session fixation. */
    private void startSession(HttpServletRequest req, Long userId) {
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        req.getSession(true).setAttribute("userId", userId);
    }

    /** Deletes an old uploaded image (only files under ./uploads). */
    private void deleteUpload(String url) {
        if (url == null || !url.startsWith("/uploads/")) return;
        try {
            Files.deleteIfExists(Paths.get("uploads").resolve(Paths.get(url).getFileName().toString()));
        } catch (IOException ignored) { }
    }

    private void addStats(Model model) {
        model.addAttribute("availableCount", materials.countByStatus(Status.AVAILABLE));
        model.addAttribute("reservedCount", materials.countByStatus(Status.RESERVED));
        model.addAttribute("completedCount", materials.countByStatus(Status.COMPLETED));
        model.addAttribute("activeRequests", requests.countByFulfilledFalse());
    }

    private String saveImage(MultipartFile f) throws IOException {
        if (f == null || f.isEmpty()) return null;
        String ext = StringUtils.getFilenameExtension(f.getOriginalFilename());
        if (ext == null) return null;
        ext = ext.toLowerCase();
        if (!List.of("jpg", "jpeg", "png", "webp").contains(ext)) return null;
        Path dir = Paths.get("uploads");
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + "." + ext;
        f.transferTo(dir.resolve(name).toAbsolutePath());
        return "/uploads/" + name;
    }

    // ---------- home / auth ----------
    @GetMapping("/")
    public String home(Model model) {
        addStats(model);
        return "index";
    }

    @GetMapping("/register")
    public String registerForm() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String fullName, @RequestParam String email, @RequestParam String password,
                           @RequestParam Role role, @RequestParam(required = false) String location,
                           Model model, HttpServletRequest request) {
        email = email.trim().toLowerCase();
        if (fullName.isBlank()) {
            model.addAttribute("error", "Please enter your full name.");
            return "register";
        }
        if (role == Role.ADMIN) role = Role.REQUESTER; // cannot register as admin
        if (users.findByEmail(email).isPresent()) {
            model.addAttribute("error", "An account with that email already exists. Try logging in.");
            return "register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register";
        }
        User u = new User();
        u.fullName = fullName.trim(); u.email = email; u.role = role; u.location = nz(location).trim();
        u.password = encoder.encode(password);
        users.save(u);
        startSession(request, u.id);
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginForm() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model, HttpServletRequest request) {
        Optional<User> u = users.findByEmail(email.trim().toLowerCase());
        if (u.isEmpty() || !encoder.matches(password, u.get().password)) {
            model.addAttribute("error", "Incorrect email or password.");
            return "login";
        }
        startSession(request, u.get().id);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ---------- dashboard ----------
    @GetMapping("/dashboard")
    public String dashboard(HttpSession s, Model model) {
        expireStale();
        User u = me(s);
        addStats(model);
        if (u.role == Role.PROVIDER) {
            model.addAttribute("myListings", materials.findByOwnerOrderByCreatedAtDesc(u));
        }
        if (u.role == Role.REQUESTER) {
            List<MaterialRequest> mine = requests.findByRequesterOrderByCreatedAtDesc(u);
            Map<Long, Integer> best = new HashMap<>();
            Map<Long, Integer> counts = new HashMap<>();
            for (MaterialRequest r : mine) {
                List<MatchResult> res = matching.match(r);
                best.put(r.id, res.isEmpty() ? 0 : res.get(0).score);
                counts.put(r.id, res.size());
            }
            model.addAttribute("myRequests", mine);
            model.addAttribute("best", best);
            model.addAttribute("counts", counts);
        }
        return "dashboard";
    }

    // ---------- listings ----------
    @GetMapping("/listings")
    public String listings(@RequestParam(required = false) String q,
                           @RequestParam(required = false) Category category,
                           @RequestParam(required = false) Condition condition,
                           @RequestParam(required = false) String location,
                           @RequestParam(required = false) Integer minQty,
                           @RequestParam(required = false) Double minLength,
                           Model model) {
        expireStale();
        List<Material> list = materials.findByStatusOrderByCreatedAtDesc(Status.AVAILABLE).stream()
                .filter(m -> q == null || q.isBlank() || (m.name + " " + nz(m.description)).toLowerCase().contains(q.toLowerCase()))
                .filter(m -> category == null || m.category == category)
                .filter(m -> condition == null || m.matCondition == condition)
                .filter(m -> location == null || location.isBlank() || com.tugmamat.model.Locations.isAll(location) || nz(m.location).toLowerCase().contains(location.toLowerCase()))
                .filter(m -> minQty == null || m.quantity >= minQty)
                .filter(m -> minLength == null || (m.lengthCm != null && m.lengthCm >= minLength))
                .toList();
        model.addAttribute("list", list);
        model.addAttribute("q", q);
        model.addAttribute("selCategory", category);
        model.addAttribute("selCondition", condition);
        model.addAttribute("location", location);
        model.addAttribute("minQty", minQty);
        model.addAttribute("minLength", minLength);
        return "listings";
    }

    @GetMapping("/listings/new")
    public String newListing(HttpSession s) {
        if (me(s).role != Role.PROVIDER) return "redirect:/dashboard";
        return "listing-form";
    }

    @PostMapping("/listings")
    public String createListing(@RequestParam String name, @RequestParam Category category,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) Double lengthCm,
                                @RequestParam(required = false) Double widthCm,
                                @RequestParam int quantity,
                                @RequestParam(defaultValue = "pcs") String unit,
                                @RequestParam Condition condition,
                                @RequestParam(required = false) String location,
                                @RequestParam(required = false) MultipartFile image,
                                HttpSession s) throws IOException {
        User u = me(s);
        if (u.role != Role.PROVIDER) return "redirect:/dashboard";
        Material m = new Material();
        m.owner = u; m.name = name.trim(); m.category = category; m.description = nz(description).trim();
        m.lengthCm = dim(lengthCm); m.widthCm = dim(widthCm); m.quantity = Math.max(quantity, 1);
        m.unit = unit.trim(); m.matCondition = condition;
        m.location = nz(location).isBlank() ? nz(u.location) : location.trim();
        m.imageUrl = saveImage(image);
        materials.save(m);
        return "redirect:/listings/" + m.id;
    }

    @GetMapping("/listings/{id}")
    public String listingDetail(@PathVariable Long id, HttpSession s, Model model) {
        expireStale();
        Material m = materials.findById(id).orElse(null);
        if (m == null) return "redirect:/listings";
        User u = me(s);
        model.addAttribute("m", m);
        model.addAttribute("isOwner", m.owner.id.equals(u.id));
        model.addAttribute("canManage", m.owner.id.equals(u.id) || u.role == Role.ADMIN);
        List<Claim> taken = claims.findByMaterialOrderByCreatedAtDesc(m).stream()
                .filter(c -> c.getState() != ClaimStatus.CANCELLED).toList();
        model.addAttribute("claimList", taken);
        model.addAttribute("takenTotal", taken.stream().mapToInt(c -> c.quantity).sum());
        model.addAttribute("myTaken", taken.stream().filter(c -> c.requester.id.equals(u.id)).mapToInt(c -> c.quantity).sum());
        return "listing-detail";
    }

    @GetMapping("/listings/{id}/edit")
    public String editListing(@PathVariable Long id, HttpSession s, Model model) {
        User u = me(s);
        Material m = materials.findById(id).orElse(null);
        if (m == null || !m.owner.id.equals(u.id)) return "redirect:/listings";
        model.addAttribute("m", m);
        return "listing-form";
    }

    @PostMapping("/listings/{id}/edit")
    public String updateListing(@PathVariable Long id, @RequestParam String name, @RequestParam Category category,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) Double lengthCm,
                                @RequestParam(required = false) Double widthCm,
                                @RequestParam int quantity,
                                @RequestParam(defaultValue = "pcs") String unit,
                                @RequestParam Condition condition,
                                @RequestParam(required = false) String location,
                                @RequestParam(required = false) MultipartFile image,
                                HttpSession s) throws IOException {
        User u = me(s);
        Material m = materials.findById(id).orElse(null);
        if (m == null || !m.owner.id.equals(u.id)) return "redirect:/listings";
        m.name = name.trim(); m.category = category; m.description = nz(description).trim();
        m.lengthCm = dim(lengthCm); m.widthCm = dim(widthCm); m.quantity = Math.max(quantity, 1);
        m.unit = unit.trim(); m.matCondition = condition;
        m.location = nz(location).isBlank() ? nz(u.location) : location.trim();
        String newImage = saveImage(image);
        if (newImage != null) { deleteUpload(m.imageUrl); m.imageUrl = newImage; }
        materials.save(m);
        return "redirect:/listings/" + m.id;
    }

    @PostMapping("/listings/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam Status status, HttpSession s) {
        User u = me(s);
        Material m = materials.findById(id).orElseThrow();
        boolean owner = m.owner.id.equals(u.id);
        if (owner && status != Status.REMOVED) { m.status = status; materials.save(m); }
        else if (u.role == Role.ADMIN) { m.status = status; materials.save(m); }
        return "redirect:/listings/" + id;
    }

    /** A requester takes some pieces: the listing quantity goes down; at 0 it becomes "Given away". */
    @PostMapping("/listings/{id}/take")
    public String take(@PathVariable Long id, @RequestParam int quantity, HttpSession s) {
        User u = me(s);
        synchronized (this) {
            Material m = materials.findById(id).orElse(null);
            if (m == null) return "redirect:/listings";
            if (m.owner.id.equals(u.id)) return "redirect:/listings/" + id + "?err=own";
            if (m.status != Status.AVAILABLE || m.quantity <= 0) return "redirect:/listings/" + id + "?err=unavailable";
            if (quantity < 1) return "redirect:/listings/" + id + "?err=qty";
            if (quantity > m.quantity) return "redirect:/listings/" + id + "?err=toomany";
            m.quantity -= quantity;
            if (m.quantity == 0) m.status = Status.RESERVED;
            materials.save(m);
            Claim c = new Claim();
            c.material = m; c.requester = u; c.quantity = quantity;
            claims.save(c);
            Message msg = new Message();
            msg.listing = m; msg.sender = u; msg.recipient = m.owner;
            msg.content = u.fullName + " took " + quantity + " " + nz(m.unit) + " of " + m.name + ". "
                    + (m.quantity == 0 ? "None left." : m.quantity + " " + nz(m.unit) + " left.")
                    + " Please prepare it for pickup.";
            messages.save(msg);
        }
        return "redirect:/listings/" + id + "?taken=" + quantity;
    }

    @PostMapping("/listings/{id}/report")
    public String report(@PathVariable Long id) {
        Material m = materials.findById(id).orElseThrow();
        m.reported = true;
        materials.save(m);
        return "redirect:/listings/" + id;
    }

    // ---------- requests & matching ----------
    @GetMapping("/requests/new")
    public String newRequest(HttpSession s) {
        if (me(s).role != Role.REQUESTER) return "redirect:/dashboard";
        return "request-form";
    }

    @PostMapping("/requests")
    public String createRequest(@RequestParam String name, @RequestParam Category category,
                                @RequestParam(required = false) Double lengthCm,
                                @RequestParam(required = false) Double widthCm,
                                @RequestParam int quantity, @RequestParam Condition desiredCondition,
                                @RequestParam(required = false) String location, HttpSession s) {
        User u = me(s);
        if (u.role != Role.REQUESTER) return "redirect:/dashboard";
        MaterialRequest r = new MaterialRequest();
        r.requester = u; r.name = name.trim(); r.category = category;
        r.lengthCm = dim(lengthCm); r.widthCm = dim(widthCm); r.quantity = Math.max(quantity, 1);
        r.desiredCondition = desiredCondition;
        r.location = nz(location).isBlank() ? nz(u.location) : location.trim();
        requests.save(r);
        return "redirect:/requests/" + r.id + "/matches";
    }

    @GetMapping("/requests/{id}/matches")
    public String matches(@PathVariable Long id, HttpSession s, Model model) {
        User u = me(s);
        MaterialRequest r = requests.findById(id).orElse(null);
        if (r == null || (!r.requester.id.equals(u.id) && u.role != Role.ADMIN)) return "redirect:/dashboard";
        List<MatchResult> all = matching.match(r);
        model.addAttribute("r", r);
        model.addAttribute("exact", all.stream().filter(MatchResult::isExact).toList());
        model.addAttribute("alternatives", all.stream().filter(x -> !x.isExact()).toList());
        return "matches";
    }

    @PostMapping("/requests/{id}/delete")
    public String deleteRequest(@PathVariable Long id, HttpSession s) {
        User u = me(s);
        MaterialRequest r = requests.findById(id).orElse(null);
        if (r != null && r.requester.id.equals(u.id)) requests.delete(r);
        return "redirect:/dashboard";
    }

    @PostMapping("/requests/{id}/fulfill")
    public String fulfill(@PathVariable Long id, HttpSession s) {
        User u = me(s);
        MaterialRequest r = requests.findById(id).orElseThrow();
        if (r.requester.id.equals(u.id)) { r.fulfilled = true; requests.save(r); }
        return "redirect:/dashboard";
    }

    // ---------- messages ----------
    @GetMapping("/messages")
    public String inbox(HttpSession s, Model model) {
        User u = me(s);
        model.addAttribute("msgs", messages.findBySenderOrRecipientOrderByCreatedAtDesc(u, u));
        return "messages";
    }

    @PostMapping("/messages/send")
    public String send(@RequestParam Long listingId, @RequestParam Long recipientId, @RequestParam String content,
                       @RequestParam(defaultValue = "messages") String back, HttpSession s) {
        User u = me(s);
        Material m = materials.findById(listingId).orElseThrow();
        User to = users.findById(recipientId).orElseThrow();
        if (!content.isBlank() && !to.id.equals(u.id)) {
            Message msg = new Message();
            msg.listing = m; msg.sender = u; msg.recipient = to; msg.content = content.trim();
            messages.save(msg);
        }
        return back.equals("listing") ? "redirect:/listings/" + listingId : "redirect:/messages";
    }

    // ---------- history ----------
    // ---------- pickups ----------
    private void note(Material m, User from, User to, String text) {
        Message msg = new Message();
        msg.listing = m; msg.sender = from; msg.recipient = to; msg.content = text;
        messages.save(msg);
    }

    /** Returns the pieces of a claim to the listing. */
    private void restock(Claim c) {
        Material m = c.material;
        m.quantity += c.quantity;
        if ((m.status == Status.RESERVED || m.status == Status.COMPLETED) && m.quantity > 0) m.status = Status.AVAILABLE;
        materials.save(m);
    }

    /** Pending claims that nobody picked up in time are cancelled and their pieces go back to the listing. */
    private synchronized void expireStale() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (Claim c : claims.findAll()) {
            if (c.isPending() && c.getExpiresAt().isBefore(now)) {
                c.status = ClaimStatus.CANCELLED; c.closedAt = now; claims.save(c);
                restock(c);
                note(c.material, c.requester, c.material.owner, "Pickup of " + c.material.name + " was not collected within " + Claim.PICKUP_DAYS
                        + " days. " + c.quantity + " " + nz(c.material.unit) + " went back to the listing.");
            }
        }
    }

    /** Seller side only (like Shopee's seller orders): customers have no Pickups page. */
    @GetMapping("/claims")
    public String myClaims(HttpSession s, Model model) {
        User u = me(s);
        if (u.role == Role.REQUESTER) return "redirect:/dashboard";
        expireStale();
        List<Claim> all = claims.findAllByOrderByCreatedAtDesc();
        model.addAttribute("incoming", all.stream()
                .filter(c -> u.role == Role.ADMIN || c.material.owner.id.equals(u.id)).toList());
        return "claims";
    }

    @PostMapping("/claims/{id}/pickup")
    public String pickup(@PathVariable Long id, HttpSession s) {
        User u = me(s);
        synchronized (this) {
            Claim c = claims.findById(id).orElseThrow();
            boolean allowed = c.material.owner.id.equals(u.id) || u.role == Role.ADMIN;
            if (!allowed || !c.isPending()) return "redirect:/claims";
            c.status = ClaimStatus.PICKED_UP; c.closedAt = java.time.LocalDateTime.now(); claims.save(c);
            Material m = c.material;
            boolean anyPending = claims.findByMaterialOrderByCreatedAtDesc(m).stream().anyMatch(Claim::isPending);
            if (m.quantity == 0 && !anyPending) { m.status = Status.COMPLETED; materials.save(m); }
            note(m, u, c.requester, "Pickup confirmed: " + c.quantity + " " + nz(m.unit) + " of " + m.name + ". Thank you!");
        }
        return "redirect:/claims?done=pickup";
    }

    @PostMapping("/claims/{id}/cancel")
    public String cancelClaim(@PathVariable Long id, HttpSession s) {
        User u = me(s);
        synchronized (this) {
            Claim c = claims.findById(id).orElseThrow();
            boolean provider = c.material.owner.id.equals(u.id) || u.role == Role.ADMIN;
            if (!provider || !c.isPending()) return "redirect:/claims";
            c.status = ClaimStatus.CANCELLED; c.closedAt = java.time.LocalDateTime.now(); claims.save(c);
            restock(c);
            note(c.material, u, c.requester, "Your pickup of " + c.quantity + " " + nz(c.material.unit) + " of "
                    + c.material.name + " was cancelled by the provider. The pieces went back to the listing.");
        }
        return "redirect:/claims?done=cancel";
    }

    public record HistoryRow(Material material, String taker, int qty, java.time.LocalDateTime when) {}

    @GetMapping("/history")
    public String history(Model model) {
        List<HistoryRow> rows = new ArrayList<>();
        Set<Long> withClaims = new HashSet<>();
        for (Claim c : claims.findAllByOrderByCreatedAtDesc()) {
            withClaims.add(c.material.id);
            if (c.getState() == ClaimStatus.PICKED_UP)
                rows.add(new HistoryRow(c.material, c.requester.fullName, c.quantity, c.closedAt != null ? c.closedAt : c.createdAt));
        }
        // listings the provider marked "Given away" by hand (no per-piece records)
        for (Material m : materials.findByStatusOrderByCreatedAtDesc(Status.COMPLETED)) {
            if (!withClaims.contains(m.id) && m.quantity > 0) rows.add(new HistoryRow(m, null, m.quantity, m.createdAt));
        }
        rows.sort((a, b) -> b.when().compareTo(a.when()));
        Map<Category, Integer> byCategory = new EnumMap<>(Category.class);
        int total = 0;
        for (HistoryRow r : rows) {
            byCategory.merge(r.material().category, r.qty(), Integer::sum);
            total += r.qty();
        }
        model.addAttribute("done", rows);
        model.addAttribute("byCategory", byCategory);
        model.addAttribute("totalItems", total);
        return "history";
    }

    // ---------- admin ----------
    @GetMapping("/admin")
    public String admin(HttpSession s, Model model) {
        if (me(s).role != Role.ADMIN) return "redirect:/dashboard";
        model.addAttribute("all", materials.findAllByOrderByReportedDescCreatedAtDesc());
        return "admin";
    }

    @PostMapping("/admin/listings/{id}/dismiss")
    public String dismiss(@PathVariable Long id, HttpSession s) {
        if (me(s).role != Role.ADMIN) return "redirect:/dashboard";
        Material m = materials.findById(id).orElseThrow();
        m.reported = false;
        materials.save(m);
        return "redirect:/admin";
    }

    @PostMapping("/admin/listings/{id}/remove")
    public String remove(@PathVariable Long id, HttpSession s) {
        if (me(s).role != Role.ADMIN) return "redirect:/dashboard";
        Material m = materials.findById(id).orElseThrow();
        m.status = Status.REMOVED;
        m.reported = false;
        materials.save(m);
        return "redirect:/admin";
    }
}

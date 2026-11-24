package com.tugmamat.web;

import com.tugmamat.model.Enums.*;
import com.tugmamat.model.*;
import com.tugmamat.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Adds demo accounts and sample data when the database is empty. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final MaterialRepository materials;
    private final MaterialRequestRepository requests;

    public DataSeeder(UserRepository users, MaterialRepository materials, MaterialRequestRepository requests) {
        this.users = users;
        this.materials = materials;
        this.requests = requests;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) { migrateDemoData(); backfillPhotos(); fixOldSvgPhotos(); seedExtras(); return; }
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

        User admin = user("Admin", "admin@tugmamat.ph", enc.encode("admin123"), Role.ADMIN, "Batangas City");
        User provider = user("Provider", "provider@tugmamat.ph", enc.encode("provider123"), Role.PROVIDER, "Batangas City");
        User requester = user("Requester", "requester@tugmamat.ph", enc.encode("requester123"), Role.REQUESTER, "Batangas City");
        users.save(admin); users.save(provider); users.save(requester);

        materials.save(photo(material(provider, "Wood 2x4 lumber", Category.KAHOY, "Leftover from a house build. Still dry and straight.",
                244.0, 5.0, 3, "pcs", Condition.GOOD, "Batangas City"), "lumber-2x4"));
        materials.save(photo(material(provider, "Marine plywood 1/2 inch", Category.PLYWOOD, "One full sheet, with a few scratches on the edge.",
                244.0, 122.0, 1, "sheet", Condition.GOOD, "Batangas City"), "plywood-marine"));
        materials.save(photo(material(provider, "Ceramic floor tiles 30x30", Category.TILES, "White, no cracks.",
                30.0, 30.0, 40, "pcs", Condition.NEW, "Lipa City"), "tiles-ceramic"));
        materials.save(photo(material(provider, "Wood 2x3 lumber", Category.KAHOY, "A bit old but sturdy.",
                200.0, 3.0, 6, "pcs", Condition.FAIR, "Batangas City"), "lumber-2x3"));

        MaterialRequest r = new MaterialRequest();
        r.requester = requester; r.name = "2x4 lumber"; r.category = Category.KAHOY;
        r.lengthCm = 240.0; r.widthCm = 5.0; r.quantity = 5;
        r.desiredCondition = Condition.GOOD; r.location = Locations.ALL;
        requests.save(r);
        seedExtras();
    }



    /** Adds many more demo listings across Batangas (only while the database has few listings). */
    private void seedExtras() {
        if (materials.count() >= 12) return;
        User p = users.findByEmail("provider@tugmamat.ph").orElse(null);
        if (p == null) return;
        Object[][] rows = {
            // name, category, description, length, width, qty, unit, condition, location, image
            {"Wood 2x2 lumber", Category.KAHOY, "Short offcuts, good for framing and furniture.", 120.0, 5.0, 12, "pcs", Condition.GOOD, "Lipa City", "wood"},
            {"Tanguile 1x4 board", Category.KAHOY, "Smooth boards left from a ceiling job.", 240.0, 10.0, 8, "pcs", Condition.FAIR, "Tanauan City", "wood"},
            {"Coco lumber 2x3", Category.KAHOY, "Sturdy coco lumber, ready for scaffolding or fences.", 300.0, 5.0, 10, "pcs", Condition.FAIR, "Nasugbu", "wood"},
            {"Wood 2x4 lumber (long)", Category.KAHOY, "Straight and dry, no termite damage.", 360.0, 5.0, 14, "pcs", Condition.GOOD, "Santo Tomas City", "wood"},
            {"Ordinary plywood 3/4 inch", Category.PLYWOOD, "Three full sheets, stored indoors.", 244.0, 122.0, 3, "sheet", Condition.GOOD, "Batangas City", "plywood"},
            {"Plywood 1/4 inch", Category.PLYWOOD, "Thin sheets, good for ceilings and cabinet backs.", 244.0, 122.0, 6, "sheet", Condition.FAIR, "Bauan", "plywood"},
            {"Fiber cement board 6mm", Category.PLYWOOD, "Brand new leftovers from a ceiling project.", 244.0, 122.0, 5, "sheet", Condition.NEW, "San Juan", "plywood"},
            {"Steel rebar 10mm", Category.BAKAL, "Deformed bars, light surface rust only.", 600.0, 1.0, 25, "pcs", Condition.GOOD, "Batangas City", "steel"},
            {"Angle bar 2x2", Category.BAKAL, "Used but straight, good for frames and shelves.", 600.0, 5.0, 8, "pcs", Condition.USED, "Calaca", "steel"},
            {"Corrugated GI sheet", Category.YERO, "Roofing sheets, no holes.", 240.0, 80.0, 15, "sheets", Condition.GOOD, "Lemery", "roofing"},
            {"Rib-type roofing 0.4mm", Category.YERO, "Long-span roofing, a few dents on the edges.", 300.0, 90.0, 10, "sheets", Condition.FAIR, "Taal", "roofing"},
            {"Vinyl floor tiles 40x40", Category.TILES, "Unopened boxes, wood pattern.", 40.0, 40.0, 60, "pcs", Condition.NEW, "Santo Tomas City", "tiles"},
            {"Granite tiles 60x60", Category.TILES, "Polished grey, a few spares for repairs.", 60.0, 60.0, 24, "pcs", Condition.GOOD, "Lipa City", "tiles"},
            {"Portland cement 40kg", Category.SEMENTO, "Sealed bags, kept dry.", null, null, 12, "bags", Condition.NEW, "Malvar", "cement"},
            {"Washed sand", Category.SEMENTO, "Clean sand left from a slab pour.", null, null, 3, "cu.m", Condition.GOOD, "Rosario", "pile"},
            {"Gravel 3/4", Category.SEMENTO, "Good for concrete mix or drainage.", null, null, 2, "cu.m", Condition.GOOD, "San Jose", "pile"},
            {"Latex paint, white 4L", Category.PINTURA, "Sealed pails, flat white.", null, null, 5, "pails", Condition.NEW, "Batangas City", "paint"},
            {"Semi-gloss enamel paint", Category.PINTURA, "Opened once, lots left.", null, null, 3, "gallons", Condition.GOOD, "Ibaan", "paint"},
            {"PVC pipe 2 inch", Category.KURYENTE_TUBO, "Orange PVC, uncut lengths.", 300.0, 5.0, 10, "pcs", Condition.NEW, "Tanauan City", "pipe"},
            {"Electrical wire THHN #12", Category.KURYENTE_TUBO, "Two partial rolls, red and black.", null, null, 2, "rolls", Condition.GOOD, "Lian", "wire"},
            {"Jalousie window with frame", Category.BINTANA_PINTO, "Wood frame, glass slats complete.", 120.0, 90.0, 3, "pcs", Condition.FAIR, "Taysan", "window"},
            {"Flush door 80x210", Category.BINTANA_PINTO, "Hollow-core doors, no frame.", 210.0, 80.0, 2, "pcs", Condition.GOOD, "Balayan", "door"},
            {"Hollow blocks 4 inch", Category.IBA, "Concrete hollow blocks, no cracks.", 40.0, 20.0, 100, "pcs", Condition.GOOD, "Mabini", "block"},
        };
        java.util.Set<String> have = new java.util.HashSet<>();
        for (Material m : materials.findAll()) if (m.name != null) have.add(m.name);
        for (Object[] r : rows) {
            if (have.contains((String) r[0])) continue;
            Material m = material(p, (String) r[0], (Category) r[1], (String) r[2], (Double) r[3], (Double) r[4],
                    (Integer) r[5], (String) r[6], (Condition) r[7], (String) r[8]);
            m.imageUrl = "/img/materials/extra-" + r[9] + ".jpg";
            materials.save(m);
        }
    }

    /** For an older database: rename the original Filipino demo data to the English version. */
    private void migrateDemoData() {
        users.findByEmail("provider@tugmamat.ph").ifPresent(u -> {
            if ("Mang Roy".equals(u.fullName)) { u.fullName = "Provider"; users.save(u); }
        });
        users.findByEmail("requester@tugmamat.ph").ifPresent(u -> {
            if ("Aling Nena".equals(u.fullName)) { u.fullName = "Requester"; users.save(u); }
        });
        java.util.Map<String, String[]> old = java.util.Map.of(
                "Kahoy 2x4 lumber", new String[]{"Wood 2x4 lumber", "Leftover from a house build. Still dry and straight."},
                "Kahoy 2x3 lumber", new String[]{"Wood 2x3 lumber", "A bit old but sturdy."},
                "Marine plywood 1/2 inch", new String[]{null, "One full sheet, with a few scratches on the edge."},
                "Ceramic floor tiles 30x30", new String[]{null, "White, no cracks."});
        for (Material m : materials.findAll()) {
            String[] t = m.name == null ? null : old.get(m.name);
            if (t == null) continue;
            if (t[0] != null) m.name = t[0];
            m.description = t[1];
            materials.save(m);
        }
    }

    /** For an older database: the old cartoon .svg demo images were replaced by realistic .jpg photos. */
    private void fixOldSvgPhotos() {
        for (Material m : materials.findAll()) {
            if (m.imageUrl != null && m.imageUrl.startsWith("/img/materials/extra-") && m.imageUrl.endsWith(".svg")) {
                m.imageUrl = m.imageUrl.substring(0, m.imageUrl.length() - 4) + ".jpg";
                materials.save(m);
            }
        }
    }

    /** For an older database: add photos to demo listings that have none. */
    private void backfillPhotos() {
        for (Material m : materials.findAll()) {
            if (m.imageUrl != null || m.name == null) continue;
            String n = m.name.toLowerCase();
            String f = n.contains("2x4") ? "lumber-2x4" : n.contains("2x3") ? "lumber-2x3"
                    : n.contains("plywood") ? "plywood-marine" : n.contains("tiles") ? "tiles-ceramic" : null;
            if (f != null) { m.imageUrl = "/img/materials/" + f + ".jpg"; materials.save(m); }
        }
    }

    /** Sets the image from static/img/materials/. */
    private Material photo(Material m, String file) {
        m.imageUrl = "/img/materials/" + file + ".jpg";
        return m;
    }

    private User user(String name, String email, String pw, Role role, String loc) {
        User u = new User();
        u.fullName = name; u.email = email; u.password = pw; u.role = role; u.location = loc;
        return u;
    }

    private Material material(User owner, String name, Category cat, String desc, Double len, Double wid,
                              int qty, String unit, Condition cond, String loc) {
        Material m = new Material();
        m.owner = owner; m.name = name; m.category = cat; m.description = desc;
        m.lengthCm = len; m.widthCm = wid; m.quantity = qty; m.unit = unit;
        m.matCondition = cond; m.location = loc;
        return m;
    }
}

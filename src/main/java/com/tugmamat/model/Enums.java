package com.tugmamat.model;

public final class Enums {
    private Enums() {}

    public enum Role {
        PROVIDER("Provider (has leftover materials)"),
        REQUESTER("Requester (looking for materials)"),
        ADMIN("Administrator");
        private final String label;
        Role(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum Category {
        KAHOY("Wood / Lumber"),
        PLYWOOD("Plywood / Board"),
        BAKAL("Steel / Metal"),
        YERO("Roofing / Sheet metal"),
        TILES("Tiles / Flooring"),
        SEMENTO("Cement / Sand / Gravel"),
        PINTURA("Paint"),
        KURYENTE_TUBO("Electrical / Plumbing"),
        BINTANA_PINTO("Windows / Doors"),
        IBA("Other");
        private final String label;
        Category(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    /** NEW is the best, USED is the lowest. ordinal() matters in matching. */
    public enum Condition {
        NEW("New"),
        GOOD("Good"),
        FAIR("Fair"),
        USED("Used");
        private final String label;
        Condition(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum Status {
        AVAILABLE("Available"),
        RESERVED("Reserved"),
        COMPLETED("Given away"),
        REMOVED("Removed");
        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum ClaimStatus {
        PENDING("Pending pickup"),
        PICKED_UP("Picked up"),
        CANCELLED("Cancelled");
        private final String label;
        ClaimStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }
}

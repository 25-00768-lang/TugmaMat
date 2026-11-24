package com.tugmamat.web;

import com.tugmamat.model.Enums.Category;
import com.tugmamat.model.Enums.Condition;
import com.tugmamat.model.Enums.Role;
import com.tugmamat.model.Enums.Status;
import com.tugmamat.model.User;
import com.tugmamat.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import com.tugmamat.model.Locations;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Data needed by every page (navbar, dropdowns). */
@ControllerAdvice
public class GlobalModel {
    private final UserRepository users;

    public GlobalModel(UserRepository users) { this.users = users; }

    @ModelAttribute("me")
    public User me(HttpSession s) {
        Long id = (Long) s.getAttribute("userId");
        return id == null ? null : users.findById(id).orElse(null);
    }

    @ModelAttribute("categories")
    public Category[] categories() { return Category.values(); }

    @ModelAttribute("conditions")
    public Condition[] conditions() { return Condition.values(); }

    @ModelAttribute("statuses")
    public Status[] statuses() { return Status.values(); }

    @ModelAttribute("locations")
    public List<String> locations() { return Locations.LIST; }

    @ModelAttribute("ADMIN")
    public Role admin() { return Role.ADMIN; }
}

package com.example.notes.application.coolproject.config;

/**
 * Single source of truth for API paths. Class-level constants are full prefixes used in
 * {@code @RequestMapping}; the remaining constants are relative to their prefix.
 */
public final class Endpoints {

  private Endpoints() {}

  /** Root of the whole API. */
  public static final String MAIN = "/api/notes";

  // ─── AUTH ───────────────────────────────────────────
  public static final String AUTH = MAIN + "/auth";
  public static final String AUTH_REGISTER = "/register";
  public static final String AUTH_LOGIN = "/login";

  // ─── EXPENSES ────────────────────────────────────────
  public static final String EXPENSES = MAIN + "/expenses";
  public static final String EXPENSE_BY_ID = "/{id}";
  public static final String EXPENSES_BY_STATUS = "/status/{status}";
  public static final String EXPENSES_BY_CATEGORY = "/category/{categoryId}";

  // ─── CATEGORIES ──────────────────────────────────────
  public static final String CATEGORIES = MAIN + "/categories";
  public static final String CATEGORY_BY_ID = "/{id}";

  // ─── USERS ───────────────────────────────────────────
  public static final String USERS = MAIN + "/users";
  public static final String USER_ME = "/me";

  // ─── STATISTICS ──────────────────────────────────────
  public static final String STATISTICS = MAIN + "/statistics";
  public static final String STATISTICS_SUMMARY = "/summary";

  // ─── ADMIN ───────────────────────────────────────────
  public static final String ADMIN = MAIN + "/admin";
  public static final String ADMIN_USERS = ADMIN + "/users";
  public static final String ADMIN_USER_BY_ID = "/{id}";
  public static final String ADMIN_USER_ROLE = "/{id}/role";
  public static final String ADMIN_USER_STATUS = "/{id}/status";

  // ─── SECURITY MATCHERS ───────────────────────────────
  /** Ant pattern matching every sub-path of a prefix. */
  public static final String ALL_SUBPATHS = "/**";

  /** Paths reachable without authentication. */
  static final String[] PUBLIC = {
    AUTH + ALL_SUBPATHS, "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/error"
  };
}

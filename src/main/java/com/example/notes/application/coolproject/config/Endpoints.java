package com.example.notes.application.coolproject.config;

public final class Endpoints {

  private Endpoints() {}

  // MAIN
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
}

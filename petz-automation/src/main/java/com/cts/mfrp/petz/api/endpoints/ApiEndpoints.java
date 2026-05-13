package com.cts.mfrp.petz.api.endpoints;

/**
 * Endpoint paths for the deployed backend (com.petz, context-path = /api).
 * Paths are relative to {@link com.cts.mfrp.petz.constants.AppConstants#API_BASE_URL},
 * which already includes the /api context path.
 */
public final class ApiEndpoints {

    private ApiEndpoints() {}

    // Auth
    public static final String AUTH_REGISTER         = "/auth/register";
    public static final String AUTH_LOGIN            = "/auth/login";

    // User
    public static final String USERS_ME              = "/users/me";
    public static final String USERS_ME_PHOTO        = "/users/me/photo";

    // Pets
    public static final String PETS                  = "/pets/";
    public static final String PETS_MY               = "/pets/my";
    public static final String PETS_BY_ID            = "/pets/{id}";

    // Hospitals — public
    public static final String HOSPITALS_PUBLIC      = "/hospitals/public";
    public static final String HOSPITALS_PUBLIC_BY_ID = "/hospitals/public/{id}";

    // NGO — public
    public static final String NGO_PUBLIC            = "/ngo/public";
    public static final String NGO_PUBLIC_BY_ID      = "/ngo/public/{id}";

    // Adoption
    public static final String ADOPTION_ANIMALS      = "/adoption/animals";
    public static final String ADOPTION_ANIMALS_BY_ID = "/adoption/animals/{id}";
}

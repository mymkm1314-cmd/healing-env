package com.mym.healingenv.common;

public enum UserRole {
    ADMIN(1),
    PROJECT_MANAGER(2),
    EVALUATOR(3),
    VIEWER(4);

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserRole fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        return null;
    }
}

package com.mym.healingenv.utils;

import com.mym.healingenv.dto.UserDTO;

public class UserHolder {
    private static final ThreadLocal<UserDTO> userHolder = new ThreadLocal<>();
    public static void saveUser(UserDTO user){
        userHolder.set(user);
    }
    public static UserDTO get(){
        return userHolder.get();
    }
    public static Long getUserId() {
        UserDTO user = get();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        UserDTO user = get();
        return user != null ? user.getUsername() : null;
    }

    public static Integer getRole() {
        UserDTO user = get();
        return user != null ? user.getRole() : null;
    }
    public static void clear() {
        userHolder.remove();
    }
}

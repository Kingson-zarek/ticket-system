package com.hnty.ticketsystem.security;

/**
 * @author Crown
 */
public class UserContext {

    private UserContext(){}

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
       CurrentUser currentUser = HOLDER.get();
       return currentUser == null ? null : currentUser.getUserId();
    }

    public static String role(){
        CurrentUser currentUser = HOLDER.get();
        return currentUser == null ? null : currentUser.getRole();
    }

    public static void clear(){
        HOLDER.remove();
    }

}

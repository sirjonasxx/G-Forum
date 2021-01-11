package gforum.entities;

public enum HForumPermission {
    EVERYONE(0),
    MEMBERS(1),
    ADMINS(2),
    OWNER(3);

    public final int state;

    HForumPermission(int state) {
        this.state = state;
    }

    public static HForumPermission fromValue(int state) {
        switch (state) {
            case 0:
                return EVERYONE;
            case 1:
                return MEMBERS;
            case 2:
                return ADMINS;
            case 3:
                return OWNER;
        }
        return EVERYONE;
    }
}
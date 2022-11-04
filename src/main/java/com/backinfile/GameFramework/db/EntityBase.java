package com.backinfile.GameFramework.db;

public abstract class EntityBase {
    public int id;

    public static final int STATE_NEW = 0;
    public static final int STATE_NORMAL = 1;
    public static final int STATE_DELETE = 2;

    private int state = STATE_NEW;


    public void setState(int state) {
        this.state = state;
    }

    public void save() {
//        switch (state) {
//            case STATE_NEW:
//                DB.insert(this);
//                break;
//            case STATE_NORMAL:
//                DB.update(this);
//                break;
//            case STATE_DELETE:
//                throw new SysException("try to save a deleted entity");
//        }
//        this.state = STATE_NORMAL;
    }

    public void remove() {
        if (state == STATE_NORMAL) {
            DB.delete(this);
        }
        this.state = STATE_DELETE;
    }
}

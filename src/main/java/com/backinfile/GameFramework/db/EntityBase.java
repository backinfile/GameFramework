package com.backinfile.GameFramework.db;

import com.backinfile.support.SysException;

public abstract class EntityBase {
    public long id;

    public static final int STATE_NEW = 0;
    public static final int STATE_NORMAL = 1;
    public static final int STATE_DELETE = 2;

    private int state = STATE_NEW;
    private ISaveProvider saveProvider = DBManager.getSaveProvider();


    public void setState(int state) {
        this.state = state;
    }

    public void setSaveProvider(ISaveProvider saveProvider) {
        this.saveProvider = saveProvider;
    }

    public void save() {
        if (saveProvider == null) {
            return;
        }
        switch (state) {
            case STATE_NEW:
                saveProvider.insert(this);
                break;
            case STATE_NORMAL:
                saveProvider.update(this);
                break;
            case STATE_DELETE:
                throw new SysException("try to save a deleted entity class:" + this.getClass().getName());
        }
        this.state = STATE_NORMAL;
    }

    public void remove() {
        if (saveProvider == null) {
            return;
        }
        if (state == STATE_NORMAL) {
            saveProvider.delete(this);
        }
        this.state = STATE_DELETE;
    }
}

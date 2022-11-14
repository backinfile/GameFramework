package com.backinfile.GameFramework.db;

import com.backinfile.support.SysException;

public abstract class EntityBase {
    public long id;

    private int state = EntityState.STATE_NEW;
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
            case EntityState.STATE_NEW:
                saveProvider.insert(this);
                break;
            case EntityState.STATE_NORMAL:
                saveProvider.update(this);
                break;
            case EntityState.STATE_DELETE:
                throw new SysException("try to save a deleted entity class:" + this.getClass().getName());
            case EntityState.STATE_SERIALIZE:
                throw new SysException("try to save a serialized entity class:" + this.getClass().getName());
            default:
                throw new SysException("");
        }
        this.state = EntityState.STATE_NORMAL;
    }

    public void remove() {
        if (saveProvider == null) {
            return;
        }
        if (state == EntityState.STATE_NORMAL) {
            saveProvider.delete(this);
            this.state = EntityState.STATE_DELETE;
            return;
        }
        throw new SysException("try to delete entity error class:" + this.getClass().getName() + " state:" + state);
    }
}

package com.github.runningforlife.photosniffer.presenter;

/**
 * Realm date updateAsync operation
 */

public enum RealmOp {
    OP_NONE(0),
    OP_INSERT(1),
    OP_DELETE(2),
    OP_BATCH_DELETE(3),
    OP_MODIFY(4),
    OP_REFRESH(5);

    private int mOp;

    RealmOp(int op){
        mOp = op;
    }

    public int getOp(){
        return mOp;
    }
}

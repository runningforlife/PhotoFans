package com.github.runningforlife.photosniffer.presenter;

/**
 * Realm date update operation
 */

public enum UpdateOp {
    OP_NONE(0),
    OP_ADD(1),
    OP_DELETE(2),
    OP_BATCH_DELETE(3);

    private int mOp;

    UpdateOp(int op){
        mOp = op;
    }

    public int getOp(){
        return mOp;
    }
}

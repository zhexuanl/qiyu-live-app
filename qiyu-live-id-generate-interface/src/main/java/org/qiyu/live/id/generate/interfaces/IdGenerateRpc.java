package org.qiyu.live.id.generate.interfaces;


public interface IdGenerateRpc {


    Long getSeqId(Integer id);

    /**
     * Get not sequenced Id
     * @param id
     * @return
     */
    Long getUnSeqId(Integer id);
}

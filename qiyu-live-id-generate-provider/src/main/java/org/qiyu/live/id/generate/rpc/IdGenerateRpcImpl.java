package org.qiyu.live.id.generate.rpc;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.id.generate.interfaces.IdGenerateRpc;
import org.qiyu.live.id.generate.service.IdGenerateService;


@DubboService
@RequiredArgsConstructor
public class IdGenerateRpcImpl implements IdGenerateRpc {

    private final IdGenerateService idGenerateService;

    @Override
    public Long getSeqId(Integer id) {
        return idGenerateService.getSeqId(id);
    }

    @Override
    public Long getUnSeqId(Integer id) {
        return idGenerateService.getUnSeqId(id);
    }
}

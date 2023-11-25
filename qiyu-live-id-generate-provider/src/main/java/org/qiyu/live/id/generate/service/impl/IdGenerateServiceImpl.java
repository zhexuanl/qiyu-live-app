package org.qiyu.live.id.generate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qiyu.live.id.generate.IdAllocationException;
import org.qiyu.live.id.generate.dao.mapper.IdGenerateMapper;
import org.qiyu.live.id.generate.dao.po.IdGeneratePO;
import org.qiyu.live.id.generate.service.IdGenerateService;
import org.qiyu.live.id.generate.service.bo.LocalSeqIdBO;
import org.qiyu.live.id.generate.service.bo.LocalUnSeqIdBO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


@Service
@Slf4j
@RequiredArgsConstructor
public class IdGenerateServiceImpl implements IdGenerateService, InitializingBean {

    private static final Map<Integer, LocalSeqIdBO> localSeqIdBOMap = new ConcurrentHashMap<>();
    private static final Map<Integer, LocalUnSeqIdBO> localUnSeqIdBOMap = new ConcurrentHashMap<>();
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000), r -> {
        Thread thread = new Thread(r);
        thread.setName("id-generate-thread-" + ThreadLocalRandom.current().nextInt(1000));
        return thread;
    });
    private static final float UPDATE_RATE = 0.75f;
    private static final int SEQ_ID = 1;
    private static final Map<Integer, Semaphore> semaphoreMap = new ConcurrentHashMap<>();

    private final IdGenerateMapper idGenerateMapper;

    @Override
    public Long getUnSeqId(Integer id) {
        if (id == null) {
            log.error("[getSeqId] id is error");
            return null;
        }
        final LocalUnSeqIdBO localUnSeqIdBO = localUnSeqIdBOMap.get(id);
        if (localUnSeqIdBO == null) {
            log.error("[getUnSeqId] localUnSeqIdBO is null,id is {}", id);
            return null;
        }

        Long returnId = localUnSeqIdBO.getIdQueue().poll();
        if (returnId == null) {
            log.error("[getUnSeqId] returnId is null,id is {}", id);
            return null;
        }
        this.refreshLocalUnSeqId(localUnSeqIdBO);
        return returnId;
    }

    @Override
    public Long getSeqId(Integer id) {
        if (id == null) {
            log.error("[getSeqId] id is error,id is  null");
            return null;
        }
        LocalSeqIdBO localSeqIdBO = localSeqIdBOMap.get(id);
        if (localSeqIdBO == null) {
            log.error("[getSeqId] localSeqIdBO is null,id is {}", id);
            return null;
        }
        this.refreshLocalSeqId(localSeqIdBO);
        long returnId = localSeqIdBO.getCurrentNum().incrementAndGet();
        if (returnId > localSeqIdBO.getNextThreshold()) {
            //同步去刷新
            log.error("[getSeqId] id is over limit, next id is {}", localSeqIdBO.getCurrentNum());
            return null;
        }
        return returnId;
    }

    /**
     * 刷新本地有序id段
     *
     * @param localSeqIdBO
     */
    private void refreshLocalSeqId(LocalSeqIdBO localSeqIdBO) {
        long step = localSeqIdBO.getNextThreshold() - localSeqIdBO.getCurrentStart();
        if (localSeqIdBO.getCurrentNum().get() - localSeqIdBO.getCurrentStart() > step * UPDATE_RATE) {
            Semaphore semaphore = semaphoreMap.get(localSeqIdBO.getId());
            if (semaphore == null) {
                log.error("semaphore is null,id is {}", localSeqIdBO.getId());
                return;
            }
            boolean acquireStatus = semaphore.tryAcquire();
            if (acquireStatus) {
                log.info("开始尝试进行本地id段的同步操作");
                //异步进行同步id段操作
                threadPoolExecutor.execute(() -> {
                    try {
                        IdGeneratePO idGeneratePO = idGenerateMapper.selectById(localSeqIdBO.getId());
                        tryUpdateMySQLRecord(idGeneratePO);
                    } catch (IdAllocationException e) {
                        log.error("[refreshLocalSeqId] error: ", e);
                    } finally {
                        semaphoreMap.get(localSeqIdBO.getId()).release();
                        log.info("本地有序id段同步完成,id is {}", localSeqIdBO.getId());
                    }
                });
            }
        }
    }

    /**
     * 刷新本地无序id段
     *
     * @param localUnSeqIdBO
     */
    private void refreshLocalUnSeqId(LocalUnSeqIdBO localUnSeqIdBO) {
        long begin = localUnSeqIdBO.getCurrentStart();
        long end = localUnSeqIdBO.getNextThreshold();
        long remainSize = localUnSeqIdBO.getIdQueue().size();
        //如果使用剩余空间不足25%，则进行刷新
        if ((end - begin) * 0.25 > remainSize) {
            Semaphore semaphore = semaphoreMap.get(localUnSeqIdBO.getId());
            if (semaphore == null) {
                log.error("semaphore is null,id is {}", localUnSeqIdBO.getId());
                return;
            }
            boolean acquireStatus = semaphore.tryAcquire();
            if (acquireStatus) {
                threadPoolExecutor.execute(() -> {
                    try {
                        IdGeneratePO idGeneratePO = idGenerateMapper.selectById(localUnSeqIdBO.getId());
                        tryUpdateMySQLRecord(idGeneratePO);
                    } catch (IdAllocationException e) {
                        log.error("[refreshLocalUnSeqId] error is ", e);
                    } finally {
                        semaphoreMap.get(localUnSeqIdBO.getId()).release();
                        log.info("本地无序id段同步完成，id is {}", localUnSeqIdBO.getId());
                    }
                });
            }
        }
    }

    //bean初始化的时候会回调到这里
    @Override
    public void afterPropertiesSet() {
        List<IdGeneratePO> idGeneratePOList = idGenerateMapper.selectAll();
        for (IdGeneratePO idGeneratePO : idGeneratePOList) {
            log.info("服务刚启动，抢占新的id段");
            tryUpdateMySQLRecord(idGeneratePO);
            semaphoreMap.put(idGeneratePO.getId(), new Semaphore(1));
        }
    }

    /**
     * 更新mysql里面的分布式id的配置信息，占用相应的id段
     * 同步执行，很多的网络IO，性能较慢
     *
     * @param idGeneratePO
     */
    private void tryUpdateMySQLRecord(IdGeneratePO idGeneratePO) {

        int updateResult = idGenerateMapper.updateNewIdCountAndVersion(idGeneratePO.getId(), idGeneratePO.getVersion());
        if (updateResult > 0) {
            localIdBOHandler(idGeneratePO);
            return;
        }
        //重试进行更新
        for (int i = 0; i < 3; i++) {
            idGeneratePO = idGenerateMapper.selectById(idGeneratePO.getId());
            updateResult = idGenerateMapper.updateNewIdCountAndVersion(idGeneratePO.getId(), idGeneratePO.getVersion());
            if (updateResult > 0) {
                localIdBOHandler(idGeneratePO);
                return;
            }
        }
        throw new IdAllocationException("表id段占用失败，竞争过于激烈，id is " + idGeneratePO.getId());
    }

    /**
     * 专门处理如何将本地ID对象放入到Map中，并且进行初始化的
     *
     * @param idGeneratePO
     */
    private void localIdBOHandler(IdGeneratePO idGeneratePO) {
        long currentStart = idGeneratePO.getCurrentStart();
        long nextThreshold = idGeneratePO.getNextThreshold();
        long currentNum = currentStart;
        if (idGeneratePO.getIsSeq() == SEQ_ID) {
            AtomicLong atomicLong = new AtomicLong(currentNum);
            LocalSeqIdBO localSeqIdBO = LocalSeqIdBO.builder()
                    .id(idGeneratePO.getId())
                    .currentNum(atomicLong)
                    .currentStart(currentStart)
                    .nextThreshold(nextThreshold)
                    .build();
            localSeqIdBOMap.put(localSeqIdBO.getId(), localSeqIdBO);
        } else {
            LocalUnSeqIdBO localUnSeqIdBO = LocalUnSeqIdBO.builder()
                    .currentStart(currentStart)
                    .nextThreshold(nextThreshold)
                    .id(idGeneratePO.getId())
                    .build();
            long begin = localUnSeqIdBO.getCurrentStart();
            long end = localUnSeqIdBO.getNextThreshold();
            List<Long> idList = new ArrayList<>();
            for (long i = begin; i < end; i++) {
                idList.add(i);
            }
            //将本地id段提前打乱，然后放入到队列中
            Collections.shuffle(idList);
            ConcurrentLinkedQueue<Long> idQueue = new ConcurrentLinkedQueue<>(idList);
            localUnSeqIdBO.setIdQueue(idQueue);
            localUnSeqIdBOMap.put(localUnSeqIdBO.getId(), localUnSeqIdBO);
        }
    }
}

package org.qiyu.live.id.generate.service.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalUnSeqIdBO {

    private int id;

    /**
     * Store unordered ids in this queue ahead of time
     */
    private ConcurrentLinkedQueue<Long> idQueue;

    /**
     * Starting value of the current id segment
     */
    private Long currentStart;

    /**
     * End value of current id segment
     */
    private Long nextThreshold;

    private AtomicLong currentNum;
}

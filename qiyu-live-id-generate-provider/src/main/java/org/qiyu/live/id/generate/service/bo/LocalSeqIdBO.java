package org.qiyu.live.id.generate.service.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalSeqIdBO {

    private int id;
    /**
     * The value of the current ordered id recorded in memory
     */
    private AtomicLong currentNum;

    /**
     * Starting value of the current id field
     */
    private Long currentStart;
    /**
     * End value of current id segment
     */
    private Long nextThreshold;
}

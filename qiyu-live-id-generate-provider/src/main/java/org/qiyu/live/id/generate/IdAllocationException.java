package org.qiyu.live.id.generate;

import lombok.Getter;

@Getter
public class IdAllocationException extends RuntimeException {

    public IdAllocationException(String message) {
        super(message);
    }

    public IdAllocationException(Throwable cause) {
        super(cause);
    }

    public IdAllocationException(String message, Throwable cause) {
        super(message, cause);
    }


}

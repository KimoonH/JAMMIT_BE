package com.jammit_be.common.monitor;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class QueryCountInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        // HTTP 요청 컨텍스트
        RequestContext requestContext = RequestContextHolder.getContext();
        if (requestContext != null) {
            requestContext.incrementQueryCount(sql);
        }

        return sql;
    }
}
